package optimize;

import station.EVObject;
import station.negotiation.Suggestion;
import station.negotiation.SuggestionComputer;

import java.util.*;

/**
 * Created by Thesis on 8/1/2018.
 */
public class SuggestionsOptimizer {

    private ArrayList<EVObject> evs;
    private int[] remaining_chargers;
    private int[] price;
    private int utility;
    private SuggestionComputer computer;

    private Comparator<EVObject> comparator;
    private PriorityQueue<EVObject> suggestions_queue; // instead of ArrayList<> - smaller to bigger
    private boolean empty;

    public SuggestionsOptimizer(ArrayList<EVObject> evs, int[] remaining_chargers, int[] price) {
        this.evs = evs;
        this.remaining_chargers = Arrays.copyOf(remaining_chargers, remaining_chargers.length);
        this.price = price;
        computer = new SuggestionComputer(this.remaining_chargers, price, 0);

        comparator = (o1, o2) -> {
            int comp = o1.getSuggestion().getRating() - o2.getSuggestion().getRating();
            if (comp != 0)
                return comp;
            else {
                return o2.getSuggestion().getProfit() - o1.getSuggestion().getProfit();
            }
        };
        suggestions_queue = new PriorityQueue<>(5, comparator);
        empty = false;
    }

    /**
     * Computes the final suggestions for the given evs
     */
    public void optimizeSuggestions () {
        for (EVObject ev: evs) {
            computer.computeAlternative(ev);
            if (ev.hasSuggestion())
                suggestions_queue.offer(ev);
        }

        if (suggestions_queue.size() > 0) {
            this.printOrderedSuggestions();
            this.updateChargers();
            //this.filterSuggestions();
            this.computeUtility();
            this.printFinalOrderedSuggestions();
        } else {
            //this.filterSuggestions();
            empty = true;
        }
    }

    private void updateChargers () {
        System.out.println("Updating Chargers after first computation of Suggestions");
        boolean alt = false;

        EVObject ev;

        while ((ev = suggestions_queue.poll()) != null) {
            System.out.println("-Updating chargers for ev: " + ev.getId());
            Suggestion suggestion = ev.getSuggestion();

            if (suggestion.getRating() > 15)
                break;

            int count = 0;
            int[] slots_changed = new int[remaining_chargers.length];

            // 2) update chargers that this suggestion consumes
            for (int s = suggestion.getStart(); s <= suggestion.getEnd(); s++) {
                if (remaining_chargers[s] > 0) {
                    remaining_chargers[s]--;
                    slots_changed[s] = 1;
                    count++;
                }

            }
            // 2.1) if not available chargers, then reset the array
            if (count < suggestion.getEnergy()) {
                alt = true;
                for (int s = suggestion.getStart(); s <= suggestion.getEnd(); s++) {
                    if (slots_changed[s] == 1)
                        remaining_chargers[s]++;
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
                System.out.println("    Chargers updated successfully!\n");
                //ev.setHasSuggestion(true);
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

        ArrayList<EVObject> removed = new ArrayList<>();
        for (EVObject ev: evs) {
            if (!ev.hasSuggestion()) removed.add(ev);
        }

        for (EVObject ev: removed) {
            System.out.println("EV in removed: " + ev.getId());
            if (!ev.hasSuggestion()) evs.remove(ev);
        }

    }

    private void computeUtility () {

        int util = 0;

        for (EVObject ev: evs) {
            util += ev.getSuggestion().getProfit();
        }
        utility = util;
    }

    private void printOrderedSuggestions () {
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
            //finish = true;
        }
    }

    private void printFinalOrderedSuggestions () {
        System.out.println("Ordered Suggestions");
        Collections.sort(evs, comparator);
        int count = 0;
        EVObject ev;
        while (count != evs.size()) {
            ev = evs.get(count);
            System.out.println("    " + (count+1) + ". " + ev.getSuggestion().toString() +
                    "(ev:" + ev.getId() + ")");
            count++;
        }
        System.out.println();
    }

    public int getUtility() {
        return utility;
    }

    public boolean isEmpty () {
        return empty;
    }
}
