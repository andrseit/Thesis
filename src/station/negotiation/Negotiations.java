package station.negotiation;

import evs.EV;
import evs.Preferences;
import various.ArrayTransformations;

import java.util.*;

/**
 * Created by Thesis on 18/11/2017.
 */

/**
 * This class takes as inputs the evs that did not fit in the initial schedule, the initial schedule and the remaining chargers
 * and computes suggestions to return to the station.
 */
public class Negotiations {

    private class SuggestionInfo {
        private EV ev;
        private Suggestion suggestion;
        private Suggestion alternative;
        private boolean less_energy;

        public SuggestionInfo(EV ev) {
            this.ev = ev;
        }

        public int getSuggestionRating() {
            return suggestion.getRating();
        }

        public void setSuggesion (Suggestion s) {
           this.suggestion = s;
        }

        public void setAlternative (Suggestion s) {
            this.alternative = s;
        }

        public Suggestion getSuggestion() {
            return suggestion;
        }

        public Suggestion getAlternative() {
            return alternative;
        }

        public EV getEV() { return ev; }

        public void setSuggestionType (boolean less_energy) {
            this.less_energy = less_energy;
        }

        public boolean getSuggestionType () { return less_energy; }

        public String toString () {
            StringBuilder str = new StringBuilder();
            str.append("Start: " + ev.getStartSlot() + "/" + suggestion.getStart() + "(" + alternative.getStart() + ")\n");
            str.append("End: " + ev.getEndSlot() + "/" + suggestion.getEnd() + "(" + alternative.getEnd() + ")\n");
            str.append("Energy: " + ev.getEnergy() + "/" + suggestion.getEnergy() + "(" + alternative.getEnergy() + ")\n");
            str.append("Rating: " + suggestion.getRating() + "\n");
            return str.toString();
        }
    }

    private ArrayList<EV> evs;
    private int[][] schedule;
    private int[] chargers;

    private ArrayList<SuggestionInfo> suggestions_list; // each row represents and ev - parallel with evs: ArrayList<EV>
    private PriorityQueue<SuggestionInfo> suggestions_queu; // instead of ArrayList<> - smaller to bigger

    public Negotiations(ArrayList<EV> evs, int[][] schedule, int[] chargers) {
        this.evs = evs;
        this.schedule = schedule;
        this.chargers = Arrays.copyOf(chargers, chargers.length);

        suggestions_list = new ArrayList<>();
        suggestions_queu = new PriorityQueue<>(evs.size(), new Comparator<SuggestionInfo>() {
            @Override
            public int compare(SuggestionInfo o1, SuggestionInfo o2) {
                return o2.getSuggestionRating() - o1.getSuggestionRating();
            }
        });
    }

    /**
     * For each EV not charged compute some suggestions
     */
    public void computeSuggestions () {
        for (EV ev: evs) {
            //this.lessEnergy(ev.getPreferences());
            SuggestionInfo suggestion = new SuggestionInfo(ev);

            Suggestion first = this.lessEnergy(ev.getPreferences());
            this.evaluateSuggestion(ev.getPreferences(), first);

            Suggestion second = this.alteredWindow(ev.getPreferences());
            this.evaluateSuggestion(ev.getPreferences(), second);

            this.compareSuggestions(suggestion, first, second);
            suggestions_list.add(suggestion);
            suggestions_queu.offer(suggestion);
        }

        this.sortSuggestionsList();
        for (SuggestionInfo s: suggestions_list) {
            System.out.println(s.toString());
        }

        this.updateChargers();
        ArrayTransformations t = new ArrayTransformations();
        t.printOneDimensionArray("Final Chargers: ", chargers);


    }

    private void compareSuggestions (SuggestionInfo suggestion, Suggestion s1, Suggestion s2) {
        int difference = s1.getRating() - s2.getRating();
        System.out.println("Difference: " + difference);
        if (difference < 0) {
            suggestion.setSuggesion(s1);
            suggestion.setAlternative(s2);
            suggestion.setSuggestionType(true);
        }
        else {
            suggestion.setSuggesion(s2);
            suggestion.setAlternative(s1);
            suggestion.setSuggestionType(false);
        }

    }


    private void sortSuggestionsList () {
        // what is going on
        Collections.sort(suggestions_list, new Comparator<SuggestionInfo>() {
            @Override
            public int compare(SuggestionInfo o1, SuggestionInfo o2) {
                return o2.getSuggestionRating()-o1.getSuggestionRating();
            }
        });
    }

    /**
     * This method will take as a parameter a suggestion info an it will compute
     * an alternative suggestion
     */
    private void computeAlternative (SuggestionInfo suggestion) {

        System.out.println("Computing alternative");

        Suggestion less_energy_suggestion = this.lessEnergy(suggestion.getEV().getPreferences());
        Suggestion altered_window_suggestion = this.alteredWindow(suggestion.getEV().getPreferences());
        this.evaluateSuggestion(suggestion.getEV().getPreferences(), less_energy_suggestion);
        this.evaluateSuggestion(suggestion.getEV().getPreferences(), altered_window_suggestion);
        this.compareSuggestions(suggestion, less_energy_suggestion, altered_window_suggestion);

        suggestions_queu.offer(suggestion);
        suggestions_list.add(suggestion);
        System.out.println(suggestion.toString());
    }

    private void updateChargers () {

        System.out.println("Updating Chargers");
        ArrayTransformations t = new ArrayTransformations();
        boolean alt = false;


        SuggestionInfo suggestion = null;
        while ((suggestion = suggestions_queu.poll()) != null) {

            if (suggestion.getSuggestionRating() > 10)
                break;
            int count = 0;
            // autos o pinakas krataei poies theseis allaksan kathws ginotan to update
            // gia na tis epanaferei meta an xreiastei
            // NA GINEI VELTISTOPOIHSH - gia na min dimiourgei kathe fora mikos chargers.length,
            // enw xreiazetai poly mikrotero
            int[] slots_changed = new int[chargers.length];
            // 1) take the top suggestion

            System.out.println(suggestion.toString() + "\n apo update");
            // 2) update chargers that this suggestion consumes
            for (int s = suggestion.getSuggestion().getStart(); s <= suggestion.getSuggestion().getEnd(); s++) {
                if (chargers[s] > 0) {
                    chargers[s]--;
                    slots_changed[s] = 1;
                    count++;
                }
                // 2.1) if not available chargers, then reset the array
            }
            System.out.println("Count: " + count);
            if (count < suggestion.getSuggestion().getEnergy()) {
                alt = true;
                for (int s = suggestion.getSuggestion().getStart(); s <= suggestion.getSuggestion().getEnd(); s++) {
                    if (slots_changed[s] == 1)
                        chargers[s]++;
                    // 2.1) if not available chargers, then reset the array
                }
            }
            t.printOneDimensionArray("Chargers", chargers);
            if (alt) this.computeAlternative(suggestion);

            alt = false;
        }

    }

    /**
     * First type of suggestion: Less energy in the initially given window
     */
    private Suggestion lessEnergy (Preferences initial) {

        Suggestion new_preferences = new Suggestion();
        new_preferences.setStart(initial.getStart());
        new_preferences.setEnd(initial.getEnd());

        int available_energy = this.energyInSlots(initial.getStart(), initial.getEnd());
        new_preferences.setEnergy(available_energy);
        System.out.println("Energy found in given slots is: " + available_energy);
        return new_preferences;
    }

    /**
     * Finds the available energy between two time points.
     * @param start: start slot
     * @param end: end slot
     * @return
     */
    private int energyInSlots (int start, int end) {
        int available_energy = 0;
        for (int slot = start; slot <= end; slot++) {
            if (chargers[slot] != 0)
                available_energy ++;
        }
        return available_energy;
    }

    /**
     * Second type of suggestion: Modify window (change given start-end slots) but keep the same energy
     */
    private Suggestion alteredWindow (Preferences initial) {

        Suggestion new_preferences = new Suggestion();
        new_preferences.setEnergy(initial.getEnergy());

        int available_energy = energyInSlots(initial.getStart(), initial.getEnd());
        int[] new_slots = this.searchSlots(1, initial, available_energy);

        new_preferences.setStartEndSlots(new_slots[0], new_slots[1]);
        System.out.println(new_preferences.toString());

        return new_preferences;
    }

    private int[] searchSlots (int step, Preferences p, int available_energy) {

        ArrayTransformations t = new ArrayTransformations();
        t.printOneDimensionArray("Search Slots Chargers: ", chargers);
        int left;
        int right;

        if (step == -1) {
            right = p.getStart();
            left = p.getEnd();
        } else {
            left = p.getStart();
            right = p.getEnd();
        }

        while (available_energy < p.getEnergy()) {
            right += step;
            if(right == chargers.length || right < 0) {
                right -= step;
                break;
            }
            if (chargers[right] !=0 ) available_energy++;
        }
        //if (available_energy < initial.getEnergy()) left_start = initial.getStart();

        // search to the right
        while (available_energy < p.getEnergy()) {
            left -= step;
            if(left == chargers.length || left < 0) {
                left += step;
                break;
            }
            if (chargers[left] !=0 ) available_energy++;
        }

        // clear zero slots
        while (chargers[left] == 0) {
            left += step;
        }
        while (chargers[right] == 0) {
            right -= step;
        }

        int[] new_slots = new int[2];

        // gia na epistrefei kati terastio kai etC na exei xalia rating
        // opote na min epilegetai pote
        if (available_energy < p.getEnergy()) {
            left = Integer.MAX_VALUE;
            right = Integer.MAX_VALUE;
        }

        if (left > right) {
            new_slots[0] = right;
            new_slots[1] = left;
        } else {
            new_slots[0] = left;
            new_slots[1] = right;
        }

        return new_slots;
    }
    /**
     * Combine the two types of suggestions: less energy + modify window
     */
    private void hybrid () {

    }

    /**
     * Take as input a suggestion (new preferences) and the initial preferences of an EV.
     * Based on some metric check if the suggestion is legitimate.
     */
    private void evaluateSuggestion (Preferences initial, Suggestion suggestion) {

        int start_dif = Math.abs(initial.getStart() - suggestion.getStart());
        int end_diff = Math.abs(initial.getEnd() - suggestion.getEnd());
        int energy_diff = initial.getEnergy() - suggestion.getEnergy();

        suggestion.setRating(start_dif + end_diff + energy_diff);
        System.out.println("\nDifferences: " + start_dif + ", " + end_diff + ", " + energy_diff);

    }

    private void adjustCollisions () {

    }

    /**
     * Adds a suggestion to the list with the alternative suggestions
     */
    private void addSuggestion () {

    }

    /**
     * Returns the list of the suggestions
     */
    private void getSuggestions () {

    }


}
