package station.auction;

import station.EVObject;
import optimize.CPLEX;
import station.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Thesis on 17/11/2017.
 */
class VCG {

    private Station station;
    private CPLEX cp;
    private ArrayList<EVObject> evs;
    private int initial_utility;

    public VCG(Station station) {
        this.station = station;
        this.cp = station.getCP();
        evs = station.getBidderList();
        this.initial_utility = station.getInitialUtility();
    }

    public void vcg () {

        System.out.println("Computing payments with VCG...");

        PriorityQueue<EVObject> queue = new PriorityQueue<EVObject>(10, new Comparator<EVObject>() {
            @Override
            public int compare(EVObject ev1, EVObject ev2) {
                return ev2.getPays() - ev1.getPays();
            }
        });



        int row = 0;

        for (EVObject ev: evs) {
            queue.offer(ev);
            ev.setScheduleRow(row);
            row++;
        }

        if (queue.size() > 1) {
            while (!queue.isEmpty()) {

                EVObject removed = queue.poll();
                if (removed.getCharged()) {
                    System.out.println("Computing payment for ev" + removed.getId());
                    evs.remove(removed);
                    removed.setFinalPayment(computePayments(removed.getBid(), removed.getEnergy()));
                    evs.add(removed);
                }
            }
        } else {
            EVObject removed = queue.poll();
            if (removed.getCharged())
                removed.setFinalPayment(removed.getBid() * removed.getEnergy());
        }
        for (EVObject ev : evs) {
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
        PriorityQueue<EVObject> queue = new PriorityQueue<EVObject>(10, new Comparator<EVObject>() {
            @Override
            public int compare(EVObject ev1, EVObject ev2) {
                return ev1.getMinSlot() - ev2.getMinSlot();
            }
        });

        for (EVObject ev: evs) {
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

        for (EVObject ev: evs) {
            queue.offer(ev);
        }

        return queue.peek().getMaxSlot();
    }
}
