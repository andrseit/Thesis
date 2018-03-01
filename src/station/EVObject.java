package station;

import evs.EV;
import evs.Preferences;
import station.negotiation.Suggestion;
import various.IntegerConstants;

public class EVObject {


    // this keeps the reference to the original EV object
    // to help with the conversation
    // like a connection so that the message is delivered immediately
    private EV object_address;
    private Preferences preferences;
    private Preferences oldPreferences;
    private int bid;
    private int id;
    private int station_id;
    private int totalLoss;
    private int inform_slot;
    private int slotsNeeded; // slots the ev needs to reach the station - so that it makes the offer in the right time
    private int lastSlot; // the slot that at maximum the station can make the offer
    private int x, y; // location on map
    private boolean charged = false;
    private int final_pays; // what the ev will finally pay

    // new data variables, now there is only one bid, so SlotsStruct is unnecessary

    private Suggestion suggestion;
    private Preferences final_suggestion;
    private boolean has_suggestion;
    // so that a new suggestion can be checked if it is worth making
    private int best_less_energy; // the rating of the best less energy suggestion so far
    private int best_altered_window; // the rating of the bes altered window suggestion so far
    private int previous_best_le; // to reset if a suggestion is invalid
    private int previous_best_aw;

    public EVObject() {
        preferences = new Preferences();
        has_suggestion = false;
        suggestion = new Suggestion();
        best_less_energy = Integer.MAX_VALUE;
        best_altered_window = Integer.MAX_VALUE;
        lastSlot = 0;
    }

    public void setEVAddress(EV evAddress) {
        this.object_address = evAddress;
    }

    public void addEVPreferences(int start, int end, int bid, int energy) {
        preferences.setStart(start);
        preferences.setEnd(end);
        preferences.setEnergy(energy);
        this.bid = bid;
    }


    public String printEV() {
        //str.append(" informed at slot " + getInformSlot() + "\n");
        //str.append("EVs id: " + id + "(" + station_id + ")" + " -> energy needed: " + preferences.getEnergy() + "   informed at slot: " + inform_slot + ".\n");
        // str.append("\nSchedule row: " + schedule_row + "\n");
        //if (charged)
        //str.append(" will charge!");
        int dif = getEndSlot() - getStartSlot() + 1;
        return ("EVs id: " + id + "(" + station_id + ")" + " -> ") +
                getStartSlot() + "-" + getEndSlot() + "/" + getEnergy() + "(" + dif + ")" +
                "\n";
    }

    public int getMinSlot() {
        return preferences.getStart();
    }

    public int getMaxSlot() {
        return preferences.getEnd();
    }

    public int getId() {
        return id;
    }

    public int getEnergy() {
        return preferences.getEnergy();
    }

    public void setFinalPayment(int pays) {
        this.final_pays = pays;
    }

    public int getFinalPayment() {
        return final_pays;
    }

    public void setEnergy(int energy) {
        preferences.setEnergy(energy);
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getStartSlot() {
        return preferences.getStart();
    }

    public int getEndSlot() {
        return preferences.getEnd();
    }

    public int getBid() {
        return bid;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public EV getObjectAddress() {
        return object_address;
    }

    public Suggestion getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(Suggestion suggestion) {
        this.suggestion = suggestion;
    }

    public boolean hasSuggestion() {
        return has_suggestion;
    }

    public void setHasSuggestion(boolean has_suggestion) {
        this.has_suggestion = has_suggestion;
    }

    public String toString() {
        return ("ev_" + id + ": ") +
                preferences.getStart() + " - " + preferences.getEnd() + " / " + preferences.getEnergy();
    }

    public int getBestLessEnergy() {
        return best_less_energy;
    }

    public int getBestAlteredWindow() {
        return best_altered_window;
    }

    public void setBestLessEnergy(int best_less_energy) {
        previous_best_le = this.best_less_energy;
        this.best_less_energy = best_less_energy;
    }

    public void setBestAlteredWindow(int best_altered_window) {
        previous_best_aw = this.best_altered_window;
        this.best_altered_window = best_altered_window;
    }

    public void setBestRating(int type, int rating) {
        if (type == IntegerConstants.LESS_ENERGY_TYPE) {
            //previous_best_le = best_less_energy;
            best_less_energy = rating;
        } else {
            //previous_best_aw = best_altered_window;
            best_altered_window = rating;
        }
    }


    public Preferences getFinalSuggestion() {
        return final_suggestion;
    }

    public void setFinalSuggestion() {
        final_suggestion = new Preferences();
        final_suggestion.setStart(suggestion.getStart());
        final_suggestion.setEnd(suggestion.getEnd());
        final_suggestion.setEnergy(suggestion.getEnergy());
    }

    /**
     * If there are no chargers for the initial
     */
    public void resetBestsUpdatingChargers() {
        if (suggestion.getType() == IntegerConstants.LESS_ENERGY_TYPE) {
            best_less_energy = previous_best_le;
        } else {
            best_altered_window = previous_best_aw;
        }
    }

    public int getStationId() {
        return station_id;
    }

    public void setStationId(int station_id) {
        this.station_id = station_id;
    }

    public void setFinalPreferences() {
        oldPreferences = new Preferences();
        oldPreferences.setStart(preferences.getStart());
        oldPreferences.setEnd(preferences.getEnd());
        oldPreferences.setEnergy(preferences.getEnergy());
        preferences.setStartEndSlots(final_suggestion.getStart(), final_suggestion.getEnd());
        preferences.setEnergy(final_suggestion.getEnergy());
    }

    public int getSlotsNeeded() {
        return slotsNeeded;
    }

    public void setSlotsNeeded(int slotsNeeded) {
        this.slotsNeeded = slotsNeeded;
    }

    public int getX() {
        return x;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getY() {
        return y;
    }


    public int getLastSlot() {
        return lastSlot;
    }

    public void setLastSlot(int lastSlot) {
        this.lastSlot = lastSlot;
    }

    public int getInformSlot() {
        return inform_slot;
    }

    public void setInformSlot(int inform_slot) {
        this.inform_slot = inform_slot;
    }

    public boolean isCharged() {
        return charged;
    }

    public void setCharged(boolean charged) {
        this.charged = charged;
    }

    public void setTotalLoss () {
        int start = oldPreferences.getStart();
        int end = oldPreferences.getEnd();
        int energy = oldPreferences.getEnergy();
        int fStart = final_suggestion.getStart();
        int fEnd = final_suggestion.getEnd();
        int fEnergy = final_suggestion.getEnergy();

        if (fStart < start || fStart > end)
            totalLoss += Math.abs(start - fStart);
        if (fEnd < start || fEnd > end)
            totalLoss += Math.abs(end - fEnd);
        totalLoss += Math.abs(energy - fEnergy);
    }

    public int getPreferencesLoss () {
        return totalLoss;
    }
}
