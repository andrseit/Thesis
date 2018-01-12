package various;

import evs.strategy.Strategy;

import java.util.ArrayList;

/**
 * Created by Darling on 8/8/2017.
 */
public class EVData {

    private int energy;
    private int inform_slot;
    private ArrayList<Integer[]> bids;

    // new data variables - now there is only one bid for all the energy demanded
    // so there are only one start-end slots
    private int bid;
    private int start;
    private int end;

    private int s_start;
    private int s_end;
    private int s_energy;
    private int s_prob;
    private int s_rounds;

    private String json_sting;

    public EVData (int energy, int bid, int start, int end, int inform_slot) {

        bids = new ArrayList<Integer[]>();

        this.energy = energy;
        this.bid = bid;
        this.start = start;
        this.end = end;
        this.inform_slot = inform_slot;

    }

    public void addBid (int start, int end, int bid) {
        Integer[] bid_array = new Integer[3];
        bid_array[0] = start;
        bid_array[1] = end;
        bid_array[2] = bid;
        bids.add(bid_array);
    }

    public void setStrategy (int start, int end, int energy, int probability, int rounds) {
        s_start = start;
        s_end = end;
        s_energy = energy;
        s_prob = probability;
        s_rounds = rounds;
    }

    public Strategy getStrategy () {
        return new Strategy(s_energy, s_start, s_end, s_prob, s_rounds);
    }

    public void setJSONString (String str) {
        this.json_sting = str;
    }
    public void setEnergy (int energy) {
        this.energy = energy;
    }

    public int getStartAtIndex (int index) {
        return bids.get(index)[0];
    }

    public int getEndAtIndex (int index) {
        return bids.get(index)[1];
    }

    public int getBidAtIndex (int index) {
        return bids.get(index)[2];
    }

    public int getEnergy () {
        return energy;
    }

    public ArrayList<Integer[]> getBids () { return bids; }

    public void setInformSlot (int inform_slot) { this.inform_slot = inform_slot; }

    public int getInformSlot () { return  inform_slot; }

    public int getBid() { return bid; }

    public void setBid(int bid) { this.bid = bid; }

    public int getStart() { return start; }

    public void setStart(int start) { this.start = start; }

    public int getEnd() { return end; }

    public void setEnd(int end) { this.end = end; }

    @Override
    public String toString () {
        return json_sting;
    }

}
