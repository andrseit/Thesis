package station.auction;

import station.EVInfo;
import optimize.CPLEX;
import station.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Thesis on 17/11/2017.
 */
public class VCG {

    private Station station;
    private CPLEX cp;
    private ArrayList<EVInfo> evs;
    private int initial_utility;

    public VCG(Station station) {
        this.station = station;
        this.cp = station.getCP();
        evs = station.getBidderList();
        this.initial_utility = station.getInitialUtility();
    }

    public void vcg () {

        System.out.println("Computing payments with VCG...");

        PriorityQueue<EVInfo> queue = new PriorityQueue<EVInfo>(10, new Comparator<EVInfo>() {
            @Override
            public int compare(EVInfo ev1, EVInfo ev2) {
                return ev2.getPays() - ev1.getPays();
            }
        });



        int row = 0;

        for (EVInfo ev: evs) {
            queue.offer(ev);
            ev.setScheduleRow(row);
            row++;
        }

        if (queue.size() > 1) {
            while (!queue.isEmpty()) {

                EVInfo removed = queue.poll();
                if (removed.getCharged()) {
                    evs.remove(removed);
                    removed.setFinalPayment(computePayments(removed.getBid(), removed.getEnergy()));
                    evs.add(removed);
                }
            }
        } else {
            EVInfo removed = queue.poll();
            if (removed.getCharged())
                removed.setFinalPayment(removed.getBid() * removed.getEnergy());
        }
        for (EVInfo ev : evs) {
            System.out.println(ev.getId() + " ---> " + ev.getFinalPayment());
        }
    }

    private int computePayments (int bid, int energy) {

        cp.model(station.getBidderList(), station.getSlotsNumber(), station.getPrice(),
                station.getRemainingChargers(), getMinSlot(), getMaxSlot());

        int new_utility = cp.getUtility();

        return new_utility - (initial_utility - bid*energy);


    }


    private int getMinSlot () {
        PriorityQueue<EVInfo> queue = new PriorityQueue<EVInfo>(10, new Comparator<EVInfo>() {
            @Override
            public int compare(EVInfo ev1, EVInfo ev2) {
                return ev1.getMinSlot() - ev2.getMinSlot();
            }
        });

        for (EVInfo ev: evs) {
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

        for (EVInfo ev: evs) {
            queue.offer(ev);
        }

        return queue.peek().getMaxSlot();
    }
}
