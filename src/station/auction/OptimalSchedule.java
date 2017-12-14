package station.auction;

import station.EVInfo;
import optimize.CPLEX;
import station.Station;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Thesis on 17/11/2017.
 */
public class OptimalSchedule {

    private Station station;
    private CPLEX cp;


    public OptimalSchedule (Station station) {
        this.station = station;
        this.cp = station.getCP();
    }

    public void computeOptimalSchedule () {
        System.out.println("Computing optimal schedule...");

        int min_slot = getMinSlot();
        int max_slot = getMaxSlot();


        System.out.println(getMinSlot() + "-----" + getMaxSlot());
        cp.model(station.getBidderList(), station.getSlotsNumber(), station.getPrice(),
                station.getRemainingChargers(), min_slot, max_slot);

        station.setScheduleMap(cp.getScheduleMap());
        station.setInitialUtility(cp.getUtility());

        System.out.println("Schedule computed!");
    }

    private int getMinSlot () {
        PriorityQueue<EVInfo> queue = new PriorityQueue<EVInfo>(10, new Comparator<EVInfo>() {
            @Override
            public int compare(EVInfo ev1, EVInfo ev2) {
                return ev1.getMinSlot() - ev2.getMinSlot();
            }
        });

        for (EVInfo ev: station.getBidderList()) {
            queue.offer(ev);
        }
        return queue.peek().getMinSlot();
    }

    private int getMaxSlot () {
        PriorityQueue<EVInfo> queue = new PriorityQueue<EVInfo>(10, new Comparator<EVInfo>() {
            @Override
            public int compare(EVInfo ev1, EVInfo ev2) {
                return ev2.getMaxSlot() - ev1.getMaxSlot();
            }
        });

        for (EVInfo ev: station.getBidderList()) {
            queue.offer(ev);
        }

       return queue.peek().getMaxSlot();
    }

}
