package station;

import evs.EV;
import jade.core.Agent;
import optimize.CPLEX;
import station.auction.OptimalSchedule;
import station.auction.VCG;
import station.negotiations.NegotiationComputer;
import various.ArrayTransformations;
import various.JSONFileParser;

import java.util.ArrayList;

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
    private ArrayList<EV> ev_bidders;
    private ArrayList<EV> locked_bidders;
    private ArrayList<EV> not_charged; // the ev's that did not fit in the initial schedule
    private CPLEX cp;

    private int initial_utility;
    private int[] who_charges;

    private int id_counter = 0;
    private ArrayTransformations transformations;

    public Station () {

        JSONFileParser parser = new JSONFileParser();
        parser.readStationData();
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

    private void compute() {
        OptimalSchedule optimal = new OptimalSchedule(this);
        optimal.computeOptimalSchedule();
        System.out.println(schedule.printFullScheduleMap(price));
        VCG v = new VCG(this);
        v.vcg();
        this.findNotCharged();
        this.moveLockedBidders();
    }


    public void findNotCharged () {
        for (EV ev: ev_bidders) {
            if (!ev.getCharged())
                not_charged.add(ev);
        }
    }


    private void moveLockedBidders () {
        for (EV ev: ev_bidders) {
            locked_bidders.add(ev);
        }
        ev_bidders.clear();
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



    public String printSchedule () {
        return schedule.printFullScheduleMap(price);
    }

    public int getSlotsNumber() {
        return num_slots;
    }

    public int getChargersNumber() {
        return num_chargers;
    }

    public int getBiddersNumber () { return this.ev_bidders.size(); }

    public ArrayList<EV> getBidderList () { return ev_bidders; }

    public CPLEX getCP () { return cp; }

    public int[] getPrice () { return price; }

    public int[] getRemainingChargers () {return schedule.getRemainingChargers(); }

    public void setScheduleMap (int[][] schedule_map) {
        schedule.saveInitialScheduleMap(schedule_map);
    }

    public void setInitialUtility (int utility) {
        this.initial_utility = utility;
    }

    public int getInitialUtility () {
        return initial_utility;
    }
}
