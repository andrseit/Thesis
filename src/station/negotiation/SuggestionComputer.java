package station.negotiation;

import evs.Preferences;
import station.EVObject;
import station.pricing.Pricing;
import various.IntegerConstants;

public class SuggestionComputer {


    private int[] chargers;
    private Pricing pricing;

    public SuggestionComputer(int[] chargers, Pricing pricing) {
        this.chargers = chargers;
        this.pricing = pricing;
    }

    /**
     * First type of suggestion: Less energy in the initially given window
     */
    private Suggestion lessEnergy(Preferences initial) {

        //System.out.println("    *Computing less energy...");
        Suggestion suggestion = new Suggestion();
        suggestion.setStart(initial.getStart());
        suggestion.setEnd(initial.getEnd());

        suggestion.setEnergy(this.energyInSlots(initial.getStart(), initial.getEnd(), suggestion));
        //System.out.println("        Energy found in given slots is: " + suggestion.getEnergy());
        return suggestion;
    }

    /**
     * Finds the available energy between two time points.
     *
     * @param start: start slot
     * @param end:   end slot
     * @return
     */
    private int energyInSlots(int start, int end, Suggestion suggestion) {
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
    private Suggestion alteredWindow(Preferences initial, int lowerBound) {

        //System.out.println("    *Computing altered window...");
        Suggestion suggestion = new Suggestion();
        suggestion.setEnergy(initial.getEnergy());

        int available_energy = energyInSlots(initial.getStart(), initial.getEnd(), suggestion);
        this.searchSlots(1, initial, available_energy, suggestion, lowerBound);
        this.searchSlots(-1, initial, available_energy, suggestion, lowerBound);
        //System.out.println(new_preferences.toString());
        //System.out.println("        Slots found: " + suggestion.getStart() + "-" + suggestion.getEnd() + "/" + suggestion.getEnergy());
        return suggestion;
    }

    private void searchSlots(int step, Preferences p, int available_energy, Suggestion suggestion, int lowerBound) {

        //lowerBound = 0;
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
            if (right == chargers.length || right < lowerBound) {
                right -= step;
                break;
            }
            if (chargers[right] != 0) {
                affected_slots[right] = 1;
                available_energy++;
            }
        }
        //if (available_energy < initial.getEnergy()) left_start = initial.getStart();

        // search to the right
        while (available_energy < p.getEnergy()) {
            left -= step;
            if (left == chargers.length || left < lowerBound) {
                left += step;
                break;
            }
            if (chargers[left] != 0) {
                affected_slots[left] = 1;
                available_energy++;
            }
        }

        // clear zero slots
        while ((left < chargers.length && left >= lowerBound) && chargers[left] == 0) {
            left += step;
        }
        while ((right < chargers.length && right >= lowerBound) && chargers[right] == 0) {
            right -= step;
        }

        int[] new_slots = new int[2];

        // returns a huge value so that it will have bad rating and never be chosen
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
        int oldLength = Math.abs(suggestion.getStart() - suggestion.getEnd());
        int newLength = Math.abs(new_slots[0] - new_slots[1]);
        if (suggestion.getStart() == -1) {
            suggestion.setSlotsAffected(affected_slots);
            suggestion.setStartEndSlots(new_slots[0], new_slots[1]);
        } else {
            if (newLength < oldLength) {
                suggestion.setSlotsAffected(affected_slots);
                suggestion.setStartEndSlots(new_slots[0], new_slots[1]);
            }
        }

    }

    /**
     * This method will take as a parameter a suggestion info an it will compute
     * an alternative suggestion
     */
    public void computeAlternative(EVObject ev) {

        //System.out.println("--Computing alternative for ev: " + ev.getId());

        Suggestion less_energy_suggestion = this.lessEnergy(ev.getPreferences());
        less_energy_suggestion.setType(IntegerConstants.LESS_ENERGY_TYPE);
        Suggestion altered_window_suggestion = this.alteredWindow(ev.getPreferences(), ev.getLastSlot());
        altered_window_suggestion.setType(IntegerConstants.ALTERED_WINDOW_TYPE);
        this.evaluateSuggestion(ev.getPreferences(), less_energy_suggestion);
        this.evaluateSuggestion(ev.getPreferences(), altered_window_suggestion);
        this.compareSuggestions(ev, less_energy_suggestion, altered_window_suggestion);
        this.checkSuggestion(ev);
        if (ev.hasSuggestion()) {
            this.computeProfit(ev);
            ev.getSuggestion().setInitial(false);
        } else {
            //System.out.println("No suitable suggestion found!");
        }


        //suggestions_queue.offer(ev);
    }

    /**
     * Take as input a suggestion (new preferences) and the initial preferences of an EVObject.
     * Based on some metric check if the suggestion is legitimate.
     */
    private void evaluateSuggestion(Preferences initial, Suggestion suggestion) {

        int start = initial.getStart();
        int end = initial.getEnd();
        int sStart = suggestion.getStart();
        int sEnd = suggestion.getEnd();
        int energy_dif, window_diff = 0;

        if (sEnd < end) {
            window_diff += end - sEnd;
        } else if (sStart > start) {
            window_diff += sStart - start;
        }
        int oldRange = end - start + 1;
        int newRange = sEnd - sStart + 1;
        if (newRange > oldRange)
            window_diff += newRange - oldRange;
        energy_dif = Math.abs(suggestion.getEnergy() - initial.getEnergy());

        suggestion.setRating(window_diff + energy_dif);
        //System.out.println("Differences: " + window_diff + ", " + energy_dif);
        //System.out.println("Differences: " + start_dif + ", " + end_dif + ", " + energy_dif);
    }

    private boolean isInRange(int start, int end, int suggested) {
        return suggested >= start && suggested <= end;
    }

    private void computeProfit(EVObject ev) {

        Suggestion suggestion = ev.getSuggestion();
        suggestion.setProfit(pricing.computeCost(suggestion.getSlotsAfected(), suggestion.isInitial()));
    }

    /**
     * Sets max Integers which is translated by the evs as No Suggestion
     *
     * @param ev
     */
    private void setNoSuggestion(EVObject ev) {
        Suggestion suggestion = new Suggestion();
        suggestion.setStartEndSlots(Integer.MAX_VALUE, Integer.MAX_VALUE);
        suggestion.setEnergy(0);
        ev.setSuggestion(suggestion);
    }

    /**
     * Checks if the suggestion is legit, sets if the ev has suggestion
     * also checks if is better than previous
     *
     * @param ev
     */
    private void checkSuggestion(EVObject ev) {
        Suggestion suggestion = ev.getSuggestion();
        if (suggestion.getStart() == Integer.MAX_VALUE || suggestion.getEnd() == Integer.MAX_VALUE || suggestion.getEnergy() == 0) {
            ev.setHasSuggestion(false);
        }
    }


    /**
     * @param ev
     * @param s1
     * @param s2
     * @return
     */
    private void compareSuggestions(EVObject ev, Suggestion s1, Suggestion s2) {
        int difference = s1.getRating() - s2.getRating();
        Suggestion best, alt;
        if (difference < 0) {
            best = s1;
            alt = s2;
            //ev.setSuggestion(s1);
        } else {
            best = s2;
            alt = s1;
            //ev.setSuggestion(s2);
        }

        if (isBetter(ev, best)) {
            ev.setSuggestion(best);
            ev.setBestRating(best.getType(), best.getRating());
            ev.setHasSuggestion(true);
        } else if (isBetter(ev, alt)) {
            ev.setSuggestion(alt);
            ev.setBestRating(alt.getType(), alt.getRating());
            ev.setHasSuggestion(true);
        } else {
            ev.setHasSuggestion(false);
        }

    }

    /**
     *
     * @param ev
     * @param suggestion
     * @return
     */
    private boolean isBetter(EVObject ev, Suggestion suggestion) {

        if (suggestion.getStart() == Integer.MAX_VALUE || suggestion.getEnd() == Integer.MAX_VALUE)
            return false;

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

}
