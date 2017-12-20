package station;

import evs.EV;
import evs.Preferences;
import jade.core.Agent;
import station.negotiation.Suggestion;
import various.IntegerConstants;

import java.util.ArrayList;

/**
 * Created by Darling on 29/7/2017.
 */
public class EVInfo{

    // to store the various preferences of the evs
    private class SlotsStruct {
        private int start;
        private int end;
        private int bid;

        public SlotsStruct(int start, int end, int bid) {
            this.start = start;
            this.end = end;
            this.bid = bid;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public int getBid() {
            return bid;
        }
    }

    // this keeps the reference to the original EV object
    // to help with the conversation
    // like a connection so that the message is delivered immediately
    private EV object_address;
    private Preferences preferences;
    private int id;
   //private int energy;
    private int inform_slot;
    private ArrayList<SlotsStruct> slots;
    private boolean charged = false;
    private int initial_pays; // what the ev will pay initially
    private int final_pays; // what the ev will finally pay
    private int schedule_row; // in which row of the schedule the ev is represented, because it migth change when the ordering happens

    // new data variables, now there is only one bid, so SlotsStruct is unnecessary
    private int bid;
    private Suggestion suggestion;
    private boolean has_suggestion;
    // so that a new suggestion can be checked if it is worth making
    private int best_less_energy; // the rating of the best less energy suggestion so far
    private int best_altered_window; // the rating of the bes altered window suggestion so far
    //private int start;
    //private int end;

    public EVInfo() {
        slots = new ArrayList<SlotsStruct>();
        preferences = new Preferences();
        has_suggestion = false;
        suggestion = new Suggestion();
        best_less_energy = Integer.MAX_VALUE;
        best_altered_window = Integer.MAX_VALUE;
    }

    public void setEVAddress (EV evAddress) {
        this.object_address= evAddress;
    }

    public void addEVPreferences (int start, int end, int bid, int energy) {
        preferences.setStart(start);
        preferences.setEnd(end);
        preferences.setEnergy(energy);
        this.bid = bid;
    }


    public String printEV() {
        StringBuilder str = new StringBuilder();
        str.append("EVs id: " + id + " -> energy needed: " + preferences.getEnergy() + "   informed at slot: " + inform_slot + ".\n");
        for (SlotsStruct slot : slots) {
            str.append("    > " + slot.getStart() + "-" + slot.getEnd() + " :: " + slot.getBid());
        }
        str.append("\nSchedule row: " + schedule_row + "\n");
        if (charged)
            str.append(" will charge!");
        return str.toString();
    }

    public int getMinSlot () {
        return preferences.getStart();
    }

    public int getMaxSlot () {
        return preferences.getEnd();
    }

    public int getId() {
        return id;
    }

    public int getEnergy() {
        return preferences.getEnergy();
    }

    public ArrayList<SlotsStruct> getSlots() {
        return slots;
    }

    public int getBidsNumber() {
        return slots.size();
    }

    public int getStartSlot(int index) {
        return slots.get(index).getStart();
    }

    public int getEndSlot(int index) {
        return slots.get(index).getEnd();
    }

    public int getBid(int index) {
        return slots.get(index).getBid();
    }

    public int getBidAtSlot(int index) {
        for (SlotsStruct slot : slots) {
            if (index <= slot.getEnd() && index >= slot.getStart()) {
                return slot.getBid();
            }
        }
        return 0;
    }

    public int getPays() {
        return initial_pays;
    }

    public void setPays(int pays) {
        this.initial_pays = pays;
    }

    public int paysDifference(int new_price) {
        return new_price - initial_pays;
    }

    public void setFinalPayment(int pays) {
        this.final_pays = pays;
    }

    public int getFinalPayment() {
        return final_pays;
    }

    public void setCharged(boolean v) {
        this.charged = v;
    }

    public boolean getCharged() {
        return charged;
    }

    public int[][] getSlotsArray() {
        int[][] slots_array = new int[slots.size()][2];
        for (int b = 0; b < slots.size(); b++) {
            slots_array[b][0] = slots.get(b).getStart();
            slots_array[b][1] = slots.get(b).getEnd();
        }
        return slots_array;
    }

    public void setScheduleRow(int row) {
        this.schedule_row = row;
    }

    public int getScheduleRow() {
        return schedule_row;
    }

    public void setEnergy(int energy) {
        preferences.setEnergy(energy);
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getInformTime() {
        return inform_slot;
    }

    public void setInformTime(int inform_time) {
        this.inform_slot = inform_time;
    }

    public int[][] getAllSlots () {
        int[][] slots_array = new int[slots.size()][3];
        int counter = 0;
        for (SlotsStruct s: slots) {
            slots_array[counter][0] = s.getStart();
            slots_array[counter][1] = s.getEnd();
            slots_array[counter][2] = s.getBid();
            counter++;
        }
        return slots_array;
    }

    /*
    public String toString () {
        return "energy: " + preferences.getEnergy() + " bid: " + bid + " start: " + preferences.getStart() + " end: " + preferences.getEnd();
    }
    */

    public int getStartSlot () {
        return preferences.getStart();
    }

    public int getEndSlot () {
        return preferences.getEnd();
    }

    public int getBid () {
        return bid;
    }

    public Preferences getPreferences () { return preferences; }

    public EV getObjectAddress () { return object_address; }

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

    public String toString () {
        StringBuilder str = new StringBuilder();
        str.append("ev_" + schedule_row + ": ");
        str.append(preferences.getStart() + " - " + preferences.getEnd() + " / " + preferences.getEnergy());
        str.append(" -- " + bid);
        return str.toString();
    }

    public int getBestLessEnergy() {
        return best_less_energy;
    }

    public int getBestAlteredWindow() {
        return best_altered_window;
    }

    public void setBestLessEnergy(int best_less_energy) {
        this.best_less_energy = best_less_energy;
    }

    public void setBestAlteredWindow(int best_altered_window) {
        this.best_altered_window = best_altered_window;
    }

    public void setBestRating (int type, int rating) {
        if (type == IntegerConstants.LESS_ENERGY_TYPE)
            best_less_energy = rating;
        else
            best_altered_window = rating;
    }
}