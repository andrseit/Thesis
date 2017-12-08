package station.negotiation;

import station.EVInfo;
import evs.Preferences;
import various.ArrayTransformations;
import various.PrintOuch;

import java.util.*;

/**
 * Created by Thesis on 18/11/2017.
 */

/**
 * This class takes as inputs the evs that did not fit in the initial schedule, the initial schedule and the remaining chargers
 * and computes suggestions to return to the station.
 */
public class Negotiations {

    private ArrayList<EVInfo> evs;
    private int[][] schedule;
    private int[] chargers;

    private PriorityQueue<EVInfo> suggestions_queue; // instead of ArrayList<> - smaller to bigger
    private ArrayList<Preferences> suggestions; // this list contains the final suggestions

    public Negotiations(ArrayList<EVInfo> evs, int[][] schedule, int[] chargers) {
        this.evs = evs;
        this.schedule = schedule;
        this.chargers = Arrays.copyOf(chargers, chargers.length);

        suggestions = new ArrayList<>();
        suggestions_queue = new PriorityQueue<>(evs.size(), new Comparator<EVInfo>() {
            @Override
            public int compare(EVInfo o1, EVInfo o2) {
                return o2.getSuggestion().getRating() - o1.getSuggestion().getRating();
            }
        });
    }

    /**
     * For each EVInfo not charged compute some suggestions
     */
    public void computeSuggestions () {
        for (EVInfo ev: evs) {

            Suggestion first = this.lessEnergy(ev.getPreferences());
            this.evaluateSuggestion(ev.getPreferences(), first);

            Suggestion second = this.alteredWindow(ev.getPreferences());
            this.evaluateSuggestion(ev.getPreferences(), second);

            this.compareSuggestions(ev, first, second);
            suggestions_queue.offer(ev);
        }


        this.updateChargers();
        ArrayTransformations t = new ArrayTransformations();
        t.printOneDimensionArray("Final Chargers: ", chargers);


    }

    private void compareSuggestions (EVInfo ev, Suggestion s1, Suggestion s2) {
        int difference = s1.getRating() - s2.getRating();
        if (difference < 0) {
            ev.setSuggestion(s1);
        }
        else {
            ev.setSuggestion(s2);
        }

    }


    /**
     * This method will take as a parameter a suggestion info an it will compute
     * an alternative suggestion
     */
    private void computeAlternative (EVInfo ev) {

        System.out.println("Computing alternative");

        Suggestion less_energy_suggestion = this.lessEnergy(ev.getPreferences());
        Suggestion altered_window_suggestion = this.alteredWindow(ev.getPreferences());
        this.evaluateSuggestion(ev.getPreferences(), less_energy_suggestion);
        this.evaluateSuggestion(ev.getPreferences(), altered_window_suggestion);
        this.compareSuggestions(ev, less_energy_suggestion, altered_window_suggestion);

        suggestions_queue.offer(ev);
    }

    private void updateChargers () {

        System.out.println("Updating Chargers");
        ArrayTransformations t = new ArrayTransformations();
        boolean alt = false;


        EVInfo ev = null;
        while ((ev = suggestions_queue.poll()) != null) {

            Suggestion suggestion = ev.getSuggestion();

            if (suggestion.getRating() > 10)
                break;
            int count = 0;
            // autos o pinakas krataei poies theseis allaksan kathws ginotan to update
            // gia na tis epanaferei meta an xreiastei
            // NA GINEI VELTISTOPOIHSH - gia na min dimiourgei kathe fora mikos chargers.length,
            // enw xreiazetai poly mikrotero
            int[] slots_changed = new int[chargers.length];
            // 1) take the top suggestion

            PrintOuch print = new PrintOuch();
            print.comparePreferences(ev.getPreferences(), suggestion);

            // 2) update chargers that this suggestion consumes
            for (int s = suggestion.getStart(); s <= suggestion.getEnd(); s++) {
                if (chargers[s] > 0) {
                    chargers[s]--;
                    slots_changed[s] = 1;
                    count++;
                }
                // 2.1) if not available chargers, then reset the array
            }
            if (count < suggestion.getEnergy()) {
                alt = true;
                for (int s = suggestion.getStart(); s <= suggestion.getEnd(); s++) {
                    if (slots_changed[s] == 1)
                        chargers[s]++;
                    // 2.1) if not available chargers, then reset the array
                }
            }
            t.printOneDimensionArray("Chargers", chargers);
            if (alt) this.computeAlternative(ev);

            alt = false;
        }

    }

    /**
     * Set the final suggestions to each EV
     * For some EVs, suggestions may could not be computed
     * So these evs should be filtered out before the conversation begins
     */
    /*
    private void filterSuggestions () {
        SuggestionInfo suggestion = null;

        while ((suggestion = suggestions_queue.poll()) != null) {
            suggestions.add(suggestion.getSuggestion()); // add the suggestion to the general list - NOT NEEDED
        }

    }
    */
    
    /**
     * First type of suggestion: Less energy in the initially given window
     * WARNING: Add a case where the available energy is zero - return a huge value
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
     * Take as input a suggestion (new preferences) and the initial preferences of an EVInfo.
     * Based on some metric check if the suggestion is legitimate.
     */
    private void evaluateSuggestion (Preferences initial, Suggestion suggestion) {

        int start_dif = Math.abs(initial.getStart() - suggestion.getStart());
        int end_diff = Math.abs(initial.getEnd() - suggestion.getEnd());
        int energy_diff = initial.getEnergy() - suggestion.getEnergy();

        suggestion.setRating(start_dif + end_diff + energy_diff);
        //System.out.println("\nDifferences: " + start_dif + ", " + end_diff + ", " + energy_diff);

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
