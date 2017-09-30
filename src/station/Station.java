package station;

import evs.EV;
import jade.core.Agent;
import optimize.CPLEX;
import station.negotiations.NegotiationComputer;
import various.ArrayTransformations;
import various.JSONFileParser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Darling on 29/7/2017.
 */
public class Station extends Agent{

    private Schedule schedule;
    private int[] map_occupancy;
    private int[] demand; // how many evs demand to charge in each slot
    private int[] price; // the price of each slot, based on the demand
    private int num_evs;
    private int num_slots;
    private int num_chargers;
    private int min_slot, max_slot;
    private ArrayList<EV> ev_bidders;
    private ArrayList<EV> locked_bidders;
    private ArrayList<EV> not_charged; // the ev's that did not fit in the initial schedule
    private CPLEX cp;

    private int id_counter = 0;
    private ArrayTransformations transformations;

    public Station () {

        JSONFileParser parser = new JSONFileParser();
        parser.redStationData();
        num_slots = parser.getSlotsNumber();
        num_chargers = parser.getChargersNumber();
        price = new int[num_slots];
        for (int s = 0; s < num_slots/2; s++) {
            price[s] = 1;
        }
        for (int s = num_slots/2; s < num_slots; s++) {
            price[s] = 2;
        }
        schedule = new Schedule(num_slots, num_chargers);
        demand = new int[num_slots];
        schedule.printSchedule();
        cp = new CPLEX();
        ev_bidders = new ArrayList<EV>();
        locked_bidders = new ArrayList<>();
        transformations = new ArrayTransformations();
    }





    public void computeSchedule () {
        if (ev_bidders.size() > 0) {
            this.compute();
        } else {
            System.out.println("No incoming vehicles!");
        }
    }

    private void compute () {
        System.out.println("Computing schedule");
        this.getMinSlot();
        this.getMaxSlot();
        this.computeDemand();
        transformations.printOneDimensionArray("Demand", demand);

        int[] initial_payments;
        int[][] schedule_map;
        schedule_map = this.extractResults();
        schedule.saveInitialScheduleMap(schedule_map);
        schedule.printFullScheduleMap(price);
        initial_payments = this.computePayments(schedule_map);

        this.saveInitialResults(initial_payments);

        //this.vcg(initial_payments);
        //this.moveLockedBidders();
        //schedule.setRemainingChargers(num_slots);


        /*
        this.vcg(initial_payments);
        this.moveLockedBidders();
        */

        //this.negotiations();
    }


    private void vcg (int[] initial_payments) {

        System.out.println("VCG");
        not_charged = new ArrayList<EV>();

        // recompute the program to find what they will finally pay
        PriorityQueue<EV> queue = new PriorityQueue<EV>(10, new Comparator<EV>() {
            @Override
            public int compare(EV ev1, EV ev2) {
                return ev2.getPays() - ev1.getPays();
            }
        });

        int row = 0;
        for (EV ev: ev_bidders) {
            // fill the queue
            queue.offer(ev);
            ev.setScheduleRow(row);
            row++;
            // find those who did not fit
            if (!ev.getCharged())
                not_charged.add(ev);
        }

        if (queue.size() != 1) {
            while (!queue.isEmpty()) {
                EV removed = queue.poll();
                if (removed.getPays() != 0) {
                    ev_bidders.remove(removed);
                    int[] payments = this.computePayments(this.extractResults());
                    System.out.println("payments length: " + payments.length);
                    System.out.println("removed row: " + removed.getScheduleRow());
                    removed.setFinalPayment(this.removedPayment(initial_payments, payments, removed.getScheduleRow()));

                    ev_bidders.add(removed);
                }
            }

            for (EV ev : ev_bidders) {
                System.out.println(ev.getId() + " ---> " + ev.getFinalPayment());
            }


            // negotiation for those who did not fit
            for (EV ev : not_charged) {
                ev.printEV();
            }
        }
    }

    private void moveLockedBidders () {
        for (EV ev: ev_bidders) {
            locked_bidders.add(ev);
        }
        ev_bidders.clear();
    }


    private int removedPayment (int[] initial_payments, int[] payments, int row) {


        System.out.println("\n\n\n\n");
        System.out.println("Initial payments length: " + initial_payments.length);
        System.out.println("Payments length: " + payments.length);
        System.out.println("\n\n\n\n");
        int initial_valuation = 0;
        int final_valuation = 0;

        // sum of the all bids to find the social valuation
        for (int i = 0; i < initial_payments.length; i++) {
            initial_valuation += initial_payments[i];
        }

        // sum of the all bids to find the social valuation
        for (int i = 0; i < payments.length; i++) {
            final_valuation += payments[i];
        }

        System.out.println("ROW: " + row);
        // remove the bid of the removed bidder
        initial_valuation -= initial_payments[row];

        System.out.println("Initial: " + initial_valuation + ", final: " + final_valuation);
        return final_valuation - initial_valuation;
    }

    private void negotiations () {
        for (EV ev: not_charged) {
            int energy = ev.getEnergy();
            int bids_num = ev.getBidsNumber();
            int[][] slots = ev.getSlotsArray();

            NegotiationComputer neg = new NegotiationComputer();
            neg.computeOffer(energy, slots, num_chargers, schedule.getMapOccupancy());

            int start = neg.getNewStartSlot();
            int end = neg.getNewEndSlot();

            schedule.updateFullScheduleMap(ev, start, end, ev.getScheduleRow());
            schedule.printFullScheduleMap(price);
        }
    }

    private void saveInitialResults (int[] payments) {
        for (int ev = 0; ev <ev_bidders.size(); ev++) {
            ev_bidders.get(ev).setPays(payments[ev]);
        }

    }

    private int computeRemovedPayment (int[] payments) {
        int removed_pays = 0;
        for (int ev = 0; ev < ev_bidders.size(); ev++) {
            removed_pays += ev_bidders.get(ev).paysDifference(payments[ev]);
        }
        return removed_pays;
    }

    private int[][] extractResults () {

        //cp.buildModel(ev_bidders, num_chargers, num_slots, price, schedule.getFullScheduleMap(), locked_bidders.size());
        cp.model(ev_bidders, num_slots, price, schedule.getRemainingChargers(), min_slot, max_slot);

        return cp.getScheduleMap();
    }

    private int[] computePayments (int[][] schedule) {

        //System.out.println("Rows: " + schedule.length + ", Columns: " + schedule[0].length);
        int[] payments = new int[ev_bidders.size()];

        for (int ev = 0; ev < ev_bidders.size();ev++) {

            int ev_position = ev;

            int index = -1;
            int pays = 0;
            for (int s = 0; s < num_slots; s++) {
                if (schedule[ev_position][s] == 1 && index == -1) {
                    index = s;
                }
                if (schedule[ev_position][s] == 1) {
                    //pays += price[s] * ev_bidders.get(ev).getBidAtSlot(index);

                    pays += ev_bidders.get(ev).getBidAtSlot(s);
                }

                System.out.print(schedule[ev_position][s] + " ");
            }
            System.out.println("EV: " + ev_bidders.get(ev).getId() + " pays: " + pays);
            payments[ev] = pays;

        }
        return payments;
    }

    public void setEVBidders (ArrayList<EV> ev_bidders) {
        this.ev_bidders = ev_bidders;

        for (int ev = 0; ev < ev_bidders.size(); ev++) {
            ev_bidders.get(ev).setScheduleRow(ev);
        }
    }


    public void addEVBidder (EV ev) {
        id_counter++;
        ev.setID(id_counter);
        ev_bidders.add(ev);
    }

    public void addEVBidder (String ev_json) {

        JSONFileParser p = new JSONFileParser();
        EV ev = p.parseBidsString(ev_json);
        ev.setID(id_counter);
        id_counter++;
        ev_bidders.add(ev);
    }

    public String printEVBidders () {
        StringBuilder str = new StringBuilder();
        for (EV ev: ev_bidders) {
            str.append(ev.printEV());
        }
        return str.toString();
    }

    public void chooseBidders () {
        for(int ev = 0; ev < ev_bidders.size(); ev++) {
            EV current = ev_bidders.get(ev);


            // check slots if available - count how many are available in the given range
            //if(countAvailableSlots(start_slot, end_slot) >= energy)
        }
    }

    private void updateOccupancy () {

    }
    private int countAvailableSlots (int lb, int ub) {
        int available = 0;
        for(int s = 0; s < num_slots; s++) {
            if (map_occupancy[s] != num_chargers)
                available++;
        }
        return available;
    }

    private void computeDemand () {
        for (EV ev: ev_bidders) {
            for (int b = 0; b < ev.getBidsNumber(); b++) {
                int start = ev.getStartSlot(b);
                int end = ev.getEndSlot(b);
                for (int s = start; s <= end; s++) {
                    demand[s]++;
                }
            }
        }
    }

    public void printPayments () {
        for (EV ev: locked_bidders) {
            System.out.println("EV: " + ev.getId() + " pays: " + ev.getFinalPayment());
        }
    }

    private void getMinSlot () {
        PriorityQueue<EV> queue = new PriorityQueue<EV>(10, new Comparator<EV>() {
            @Override
            public int compare(EV ev1, EV ev2) {
                return ev1.getMinSlot() - ev2.getMinSlot();
            }
        });

        for (EV ev: ev_bidders) {
            queue.offer(ev);
        }
        min_slot = queue.peek().getMinSlot();
    }

    private void getMaxSlot () {
        PriorityQueue<EV> queue = new PriorityQueue<EV>(10, new Comparator<EV>() {
            @Override
            public int compare(EV ev1, EV ev2) {
                return ev2.getMaxSlot() - ev1.getMaxSlot();
            }
        });

        for (EV ev: ev_bidders) {
            queue.offer(ev);
        }

        max_slot = queue.peek().getMaxSlot();
    }



    public int getSlotsNumber() {
        return num_slots;
    }

    public int getChargersNumber() {
        return num_chargers;
    }

    public int getBiddersNumber () { return this.ev_bidders.size(); }
}
