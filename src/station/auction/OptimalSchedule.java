package station.auction;

import com.sun.org.apache.bcel.internal.generic.NEW;
import station.EVObject;
import optimize.CPLEX;
import station.NewStation;
import station.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Thesis on 17/11/2017.
 */
public class OptimalSchedule {

    private ArrayList<EVObject> bidders_list;
    private int slots_number;
    private int[] price;
    private int[] remaining_chargers;

    private CPLEX cp;


    public OptimalSchedule(ArrayList<EVObject> bidders_list, int slots_number, int[] price, int[] remaining_chargers, CPLEX cp) {
        this.bidders_list = bidders_list;
        this.slots_number = slots_number;
        this.price = price;
        this.remaining_chargers = remaining_chargers;
        this.cp = cp;
    }

    public int[][] computeOptimalSchedule () {


        System.out.println("Computing optimal schedule...");

        int min_slot = getMinSlot();
        int max_slot = getMaxSlot();


        System.out.println(getMinSlot() + "-----" + getMaxSlot());
        cp.model(bidders_list, slots_number, price,
                remaining_chargers, min_slot, max_slot);

        //station.concatScheduleMap(cp.getScheduleMap());
       //station.setInitialUtility(cp.getUtility());

        System.out.println("ScheduleOld computed!");
        return cp.getScheduleMap();
    }

    private int getMinSlot () {
        PriorityQueue<EVObject> queue = new PriorityQueue<EVObject>(10, new Comparator<EVObject>() {
            @Override
            public int compare(EVObject ev1, EVObject ev2) {
                return ev1.getMinSlot() - ev2.getMinSlot();
            }
        });

        for (EVObject ev: bidders_list) {
            queue.offer(ev);
        }
        return queue.peek().getMinSlot();
    }

    private int getMaxSlot () {
        PriorityQueue<EVObject> queue = new PriorityQueue<EVObject>(10, new Comparator<EVObject>() {
            @Override
            public int compare(EVObject ev1, EVObject ev2) {
                return ev2.getMaxSlot() - ev1.getMaxSlot();
            }
        });

        for (EVObject ev: bidders_list) {
            queue.offer(ev);
        }

       return queue.peek().getMaxSlot();
    }

}
