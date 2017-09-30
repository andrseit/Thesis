package various;

import java.util.ArrayList;

/**
 * Created by Darling on 8/8/2017.
 */
public class EVData {

    private int energy;
    private int inform_slot;
    private ArrayList<Integer[]> bids;

    public EVData () {
        bids = new ArrayList<Integer[]>();
    }

    public void addBid (int start, int end, int bid) {
        Integer[] bid_array = new Integer[3];
        bid_array[0] = start;
        bid_array[1] = end;
        bid_array[2] = bid;
        bids.add(bid_array);
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

}
