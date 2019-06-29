package agents.station;

import agents.evs.Preferences;
import agents.evs.communication.EVReceiver;

public class EVObject {

    // this keeps the reference to the original EV object
    // to help with the conversation
    // like a connection so that the message is delivered immediately
    private EVReceiver evReceiver;
    private SuggestionMessage message;

    private Preferences preferences;
    private int bid; // remove this - it is not needed
    private int id;
    private int station_id;
    private int totalLoss;
    private int x, y; // location on map
    private boolean charged = false;

    private boolean delayed;

    // new data variables, now there is only one bid, so SlotsStruct is unnecessary

    private Suggestion suggestion;
    private boolean has_suggestion;

    public EVObject(Preferences preferences) {
        this.preferences = preferences;
        has_suggestion = false;
        suggestion = new Suggestion();
    }


    /** New Code 11.2.2019 **/

    public boolean acceptedAlternative () {
        //System.out.println("Initial: " + preferences.toString());
        //System.out.println("Final: " + suggestion.getPreferences().toString());

        boolean isSuggestion = (suggestion.getPreferences().getStart() > -1 && suggestion.getPreferences().getEnd() > -1 && suggestion.getPreferences().getEnergy() > -1);
        boolean isAlternative = (suggestion.getPreferences().getStart() < preferences.getStart() || suggestion.getPreferences().getEnd() > preferences.getEnd()
                || suggestion.getPreferences().getEnergy() < preferences.getEnergy());
        return isSuggestion && isAlternative;
    }

    // prepare the message to be sent to the EV
    public void setSuggestionMessage (SuggestionMessage message) {
        this.message = message;
    }

    public SuggestionMessage getSuggestionMessage () {
        return message;
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

    public int getStationId() {
        return station_id;
    }

    public void setStationId(int station_id) {
        this.station_id = station_id;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isCharged() {
        return charged;
    }

    public void setCharged(boolean charged) {
        this.charged = charged;
    }

    public void setTotalLoss () {
        Preferences finalPreferences = suggestion.getPreferences();
        int start = preferences.getStart();
        int end = preferences.getEnd();
        int energy = preferences.getEnergy();

        int fStart = finalPreferences.getStart();
        int fEnd = finalPreferences.getEnd();
        int fEnergy = finalPreferences.getEnergy();

        if (fStart < start || fStart > end)
            totalLoss += Math.abs(start - fStart);
        if (fEnd < start || fEnd > end)
            totalLoss += Math.abs(end - fEnd);
        totalLoss += Math.abs(energy - fEnergy);
    }

    public int getPreferencesLoss () {
        return totalLoss;
    }

    public EVReceiver getEvReceiver() {
        return evReceiver;
    }

    public void setEvReceiver(EVReceiver evReceiver) {
        this.evReceiver = evReceiver;
    }

    public boolean isDelayed() {
        return delayed;
    }

    public void setDelayed(boolean delayed) {
        this.delayed = delayed;
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
}
