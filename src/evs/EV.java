package evs;

import jade.core.Agent;

import java.util.ArrayList;

/**
 * Created by Darling on 29/7/2017.
 */
public class EV extends Agent {

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

    private int id;
    private int energy;
    private int inform_slot;
    private ArrayList<SlotsStruct> slots;
    private boolean charged = false;
    private int initial_pays; // what the ev will pay initially
    private int final_pays; // what the ev will finally pay
    private int schedule_row; // in which row of the schedule the ev is represented, because it migth change when the ordering happens
    private int min_slot, max_slot;

    // new data variables, now there is only one bid, so SlotsStruct is unnecessary
    private int bid;
    private int start;
    private int end;

    public EV() {
        slots = new ArrayList<SlotsStruct>();
    }

    public void addSlotsPreferences(int start, int end, int bid) {
        slots.add(new SlotsStruct(start, end, bid));
    }

    public void addEVPreferences (int start, int end, int bid, int energy) {
        this.start = start;
        this.end = end;
        this.bid = bid;
        this.energy = energy;
    }

    protected void setup() {

    }

    public String printEV() {
        StringBuilder str = new StringBuilder();
        str.append("EVs id: " + id + " -> energy needed: " + energy + "   informed at slot: " + inform_slot + ".\n");
        for (SlotsStruct slot : slots) {
            str.append("    > " + slot.getStart() + "-" + slot.getEnd() + " :: " + slot.getBid());
        }
        str.append("\nSchedule row: " + schedule_row + "\n");
        return str.toString();
    }

    public int getMinSlot () {
        return start;
    }

    public int getMaxSlot () {
        return end;
    }

    public int getId() {
        return id;
    }

    public int getEnergy() {
        return energy;
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
        this.energy = energy;
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

    public String toString () {
        return "energy: " + energy + " bid: " + bid + " start: " + start + " end: " + end;
    }

    public int getStartSlot () {
        return start;
    }

    public int getEndSlot () {
        return end;
    }

    public int getBid () {
        return bid;
    }
}
