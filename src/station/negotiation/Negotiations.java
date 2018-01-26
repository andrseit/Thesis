package station.negotiation;

import optimize.SuggestionsOptimizer;
import station.EVObject;
import station.pricing.Pricing;

import java.util.*;

/**
 * This class takes as inputs the evs that did not fit in the initial schedule, the initial schedule and the remaining chargers
 * and computes suggestions to return to the station.
 */
public class Negotiations {


    private ArrayList<EVObject> evs;
    private int[] chargers;
    private Pricing pricing;

    private boolean finish;

    private Comparator<EVObject> comparator;

    private PriorityQueue<EVObject> suggestions_queue; // instead of ArrayList<> - smaller to bigger

    public Negotiations(ArrayList<EVObject> evs, int[] chargers, Pricing pricing) {
        this.evs = evs;
        this.chargers = Arrays.copyOf(chargers, chargers.length);
        this.pricing = pricing;
        comparator = new Comparator<EVObject>() {
            @Override
            public int compare(EVObject o1, EVObject o2) {
                int comp = o1.getSuggestion().getRating() - o2.getSuggestion().getRating();
                if (comp != 0)
                    return comp;
                else {
                    return o2.getSuggestion().getProfit() - o1.getSuggestion().getProfit();
                }
            }
        };

        suggestions_queue = new PriorityQueue<>(5, comparator);
    }


    /**
     * For each EVObject not charged compute some suggestions
     */
    public int computeSuggestions() {

        // compute suggestions
        SuggestionsOptimizer optimizer = new SuggestionsOptimizer(evs, chargers, pricing);
        optimizer.optimizeSuggestions();

//        int util;
//        for (EVObject ev: evs) {
//
//            /*
//            Suggestion first = computer.lessEnergy(ev.getPreferences());
//            this.evaluateSuggestionOld(ev.getPreferences(), first);
//
//            Suggestion second = computer.alteredWindow(ev.getPreferences());
//            this.evaluateSuggestionOld(ev.getPreferences(), second);
//
//            this.compareSuggestions(ev, first, second);
//            */
//            computer.computeAlternative(ev);
//            if (ev.hasSuggestion())
//                suggestions_queue.offer(ev);
//        }
//        if (suggestions_queue.size() > 0) {
//            //this.computeProfits();
//            this.printOrderedSuggestions();
//            this.updateChargers();
//            this.filterSuggestions();
//            util = this.computeUtility();
//            this.printFinalOrderedSuggestions();
//            return util;
//        } else {
//            return -1;
//        }
        return -1;
    }


    private int computeUtility() {

        int utility = 0;

        for (EVObject ev : evs) {
            utility += ev.getSuggestion().getProfit();
        }
        return utility;
    }

    private void printOrderedSuggestions() {
        System.out.println("Ordered Suggestions");
        int count = 0;
        EVObject ev;
        ArrayList<EVObject> temp = new ArrayList<>();
        if (suggestions_queue.size() > 0) {
            while (count < suggestions_queue.size() + 1) {
                ev = suggestions_queue.poll();
                System.out.println("    " + count + ". " + ev.getSuggestion().toString() +
                        "(ev:" + ev.getId() + ")");
                temp.add(ev);
                //suggestions_queue.offer(ev);
                count++;
            }

            for (EVObject e : temp) {
                suggestions_queue.offer(e);
            }
            System.out.println();
        } else {
            System.out.println("No suggestions!");
            finish = true;
        }
    }

    public boolean hasFinished() {
        return finish;
    }

    private void printFinalOrderedSuggestions() {
        System.out.println("Ordered Suggestions");
        Collections.sort(evs, comparator);
        int count = 0;
        EVObject ev;
        while (count != evs.size()) {
            ev = evs.get(count);
            System.out.println("    " + (count + 1) + ". " + ev.getSuggestion().toString() +
                    "(ev:" + ev.getId() + ")");
            count++;
        }
        System.out.println();
    }

    /*
    private void updateChargers () {

        System.out.println("Updating Chargers after first computation of Suggestions");
        ArrayTransformations t = new ArrayTransformations();
        t.printOneDimensionArray("Before", chargers);
        boolean alt = false;


        EVObject ev = null;
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

            if (alt) {
                System.out.println("    Not available chargers found." +
                        "Must compute alternative suggestion.\n");
                ev.resetBestsUpdatingChargers();
                computer.computeAlternative(ev);
                if (ev.hasSuggestion())
                    suggestions_queue.offer(ev);
            }
            else {
                t.printOneDimensionArray("After", chargers);
                System.out.println("    Chargers updated successfully!\n");
                //ev.setHasSuggestion(true);
                ev.getSuggestion().setSlotsAffected(slots_changed);
            }

            alt = false;
        }

    }
    */

    /**
     * Set the final suggestions to each EV
     * For some EVs, suggestions may could not be computed
     * So these evs should be filtered out before the conversation begins
     * Maybe it can add them directly and not to parse the whole list
     * for better optimization - but not a major problem
     */
    private void filterSuggestions() {

        ArrayList<EVObject> removed = new ArrayList<>();
        for (EVObject ev : evs) {
            if (!ev.hasSuggestion()) removed.add(ev);
        }

        for (EVObject ev : removed) {
            if (!ev.hasSuggestion()) evs.remove(ev);
        }

    }

    public ArrayList<EVObject> getFilteredSuggestionList() {
        return evs;
    }

    public int[] getChargers() {
        return chargers;
    }


}
