package station;

import optimize.CPLEX;
import station.auction.OptimalSchedule;
import station.negotiation.Negotiations;
import station.negotiation.Suggestion;
import various.IntegerConstants;

import java.util.ArrayList;

/**
 * Created by Thesis on 10/1/2018.
 */
public class NewStation {

    private StationInfo info;

    private Schedule schedule;
    private int[] price;
    private int slots_number;

    private ArrayList<EVObject> ev_bidders;
    private ArrayList<EVObject> waiting;
    private ArrayList<EVObject> accepted_suggestions;
    private ArrayList<EVObject> message_receivers;

    private int id_counter;
    private boolean finished; // shows when there are no other vehicles to service or negotiations to be made

    private CPLEX cp;

    public NewStation(StationInfo info, int slots_number) {
        this.info = info;
        this.slots_number = slots_number;
        price = new int[slots_number];
        for (int s = 0; s < slots_number/2; s++) {
            price[s] = 1;
        }
        for (int s = slots_number/2; s < slots_number; s++) {
            price[s] = 1;
        }

        ev_bidders = new ArrayList<>();
        waiting = new ArrayList<>();
        accepted_suggestions = new ArrayList<>();
        message_receivers = new ArrayList<>();

        id_counter = 0;
        finished = false;

        schedule = new Schedule(slots_number, info.getChargerNumber());
        cp = new CPLEX();
    }


    public boolean computeSchedule () {
        if (ev_bidders.size() > 0) {
            this.compute();
            return true;
        } else {
            System.out.println("No incoming vehicles!");
            finished = true;
            System.out.println("Finished = true;");
            return false;
        }
    }

    private void compute () {
        long tStart = System.currentTimeMillis();
        System.out.println("Starting -- Computing initial optimal schedule");
        cp = new CPLEX();
        OptimalSchedule optimal = new OptimalSchedule(ev_bidders, slots_number, price, schedule.getRemainingChargers(), cp);
        schedule.setFullScheduleMap(optimal.computeOptimalSchedule());
        elapsedSeconds(tStart, "Scheduling");

        System.out.println(schedule.printFullScheduleMap(price));
    }

    public void sendInitialOffer () {
        this.findMinWindow();
        for (int e = 0; e < ev_bidders.size(); e++) {
            EVObject ev = ev_bidders.get(e);
            SuggestionMessage message = new SuggestionMessage(info, ev.getFinalSuggestion());
            ev.getObjectAddress().addSuggestion(message);
        }
    }

    public void sendNewSuggestionMessage () {
        System.out.println("Sending new offers messages!");
        for (int e = 0; e < message_receivers.size(); e++) {
            EVObject ev = message_receivers.get(e);
            System.out.println(" to... ev_" + ev.getId());
            //if (ev.hasSuggestion()) {
                SuggestionMessage message = new SuggestionMessage(info, ev.getFinalSuggestion());
                ev.getObjectAddress().addSuggestion(message);
            //}
        }
        message_receivers.clear();
    }

    /**
     * Checks if someone who asked for a better offer - because he did not fit in the
     * schedule with his initial preferences, can now charged with these preferences
     */
    public void checkWaiting () {

        if (waiting.isEmpty()) {
            finished = true;
            System.out.println("Finished = true;");
        }

        else {
            ArrayList<EVObject> removed = new ArrayList<>();
            int[][] schedule_map = cp.getScheduleMap();
            int[] who_charged = cp.getWhoCharges();
            for (EVObject ev : waiting) {
                if (who_charged[ev.getStationId()] == 1) {
                    System.out.println("ev_" + ev.getId() + "(" + ev.getStationId() + ") was found an offer!");
                    int min = ev.getStartSlot(), max = ev.getEndSlot();
                    // find the min slot
                    for (int s = 0; s < slots_number; s++) {
                        if (schedule_map[ev.getStationId()][s] == 1) {
                            min = s;
                            break;
                        }
                    }
                    for (int s = slots_number - 1; s >= min; s--) {
                        if (schedule_map[ev.getStationId()][s] == 1) {
                            max = s;
                            break;
                        }
                    }
                    Suggestion suggestion = new Suggestion();
                    suggestion.setStartEndSlots(min, max);
                    suggestion.setEnergy(ev.getEnergy());
                    suggestion.findSlotsAffected(schedule.getRemainingChargers());
                    ev.setSuggestion(suggestion);
                    ev.setFinalSuggestion();
                    System.out.println(min + " - " + max);
                    removed.add(ev);
                    message_receivers.add(ev);
                }
            }

            /*
            for (EVObject ev : removed) {
                waiting.remove(ev);
            }
            */
        }
    }


    public void findSuggestions () {

        ArrayList<EVObject> suggestees = new ArrayList<>();
        for (EVObject ev: waiting) {
            if (!message_receivers.contains(ev))
                suggestees.add(ev);
        }
        Negotiations neg = new Negotiations(suggestees, schedule.getFullScheduleMap(), schedule.getRemainingChargers(),
                price, 0);
        neg.computeSuggestions();
        if (!neg.getFilteredSuggestionList().isEmpty()) {
            for (EVObject ev : waiting) {
                System.out.println("In list ev_" + ev.getId());
                if (neg.getFilteredSuggestionList().contains(ev)) {
                    ev.setFinalSuggestion();
                }
                else {
                    Suggestion suggestion = new Suggestion();
                    suggestion.setStartEndSlots(Integer.MAX_VALUE, Integer.MAX_VALUE);
                    suggestion.setEnergy(0);
                    //suggestion.findSlotsAffected(getRemainingChargers());
                    ev.setSuggestion(suggestion);
                    ev.setFinalSuggestion();
                }
                message_receivers.add(ev);
            }
        } else if (neg.getFilteredSuggestionList().isEmpty() && waiting.isEmpty()){
            System.out.println("I am doing it right here");
            finished = true;
        }
    }

    /**
     * sets flags: accepted and wainting
     * @param id
     * @param state
     */
    public void markEVBidder (int id, int state) {
        for (EVObject ev: ev_bidders) {
            if (ev.getId() == id && (state != IntegerConstants.EV_EVALUATE_REJECT)) {
                ev.setAcceptedAndWaiting(true, state);
            } else if (ev.getId() == id && state == IntegerConstants.EV_EVALUATE_REJECT)
                ev.setAccepted(false);
        }

        if (state == IntegerConstants.EV_EVALUATE_ACCEPT)
            System.out.println("ev_" + id + " accepted offer!");
        else if (state == IntegerConstants.EV_EVALUATE_WAIT)
            System.out.println("ev_" + id + " waits for new offer!");
        else
            System.out.println("ev_" + id + " rejected offer!");
    }

    public void updateBiddersLists () {
        ArrayList<EVObject> removed = new ArrayList<>();
        int id = 0;
        for (EVObject ev: ev_bidders) {
            if (!ev.isAccepted()) {
                removed.add(ev);
                if (waiting.contains(ev))
                    waiting.remove(ev);
            }
            else {
                ev.setStationId(id);
                if (ev.isWaiting() && !waiting.contains(ev))
                    waiting.add(ev);
                else if (waiting.contains(ev) && !ev.isWaiting()) {
                    waiting.remove(ev);
                    accepted_suggestions.add(ev);
                }
                id++;
            }
        }
        for (EVObject ev: removed) {
            ev_bidders.remove(ev);
        }
        if (waiting.isEmpty())
            finished = true;
    }

    public void addEVBidder (EVObject ev) {
        ev.setStationId(id_counter);
        id_counter++;
        ev_bidders.add(ev);
    }

    public void updateNegotiationSchedule () {
        schedule.updateNegotiationChargers(accepted_suggestions);
        System.out.println(schedule.printFullScheduleMap(price));
        accepted_suggestions.clear();
    }

    public boolean isWaitingEmpty () { return waiting.isEmpty(); }

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

    public void resetChargers () {
        schedule.resetChargers();
    }

    /**
     * The station returns a suggestion to each ev after the computation of the schedule
     * If the ev has charged, it sends a suggestion with the min and max slot
     * If the ev has not charged (suggestionsForNotCharged) it sends a new suggestion
     */
    private void findMinWindow () {
        int[] who_charged = cp.getWhoCharges();
        int[][] schedule_map = schedule.getFullScheduleMap();
        for (int e = 0; e < ev_bidders.size(); e++) {
            EVObject ev = ev_bidders.get(e);
            int min = 0, max = 0;
            if (who_charged[e] == 1) {
                // find the min slot
                for (int s = 0; s < slots_number; s++) {
                    if (schedule_map[e][s] == 1) {
                        min = s;
                        break;
                    }
                }
                for (int s = slots_number-1; s >= min; s--) {
                    if (schedule_map[e][s] == 1) {
                        max = s;
                        break;
                    }
                }
                Suggestion suggestion = new Suggestion();
                suggestion.setStartEndSlots(min, max);
                suggestion.setEnergy(ev.getEnergy());
                suggestion.findSlotsAffected(schedule.getRemainingChargers());
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

    private void elapsedSeconds (long start, String process) {
        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - start;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println(process + " lasted: " + elapsedSeconds + " sec");
    }

    public StationInfo getInfo() {
        return info;
    }

    public boolean isFinished () { return finished; }
}
