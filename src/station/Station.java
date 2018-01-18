package station;

import optimize.CPLEX;
import station.auction.OptimalSchedule;
import io.JSONFileParser;
import station.negotiation.Negotiations;
import station.negotiation.Suggestion;
import various.IntegerConstants;

import java.util.ArrayList;

/**
 * Created by Darling on 29/7/2017.
 */
public class Station{

    private StationInfo info;

    private Schedule schedule;
    private int[] map_occupancy; // remove it - it belongs to schedule
    private int[] demand; // how many evs demand to charge in each slot
    private int[] price; // the price of each slot, based on the demand
    private int num_slots;
    private int num_chargers;
    private ArrayList<EVObject> ev_bidders;
    private ArrayList<EVObject> locked_bidders;
    private ArrayList<EVObject> not_charged; // the evs that did not fit in the initial schedule
    private ArrayList<EVObject> waiting;// the evs that wait for a new suggestion
    private CPLEX cp;

    private int initial_utility;

    private int id_counter = 0;

    public Station (StationInfo info, int num_slots) {

        this.num_slots = num_slots;
        this.info = info;

        price = new int[num_slots];
        for (int s = 0; s < num_slots/2; s++) {
            price[s] = 1;
        }
        for (int s = num_slots/2; s < num_slots; s++) {
            price[s] = 1;
        }
        schedule = new Schedule(num_slots, info.getChargerNumber());
        demand = new int[num_slots];
        cp = new CPLEX();
        ev_bidders = new ArrayList<EVObject>();
        locked_bidders = new ArrayList<>();
        not_charged = new ArrayList<>();
        waiting = new ArrayList<>();
    }



    public void computeSchedule () {
        if (ev_bidders.size() > 0) {
            this.compute();
        } else {
            System.out.println("No incoming vehicles!");
        }
    }

    private void elapsedSeconds (long start, String process) {
        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - start;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println(process + " lasted: " + elapsedSeconds + " sec");
    }


    private void compute () {
        long tStart = System.currentTimeMillis();
        System.out.println("Starting -- Computing initial optimal schedule");
        cp = new CPLEX();
        OptimalSchedule optimal = new OptimalSchedule(ev_bidders, num_slots, price, getRemainingChargers(), cp);
        optimal.computeOptimalSchedule();
        //this.setScheduleMap(cp.getScheduleMap());
        //this.showIDs();
        elapsedSeconds(tStart, "Scheduling");

        this.findMinWindow();
        System.out.println(schedule.printScheduleMap(price));

    }

    /**
     * The station returns a suggestion to each ev after the computation of the schedule
     * If the ev has charged, it sends a suggestion with the min and max slot
     * If the ev has not charged (suggestionsForNotCharged) it sends a new suggestion
     */
    private void findMinWindow () {
        int[] who_charged = cp.getWhoCharges();
        int[][] schedule_map = schedule.getScheduleMap();
        for (int e = 0; e < ev_bidders.size(); e++) {
            EVObject ev = ev_bidders.get(e);
            int min = 0, max = 0;
            if (who_charged[e] == 1) {
                // find the min slot
                for (int s = 0; s < num_slots; s++) {
                    if (schedule_map[e][s] == 1) {
                        min = s;
                        break;
                    }
                }
                for (int s = num_slots-1; s >= min; s--) {
                    if (schedule_map[e][s] == 1) {
                        max = s;
                        break;
                    }
                }
                Suggestion suggestion = new Suggestion();
                suggestion.setStartEndSlots(min, max);
                suggestion.setEnergy(ev.getEnergy());
                suggestion.findSlotsAffected(getRemainingChargers());
                ev.setSuggestion(suggestion);
                ev.setFinalSuggestion();
            } else {
                Suggestion suggestion = new Suggestion();
                suggestion.setStartEndSlots(Integer.MAX_VALUE, Integer.MAX_VALUE);
                suggestion.setEnergy(0);
                //suggestion.findSlotsAffected(getRemainingChargers());
                ev.setSuggestion(suggestion);
                ev.setFinalSuggestion();
            }

        }
    }

    public void findSuggestions () {

        Negotiations neg = new Negotiations(not_charged, schedule.getScheduleMap(), getRemainingChargers(),
                price);
        neg.computeSuggestions();
        for (EVObject ev: not_charged) {
            if (neg.getFilteredSuggestionList().contains(ev))
                ev.setFinalSuggestion();
        }
    }

    public void sendSuggestionMessage () {
        int[] who_charged = cp.getWhoCharges();
        for (int e = 0; e < ev_bidders.size(); e++) {
            //if (who_charged[e] == 1) {
            EVObject ev = ev_bidders.get(e);
            SuggestionMessage message = new SuggestionMessage(info, ev.getFinalSuggestion());
            ev.getObjectAddress().addSuggestion(message);
            //}
        }
    }

    public void sendNewSuggestionMessage () {

        for (int e = 0; e < waiting.size(); e++) {
            EVObject ev = waiting.get(e);
            if (ev.hasSuggestion()) {
                SuggestionMessage message = new SuggestionMessage(info, ev.getFinalSuggestion());
                ev.getObjectAddress().addSuggestion(message);
            }
        }
    }

    public void resetChargers () {
        schedule.resetChargers();
    }

    private void compute2() {

        //PrintOuch print = new PrintOuch();
        //System.out.println("========================== 1) Optimal ScheduleOld ============================");
        long tStart = System.currentTimeMillis();
        System.out.println("Starting -- Computing initial optimal schedule");
        OptimalSchedule optimal = new OptimalSchedule(ev_bidders, num_slots, price, getRemainingChargers(), cp);
        optimal.computeOptimalSchedule();
        //this.showIDs();
        System.out.println(schedule.printScheduleMap(price));
        elapsedSeconds(tStart, "Scheduling");


        //System.out.println("\n====================================================================\n");
        /*
        System.out.println("========================== 2) VCG Payments ============================");
        tStart = System.currentTimeMillis();
        this.findNotCharged();
        VCG v = new VCG(this);
        v.vcg();
        this.moveLockedBidders();
        updateRemainingChargers();
        elapsedSeconds(tStart, "VCG");
        System.out.println(schedule.printScheduleMap(price));
        ArrayFileWriter w = new ArrayFileWriter();
        w.writeSchedule(schedule.getScheduleMap(), getRemainingChargers());
        System.out.println("\n====================================================================\n");

        Negotiations neg = new Negotiations(not_charged, schedule.getScheduleMap(), schedule.getRemainingChargers(), price,
                IntegerConstants.SUGGESTION_COMPUTER_INITIAL);
        neg.start();

        tStart = System.currentTimeMillis();
        if (not_charged.size() != 0) {
            System.out.println("\n============================= 3) Negotiation ============================\n");
            Negotiations neg = new Negotiations(not_charged, schedule.getScheduleMap(), schedule.getRemainingChargers(), price,
                    IntegerConstants.SUGGESTION_COMPUTER_INITIAL);
            //neg.computeSuggestions();
            neg.start();
            //System.out.println(schedule.printScheduleMap(price));
            //System.out.println("\n====================================================================\n");
            System.out.println("\n===================== 4) Conversation ===================================\n");
            Conversation conversation = new Conversation(neg.getFilteredSuggestionList());
            conversation.conversation();
            schedule.updateNegotiationChargers(conversation.getAcceptedEVs());

            System.out.println("\n===================== 5) Updating Chargers & ScheduleOld ===================================\n");

            //ev_bidders = conversation.getAcceptedEVs();
            System.out.println(schedule.printScheduleMap(price));
            System.out.println("\n====================================================================\n");

            while (conversation.getPendingEvs().size() > 0 && neg.hasFinished() == false) {
                neg = new Negotiations(conversation.getPendingEvs(), schedule.getScheduleMap(), schedule.getRemainingChargers(), price,
                        IntegerConstants.SUGGESTION_COMPUTER_CONVERSATION);
                neg.start();
                if (!neg.hasFinished()) {
                    conversation = new Conversation(neg.getFilteredSuggestionList());
                    conversation.conversation();
                }
            }
            System.out.println("Negotiation is over!");
        }
        elapsedSeconds(tStart, "Neogotiation");
        System.out.println("================== 6) Statistics ===========================");
        Statistics stats = new Statistics();
        stats.occupancyPercentage(schedule.getScheduleMap(), num_chargers);
        stats.evsChargedPercentage(schedule.getScheduleMap());
        */

    }

    private void showIDs () {
        for (EVObject ev: ev_bidders) {
            System.out.println("ev_" + ev.getStationId() + " ---> " + " ev_" + ev.getId());
        }
    }

    public void findNotCharged () {
        int[] who_charged = cp.getWhoCharges();
        for (int ev = 0; ev < who_charged.length; ev++) {
            EVObject current = ev_bidders.get(ev);
            if (who_charged[ev] == 1) {
                current.setCharged(true);
            } else {
                not_charged.add(current);
            }
        }
    }


    /**
     * Checks if someone who asked for a better offer - because he did not fit in the
     * schedule with his initial preferences, can now charged with these preferences
     */
    public void checkWaiting () {
        int[][] schedule_map = cp.getScheduleMap();
        int[] who_charged = cp.getWhoCharges();
        for (EVObject ev: waiting) {

            if (who_charged[ev.getStationId()] == 1) {
                System.out.println("ev_" + ev.getId() + "(" + ev.getStationId()+") was found an offer!");
                int min = ev.getStartSlot(), max = ev.getEndSlot();
                // find the min slot
                for (int s = 0; s < num_slots; s++) {
                    if (schedule_map[ev.getStationId()][s] == 1) {
                        min = s;
                        break;
                    }
                }
                for (int s = num_slots-1; s >= min; s--) {
                    if (schedule_map[ev.getStationId()][s] == 1) {
                        max = s;
                        break;
                    }
                }
                Suggestion suggestion = new Suggestion();
                suggestion.setStartEndSlots(min, max);
                suggestion.setEnergy(ev.getEnergy());
                suggestion.findSlotsAffected(getRemainingChargers());
                ev.setSuggestion(suggestion);
                ev.setFinalSuggestion();
                System.out.println(min + " - " + max);
            }
        }
    }

    public void moveLockedBidders () {
        for (EVObject ev: ev_bidders) {
            if (!waiting.contains(ev))
                locked_bidders.add(ev);
        }
        ev_bidders.clear();
    }

    public void addEVBidder (EVObject ev) {
        ev.setStationId(id_counter);
        id_counter++;
        ev_bidders.add(ev);
    }

    public void markEVBidder (int id, int state) {
        for (EVObject ev: ev_bidders) {
            if (ev.getId() == id && (state != IntegerConstants.EV_EVALUATE_REJECT)) {
                ev.setAcceptedAndWaiting(true, state);
            }
            if (waiting.contains(ev) && (state == IntegerConstants.EV_EVALUATE_ACCEPT || state == IntegerConstants.EV_EVALUATE_REJECT)) {
                ev.setWaiting(false);
                waiting.remove(ev);
            }
        }
        if (state == IntegerConstants.EV_EVALUATE_ACCEPT)
            System.out.println("ev_" + id + " accepted offer!");
        else if (state == IntegerConstants.EV_EVALUATE_WAIT)
            System.out.println("ev_" + id + " waits for new offer!");
        else
            System.out.println("ev_" + id + " rejected offer!");
    }

    public void setScheduleMap (int[][] map) {
        schedule.setFullScheduleMap(map);
    }

    public void updateEVBiddersList () {
        ArrayList<EVObject> remove = new ArrayList<>();
        int id = 0;
        for (EVObject ev: ev_bidders) {
            if (!ev.isAccepted())
                remove.add(ev);
            else {
                ev.setStationId(id);
                if (ev.isWaiting())
                    waiting.add(ev);
                id++;
            }
        }
        for (EVObject ev: remove) {
            ev_bidders.remove(ev);
        }
    }



    public void addEVBidder (String ev_json) {

        JSONFileParser p = new JSONFileParser();
        EVObject ev = p.parseBidsString(ev_json);
        ev.setID(id_counter);
        id_counter++;
        ev_bidders.add(ev);
    }

    public String printEVBidders () {
        StringBuilder str = new StringBuilder();
        for (EVObject ev: ev_bidders) {
            str.append(ev.printEV());
        }
        return str.toString();
    }

    public String printEVWaiting () {
        StringBuilder str = new StringBuilder();
        for (EVObject ev: waiting) {
            str.append(ev.printEV());
        }
        return str.toString();
    }

    public void printNotChargedBidders () {
        for (EVObject ev: not_charged) {
            System.out.println(ev.toString());
        }
    }
    public void chooseBidders () {
        for(int ev = 0; ev < ev_bidders.size(); ev++) {
            EVObject current = ev_bidders.get(ev);


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

    public void printPayments () {
        for (EVObject ev: locked_bidders) {
            System.out.println("EVObject: " + (ev.getId()-1) + " pays: " + ev.getFinalPayment());
        }
    }

    public void printBidders () {
        for (EVObject ev: ev_bidders) {
            System.out.println(ev.getObjectAddress());
        }
    }

    public String printSchedule () {
        return schedule.printScheduleMap(price);
    }

    public int getSlotsNumber() {
        return num_slots;
    }

    public int getChargersNumber() {
        return num_chargers;
    }

    public int getBiddersNumber () { return this.ev_bidders.size(); }

    public ArrayList<EVObject> getBidderList () { return ev_bidders; }

    public CPLEX getCP () { return cp; }

    public int[] getPrice () { return price; }

    public int[] getRemainingChargers () {return schedule.getRemainingChargers(); }

    public void setInitialUtility (int utility) {
        this.initial_utility = utility;
    }

    public int getInitialUtility () {
        return initial_utility;
    }

    public StationInfo getInfo () { return info; }

    public boolean isWaitingEmpty () { return waiting.isEmpty(); }

}
