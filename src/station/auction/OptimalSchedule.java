package station.auction;

import optimize.AbstractCPLEX;
import station.EVObject;

import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 * Created by Thesis on 17/11/2017.
 */
public class OptimalSchedule {

    private ArrayList<EVObject> bidders_list;
    private int slots_number;
    private int[] price;
    private int[] remaining_chargers;

    private AbstractCPLEX cp;


    public OptimalSchedule(ArrayList<EVObject> bidders_list, int slots_number, int[] price, int[] remaining_chargers, AbstractCPLEX cp) {
        this.bidders_list = bidders_list;
        this.slots_number = slots_number;
        this.price = price;
        this.remaining_chargers = remaining_chargers;
        this.cp = cp;
    }

    public int[][] computeOptimalSchedule() {

        int min_slot = getMinSlot();
        int max_slot = getMaxSlot();


        cp.model(bidders_list, slots_number, price,
                remaining_chargers, min_slot, max_slot);


        return cp.getScheduleMap();
    }

    private int getMinSlot() {
        PriorityQueue<EVObject> queue = new PriorityQueue<>(10, (ev1, ev2) -> ev1.getMinSlot() - ev2.getMinSlot());

        for (EVObject ev : bidders_list) {
            queue.offer(ev);
        }
        return queue.peek().getMinSlot();
    }

    private int getMaxSlot() {
        PriorityQueue<EVObject> queue = new PriorityQueue<>(10, (ev1, ev2) -> ev2.getMaxSlot() - ev1.getMaxSlot());

        for (EVObject ev : bidders_list) {
            queue.offer(ev);
        }

        return queue.peek().getMaxSlot();
    }

}
