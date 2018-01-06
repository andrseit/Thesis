package station.negotiation;

import evs.Preferences;
import station.EVInfo;
import various.ArrayTransformations;
import various.IntegerConstants;
import various.PrintOuch;

/**
 * This class contains methods that compute the suggestions for the evs
 * Created by Thesis on 13/12/2017.
 */
public class SuggestionComputer {


    private int[] chargers;
    private int[] price;
    /**
     * this variable show whether the computer is used to computed the initial
     * suggestions, before the conversation starts, or it is used while
     * the conversation is running
     * because if it is at the time of the conversation then it has to check
     * if the new suggestion is better than the previous
     * but in the initial suggestions it just needs to find a suitable suggestion
     */
    private int state;

    public SuggestionComputer (int[] chargers, int[] price, int state) {
        this.chargers = chargers;
        this.price = price;
        this.state = state;
    }

    /**
     * First type of suggestion: Less energy in the initially given window
     * WARNING: Add a case where the available energy is zero - return a huge value
     */
    public Suggestion lessEnergy (Preferences initial) {

        System.out.println("    *Computing less energy...");
        Suggestion suggestion = new Suggestion();
        suggestion.setStart(initial.getStart());
        suggestion.setEnd(initial.getEnd());

        suggestion.setEnergy(this.energyInSlots(initial.getStart(), initial.getEnd(), suggestion));
        System.out.println("        Energy found in given slots is: " + suggestion.getEnergy());
        return suggestion;
    }

    /**
     * Finds the available energy between two time points.
     * @param start: start slot
     * @param end: end slot
     * @return
     */
    private int energyInSlots (int start, int end, Suggestion suggestion) {
        int available_energy = 0;
        int[] affected_slots = new int[chargers.length];
        for (int slot = start; slot <= end; slot++) {
            if (chargers[slot] != 0) {
                affected_slots[slot] = 1;
                available_energy++;
            }
        }
        suggestion.setSlotsAffected(affected_slots);
        return available_energy;
    }

    /**
     * Second type of suggestion: Modify window (change given start-end slots) but keep the same energy
     */
    public Suggestion alteredWindow (Preferences initial) {

        System.out.println("    *Computing altered window...");
        Suggestion suggestion = new Suggestion();
        suggestion.setEnergy(initial.getEnergy());

        int available_energy = energyInSlots(initial.getStart(), initial.getEnd(), suggestion);
        this.searchSlots(1, initial, available_energy, suggestion);

        //System.out.println(new_preferences.toString());
        System.out.println("        Slots found: " + suggestion.getStart() + "-" + suggestion.getEnd() + "/" + suggestion.getEnergy());
        return suggestion;
    }

    private void searchSlots (int step, Preferences p, int available_energy, Suggestion suggestion) {

        //ArrayTransformations t = new ArrayTransformations();
        //t.printOneDimensionArray("Search Slots Chargers: ", chargers);
        int left;
        int right;
        int[] affected_slots = new int[chargers.length];

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
            if (chargers[right] !=0 ) {
                affected_slots[right] = 1;
                available_energy++;
            }
        }
        //if (available_energy < initial.getEnergy()) left_start = initial.getStart();

        // search to the right
        while (available_energy < p.getEnergy()) {
            left -= step;
            if(left == chargers.length || left < 0) {
                left += step;
                break;
            }
            if (chargers[left] != 0 ) {
                affected_slots[left] = 1;
                available_energy++;
            }
        }

        // clear zero slots
        while (chargers[left] == 0 && (left < chargers.length && left > 0)) {
            left += step;
        }
        while (chargers[right] == 0 && (right < chargers.length && right > 0)) {
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

        suggestion.setSlotsAffected(affected_slots);
        suggestion.setStartEndSlots(new_slots[0], new_slots[1]);
    }

    /**
     * This method will take as a parameter a suggestion info an it will compute
     * an alternative suggestion
     * WARNING: na chekarw na min einai idia me tin proigoumeni - return false na nai
     */
    public void computeAlternative (EVInfo ev) {

        System.out.println("--Computing alternative for ev: " + ev.getId());

        Suggestion less_energy_suggestion = this.lessEnergy(ev.getPreferences());
        less_energy_suggestion.setType(IntegerConstants.LESS_ENERGY_TYPE);
        Suggestion altered_window_suggestion = this.alteredWindow(ev.getPreferences());
        altered_window_suggestion.setType(IntegerConstants.ALTERED_WINDOW_TYPE);
        this.evaluateSuggestion(ev.getPreferences(), less_energy_suggestion);
        this.evaluateSuggestion(ev.getPreferences(), altered_window_suggestion);
        this.compareSuggestions(ev, less_energy_suggestion, altered_window_suggestion);
        this.checkSuggestion(ev);
        if (ev.hasSuggestion())
            this.computeProfit(ev);


        //suggestions_queue.offer(ev);
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

    private void computeProfit (EVInfo ev) {
        Suggestion suggestion = ev.getSuggestion();
        int start = suggestion.getStart();
        int end = suggestion.getEnd();
        int energy = suggestion.getEnergy();
        int bid = ev.getBid();
        int profit = 0;
        for (int s = start; s <= end; s++) {
            if (chargers[s] > 0) {
                profit += bid - price[s];
            }
        }
        suggestion.setProfit(profit);
    }

    /**
     * Checks if the suggestion is legit, sets if the ev has suggestion
     * also checks if is better than previous
     * @param ev
     */
    private void checkSuggestion (EVInfo ev) {
     Suggestion suggestion = ev.getSuggestion();
     if (suggestion.getStart() == Integer.MAX_VALUE || suggestion.getEnd() == Integer.MAX_VALUE || suggestion.getEnergy() == 0) {
         ev.setHasSuggestion(false);
     }

     /*
     if (ev.getSuggestion().getType() == IntegerConstants.LESS_ENERGY_TYPE) {
         if (ev.getSuggestion().getRating() >= ev.getBestLessEnergy()) {
             ev.setHasSuggestion(false);
         } else {
             ev.setBestRating(suggestion.getType(), suggestion.getRating());
         }
     } else {
         if (ev.getSuggestion().getRating() >= ev.getBestAlteredWindow()) {
             ev.setHasSuggestion(false);
         } else {
             ev.setBestRating(suggestion.getType(), suggestion.getRating());
         }
     }
     */
    }


    /**
     * WARNING: prosoxi na min enallasontai oi protaseis, dld proteinei mia l.e. = 3
     * meta a.w.=4-7 to arneitai meta gyrizei pali sto l.e. = 3
     * @param ev
     * @param s1
     * @param s2
     * @return
     */
    private void compareSuggestions (EVInfo ev, Suggestion s1, Suggestion s2) {
        int difference = s1.getRating() - s2.getRating();
        Suggestion best, alt;
        if (difference < 0) {
            best = s1;
            alt = s2;
            //ev.setSuggestion(s1);
        }
        else {
            best = s2;
            alt = s1;
            //ev.setSuggestion(s2);
        }

        System.out.println("State: " + state);
        if (state == IntegerConstants.SUGGESTION_COMPUTER_CONVERSATION) {
            System.out.println("OK");
            if (isBetter(ev, best)) {
                System.out.println("OK1");
                ev.setSuggestion(best);
                ev.setHasSuggestion(true);
            } else if (isBetter(ev, alt)) {
                System.out.println("OK2");
                System.out.println(alt.toString());
                ev.setSuggestion(alt);
                ev.setHasSuggestion(true);
            } else {
                System.out.println("OK3");
                ev.setHasSuggestion(false);
            }
        } else {
            System.out.println("What zi fak");
            ev.setSuggestion(best);
            ev.setBestRating(best.getType(), best.getRating());
            ev.setHasSuggestion(true);
        }

        /*
        if (!isSame(ev.getSuggestion(), best)) {
            ev.setSuggestion(best);
            System.out.println("    ** Chosen Suggestion: " + ev.getSuggestion().toString() + " **\n");
            return true;
        } else {
            ev.setSuggestion(alt);
            System.out.println("    ** Chosen Suggestion: " + ev.getSuggestion().toString() + " **\n");
            return true;
        }
        */
    }

    /**
     * WARNING: min ksexasw na kanw set to bestRating sto EVInfo, otan einai initial
     * @param ev
     * @param suggestion
     * @return
     */
    private boolean isBetter (EVInfo ev, Suggestion suggestion) {

        if (suggestion.getType() == IntegerConstants.LESS_ENERGY_TYPE) {
            if (suggestion.getRating() < ev.getBestLessEnergy()) {
                ev.setBestLessEnergy(suggestion.getRating());
                suggestion.findSlotsAffected(chargers);
                return true;
            } else {
                return false;
            }
        } else {
            if (suggestion.getRating() < ev.getBestAlteredWindow()) {
                suggestion.findSlotsAffected(chargers);
                ev.setBestAlteredWindow(suggestion.getRating());
                return true;
            } else {
                return false;
            }

        }
    }

    private boolean isSame (Suggestion previous, Suggestion alternative) {
        //System.out.println(previous.toString());
        //System.out.println(alternative.toString());
        if (previous.getStart() == alternative.getStart() && previous.getEnd() == alternative.getEnd() &&
                previous.getEnergy() == alternative.getEnergy())
            return true;
        return false;
    }
}
