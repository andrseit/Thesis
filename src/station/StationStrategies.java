package station;

import station.negotiation.Suggestion;

public class StationStrategies {

    /**
     * Adds a suggestion based on the initial preferences
     * @param ev
     */
    public Suggestion sameWindow (EVObject ev) {
        Suggestion suggestion = new Suggestion();
        int start = ev.getStartSlot();
        int end = ev.getEndSlot();
        int energy = ev.getEnergy();
        suggestion.setStartEndSlots(start, end);
        suggestion.setEnergy(energy);
        return suggestion;
    }

    /**
     * Finds the actual window that the ev will charge
     * @param ev
     * @param evRow
     */
    public Suggestion minWindow (EVObject ev, int[] evRow) {
        int min = 0, max = 0;
        for (int s = 0; s < evRow.length; s++) {
            if (evRow[s] == 1) {
                min = s;
                break;
            }
        }
        for (int s = evRow.length - 1; s >= min; s--) {
            if (evRow[s] == 1) {
                max = s;
                break;
            }
        }
        Suggestion suggestion = new Suggestion();
        suggestion.setStartEndSlots(min, max);
        suggestion.setEnergy(ev.getEnergy());
        return suggestion;
    }
}
