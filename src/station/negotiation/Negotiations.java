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
        comparator = (o1, o2) -> {
            int comp = o1.getSuggestion().getRating() - o2.getSuggestion().getRating();
            if (comp != 0)
                return comp;
            else {
                return o2.getSuggestion().getProfit() - o1.getSuggestion().getProfit();
            }
        };

        suggestions_queue = new PriorityQueue<>(5, comparator);
    }

    /**
     * For each EVObject not charged compute some suggestions
     */
    public boolean computeSuggestions() {

        // compute suggestions
        SuggestionsOptimizer optimizer = new SuggestionsOptimizer(evs, chargers, pricing);
        optimizer.optimizeSuggestions();

        return !optimizer.isEmpty();
    }

    private void printOrderedSuggestions() {
        //System.out.println("Ordered Suggestions");
        int count = 0;
        EVObject ev;
        ArrayList<EVObject> temp = new ArrayList<>();
        if (suggestions_queue.size() > 0) {
            while (count < suggestions_queue.size() + 1) {
                ev = suggestions_queue.poll();
                //System.out.println("    " + count + ". " + ev.getSuggestion().toString() +
                        //"(ev:" + ev.getId() + ")");
                temp.add(ev);
                //suggestions_queue.offer(ev);
                count++;
            }

            for (EVObject e : temp) {
                suggestions_queue.offer(e);
            }
            //System.out.println();
        } else {
            //System.out.println("No suggestions!");
            finish = true;
        }
    }


    private void printFinalOrderedSuggestions() {
        System.out.println("Ordered Suggestions");
        evs.sort(comparator);
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

    public ArrayList<EVObject> getFilteredSuggestionList() {
        return evs;
    }

    public int[] getChargers() {
        return chargers;
    }

}
