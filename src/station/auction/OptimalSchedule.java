package station.auction;

import new_classes.Optimizer;
import optimize.AbstractCPLEX;
import station.EVObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class OptimalSchedule implements Optimizer {


    private AbstractCPLEX cp;


    public OptimalSchedule(AbstractCPLEX cp) {
        this.cp = cp;
    }

    public int[][] optimize(int slotsNumber, int currentSlot, ArrayList<EVObject> evs, int[] remainingChargers, int[] price) {

        int min_slot = getMinSlot(evs);
        int max_slot = getMaxSlot(evs);


        cp.model(evs, slotsNumber, price,
                remainingChargers, min_slot, max_slot);


        return cp.getScheduleMap();
    }

    private int getMinSlot(ArrayList<EVObject> evs) {
        PriorityQueue<EVObject> queue = new PriorityQueue<>(10, Comparator.comparingInt(EVObject::getMinSlot));

        for (EVObject ev : evs)
            queue.offer(ev);
        return queue.peek().getMinSlot();
    }

    private int getMaxSlot(ArrayList<EVObject> evs) {
        PriorityQueue<EVObject> queue = new PriorityQueue<>(10, (ev1, ev2) -> ev2.getMaxSlot() - ev1.getMaxSlot());

        for (EVObject ev : evs) {
            queue.offer(ev);
        }

        return queue.peek().getMaxSlot();
    }

}
