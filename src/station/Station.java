package station;

import io.ArrayFileWriter;
import optimize.CPLEX;
import station.auction.OptimalSchedule;
import station.auction.VCG;
import station.negotiation.Conversation;
import station.negotiation.Negotiations;
import io.JSONFileParser;
import various.PrintOuch;

import java.util.ArrayList;

/**
 * Created by Darling on 29/7/2017.
 */
public class Station{

    private Schedule schedule;
    private int[] map_occupancy; // remove it - it belongs to schedule
    private int[] demand; // how many evs demand to charge in each slot
    private int[] price; // the price of each slot, based on the demand
    private int num_slots;
    private int num_chargers;
    private ArrayList<EVInfo> ev_bidders;
    private ArrayList<EVInfo> locked_bidders;
    private ArrayList<EVInfo> not_charged; // the ev's that did not fit in the initial schedule
    private CPLEX cp;

    private int initial_utility;

    private int id_counter = 0;

    public Station () {

        JSONFileParser parser = new JSONFileParser();
        parser.readStationData("station.json");
        num_slots = parser.getSlotsNumber();
        num_chargers = parser.getChargersNumber();
        price = new int[num_slots];
        for (int s = 0; s < num_slots/2; s++) {
            price[s] = 1;
        }
        for (int s = num_slots/2; s < num_slots; s++) {
            price[s] = 1;
        }
        schedule = new Schedule(num_slots, num_chargers);
        demand = new int[num_slots];
        schedule.printSchedule();
        cp = new CPLEX();
        ev_bidders = new ArrayList<EVInfo>();
        locked_bidders = new ArrayList<>();
        not_charged = new ArrayList<>();
    }



    public void computeSchedule () {
        if (ev_bidders.size() > 0) {
            this.compute();
        } else {
            System.out.println("No incoming vehicles!");
        }
    }

    private void compute() {

        PrintOuch print = new PrintOuch();
        System.out.println("========================== 1) Optimal Schedule ============================");
        System.out.println("Starting -- Computing initial optimal schedule");
        OptimalSchedule optimal = new OptimalSchedule(this);
        optimal.computeOptimalSchedule();
        System.out.println("\n====================================================================\n");


        System.out.println("========================== 2) VCG Payments ============================");
        this.findNotCharged();
        VCG v = new VCG(this);
        v.vcg();
        this.moveLockedBidders();
        updateRemainingChargers();

        System.out.println(schedule.printFullScheduleMap(price));
        ArrayFileWriter w = new ArrayFileWriter();
        w.writeSchedule(schedule.getFullScheduleMap(), getRemainingChargers());
        System.out.println("\n====================================================================\n");


        if (not_charged.size() != 0) {
            System.out.println("\n============================= 3) Negotiation ============================\n");
            Negotiations neg = new Negotiations(not_charged, schedule.getFullScheduleMap(), schedule.getRemainingChargers(), price);
            //neg.computeSuggestions();
            neg.start();
            //System.out.println(schedule.printFullScheduleMap(price));
            //System.out.println("\n====================================================================\n");
            System.out.println("\n===================== 4) Conversation ===================================\n");
            Conversation conversation = new Conversation(neg.getFilteredSuggestionList());
            conversation.conversation();
            schedule.updateNegotiationChargers(conversation.getAcceptedEVs());

            System.out.println("\n===================== 5) Updating Chargers & Schedule ===================================\n");

            //ev_bidders = conversation.getAcceptedEVs();
            System.out.println(schedule.printFullScheduleMap(price));
            System.out.println("\n====================================================================\n");

            while (conversation.getPendingEvs().size() > 0 && neg.hasFinished() == false) {
                neg = new Negotiations(conversation.getPendingEvs(), schedule.getFullScheduleMap(), schedule.getRemainingChargers(), price);
                neg.start();
                if (!neg.hasFinished()) {
                    conversation = new Conversation(neg.getFilteredSuggestionList());
                    conversation.conversation();
                }
            }
        }

    }



    public void findNotCharged () {
        int[] who_charged = cp.getWhoCharges();
        for (int ev = 0; ev < who_charged.length; ev++) {
            EVInfo current = ev_bidders.get(ev);
            if (who_charged[ev] == 1) {
                current.setCharged(true);
            } else {
                not_charged.add(current);
            }
        }
    }


    private void moveLockedBidders () {
        for (EVInfo ev: ev_bidders) {
            locked_bidders.add(ev);
        }
        ev_bidders.clear();
    }



    public void addEVBidder (EVInfo ev) {
        ev.setID(id_counter);
        id_counter++;
        ev_bidders.add(ev);
    }

    public void addEVBidder (String ev_json) {

        JSONFileParser p = new JSONFileParser();
        EVInfo ev = p.parseBidsString(ev_json);
        ev.setID(id_counter);
        id_counter++;
        ev_bidders.add(ev);
    }

    public String printEVBidders () {
        StringBuilder str = new StringBuilder();
        for (EVInfo ev: ev_bidders) {
            str.append(ev.printEV());
        }
        return str.toString();
    }

    public void chooseBidders () {
        for(int ev = 0; ev < ev_bidders.size(); ev++) {
            EVInfo current = ev_bidders.get(ev);


            // check slots if available - count how many are available in the given range
            //if(countAvailableSlots(start_slot, end_slot) >= energy)
        }
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
        for (EVInfo ev: ev_bidders) {
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
        for (EVInfo ev: locked_bidders) {
            System.out.println("EVInfo: " + (ev.getId()-1) + " pays: " + ev.getFinalPayment());
        }
    }

    public void printBidders () {
        for (EVInfo ev: ev_bidders) {
            System.out.println(ev.getObjectAddress());
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

    public ArrayList<EVInfo> getBidderList () { return ev_bidders; }

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

    public void updateRemainingChargers () { schedule.setRemainingChargers(num_slots); }
}
