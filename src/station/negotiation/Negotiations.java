package station.negotiation;

import station.EVInfo;
import various.ArrayTransformations;
import various.IntegerConstants;

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

    private SuggestionComputer computer;

    private PriorityQueue<EVInfo> suggestions_queue; // instead of ArrayList<> - smaller to bigger

    public Negotiations(ArrayList<EVInfo> evs, int[][] schedule, int[] chargers) {
        this.evs = evs;
        this.schedule = schedule;
        this.chargers = Arrays.copyOf(chargers, chargers.length);
        computer = new SuggestionComputer(this.chargers, IntegerConstants.SUGGESTION_COMPUTER_INITIAL);

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

            /*
            Suggestion first = computer.lessEnergy(ev.getPreferences());
            this.evaluateSuggestion(ev.getPreferences(), first);

            Suggestion second = computer.alteredWindow(ev.getPreferences());
            this.evaluateSuggestion(ev.getPreferences(), second);

            this.compareSuggestions(ev, first, second);
            */
            computer.computeAlternative(ev);
            suggestions_queue.offer(ev);
        }

        this.printOrderedSuggestions();
        this.updateChargers();
        this.filterSuggestions();
        this.printFinalOrderedSuggestions();

    }


    private void printOrderedSuggestions () {
        System.out.println("Ordered Suggestions");
        int count = 1;
        EVInfo ev = null;
        while (count != suggestions_queue.size() + 1) {
            ev = suggestions_queue.poll();
            System.out.println("    " + count + ". " + ev.getSuggestion().toString() +
            "(ev:" + ev.getId() + ")");
            suggestions_queue.offer(ev);
            count++;
        }
        System.out.println();
    }

    private void printFinalOrderedSuggestions () {
        System.out.println("Ordered Suggestions");
        int count = 0;
        EVInfo ev = null;
        while (count != evs.size()) {
            ev = evs.get(count);
            System.out.println("    " + (count+1) + ". " + ev.getSuggestion().toString() +
                    "(ev:" + ev.getId() + ")");
            count++;
        }
        System.out.println();
    }

    private void updateChargers () {

        System.out.println("Updating Chargers after first computation of Suggestions");
        ArrayTransformations t = new ArrayTransformations();
        boolean alt = false;


        EVInfo ev = null;
        while ((ev = suggestions_queue.poll()) != null) {
            System.out.println("-Updating chargers for ev: " + ev.getId());
            Suggestion suggestion = ev.getSuggestion();

            if (suggestion.getRating() > 15)
                break;
            int count = 0;
            // autos o pinakas krataei poies theseis allaksan kathws ginotan to update
            // gia na tis epanaferei meta an xreiastei
            // NA GINEI VELTISTOPOIHSH - gia na min dimiourgei kathe fora mikos chargers.length,
            // enw xreiazetai poly mikrotero
            int[] slots_changed = new int[chargers.length];
            // 1) take the top suggestion

            //PrintOuch print = new PrintOuch();
            //print.comparePreferences(ev.getPreferences(), suggestion);

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
            if (alt) {
                System.out.println("    Not available chargers found." +
                        "Must compute alternative suggestion.\n");
                computer.computeAlternative(ev);
                suggestions_queue.offer(ev);
            }
            else {
                System.out.println("    Chargers updated successfully!\n");
                ev.setHasSuggestion(true);
                ev.getSuggestion().setSlotsAffected(slots_changed);
            }

            alt = false;
        }

    }

    /**
     * Set the final suggestions to each EV
     * For some EVs, suggestions may could not be computed
     * So these evs should be filtered out before the conversation begins
     * Maybe it can add them directly and not to parse the whole list
     * for better optimization - but not a major problem
     */
    private void filterSuggestions () {

        ArrayList<EVInfo> removed = new ArrayList<>();
        for (EVInfo ev: evs) {
            if (!ev.hasSuggestion()) removed.add(ev);
        }

        for (EVInfo ev: removed) {
            if (!ev.hasSuggestion()) evs.remove(ev);
        }

    }


    /**
     * Combine the two types of suggestions: less energy + modify window
     */
    private void hybrid () {

    }




    public ArrayList<EVInfo> getFilteredSuggestionList () {
        return evs;
    }

    public SuggestionComputer getComputer () {
        return computer;
    }

    public int[] getChargers() {
        return chargers;
    }


}
