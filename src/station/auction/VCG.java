package station.auction;

import evs.EV;
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
    private ArrayList<EV> evs;
    private int initial_utility;

    public VCG(Station station) {
        this.station = station;
        this.cp = station.getCP();
        evs = station.getBidderList();
        this.initial_utility = station.getInitialUtility();
    }

    public void vcg () {

        System.out.println("Computing payments with VCG...");

        PriorityQueue<EV> queue = new PriorityQueue<EV>(10, new Comparator<EV>() {
            @Override
            public int compare(EV ev1, EV ev2) {
                return ev2.getPays() - ev1.getPays();
            }
        });



        int row = 0;

        for (EV ev: evs) {
            queue.offer(ev);
            ev.setScheduleRow(row);
            row++;
        }

        while (!queue.isEmpty()) {

            EV removed = queue.poll();
            if (removed.getCharged()) {
                evs.remove(removed);
                removed.setFinalPayment(computePayments(removed.getBid(), removed.getEnergy()));
                evs.add(removed);
            }
        }

        for (EV ev : evs) {
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
        PriorityQueue<EV> queue = new PriorityQueue<EV>(10, new Comparator<EV>() {
            @Override
            public int compare(EV ev1, EV ev2) {
                return ev1.getMinSlot() - ev2.getMinSlot();
            }
        });

        for (EV ev: evs) {
            queue.offer(ev);
        }
        return queue.peek().getMinSlot();
    }

    private int getMaxSlot () {
        PriorityQueue<EV> queue = new PriorityQueue<EV>(10, new Comparator<EV>() {
            @Override
            public int compare(EV ev1, EV ev2) {
                return ev2.getMaxSlot() - ev1.getMaxSlot();
            }
        });

        for (EV ev: evs) {
            queue.offer(ev);
        }

        return queue.peek().getMaxSlot();
    }
}
