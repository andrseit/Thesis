package station.offline;

import optimize.CPLEX;
import station.*;
import station.negotiation.Suggestion;
import station.pricing.Pricing;
import various.IntegerConstants;

import java.util.ArrayList;

/**
 * Created by Thesis on 19/1/2018.
 */
public abstract class AbstractStation {

    protected StationInfo info;
    protected Pricing pricing;

    protected Schedule schedule;
    protected int[] price;
    protected int slotsNumber;

    protected ArrayList<EVObject> evBidders;
    protected ArrayList<EVObject> waiting;

    protected ArrayList<EVObject> messageReceivers;

    protected int id_counter;
    protected boolean finished;
    protected boolean update;

    protected CPLEX cp;

    /**
     *
     * @return the schedule computed
     */
    public abstract int[][] compute ();

    /**
     * This is used when a bidder is able to charge in the optimal schedule
     * If so choose the slots that the station will offer
     * e.g. his start-end slot
     * or only the actual slots he used (start:0 - end: 10/e=2, but used only 0-2, so offer 0-2)
     * @param ev
     */
    public abstract void computeOffer (EVObject ev, int[] evRow);

    /**
     * For the evs that did not charged compute suggestions
     * add the suggestion to the ev
     */
    public abstract void findSuggestion ();

    /**
     * As parei ti lista (waiting) o allos kai as tous kanei oti thelei
     * px na tous valei oti de tha tous kanei prosfora
     * h' na vrei suggestions
     */
    public abstract void offersNotCharged (Pricing pricing);

    /**
     * Constructor
     * @param info
     * @param slotsNumber
     */
    public AbstractStation(StationInfo info, int slotsNumber) {
        this.info = info;
        this.slotsNumber = slotsNumber;
        price = new int[slotsNumber];
        for (int s = 0; s < slotsNumber/2; s++) {
            price[s] = 1;
        }
        for (int s = slotsNumber/2; s < slotsNumber; s++) {
            price[s] = 1;
        }
        //price[4] = 2;

        evBidders = new ArrayList<>();
        waiting = new ArrayList<>();
        messageReceivers = new ArrayList<>();

        id_counter = 0;
        finished = false;
        update = false;

        schedule = new Schedule(slotsNumber, info.getChargerNumber());
        cp = new CPLEX();
    }

    public void addEVBidder (EVObject ev) {
        ev.setStationId(id_counter);
        id_counter++;
        evBidders.add(ev);
        messageReceivers.add(ev);
        waiting.add(ev);
    }

    public boolean computeSchedule () {
        if (evBidders.size() > 0) {
            schedule.setFullScheduleMap(this.compute());
            schedule.printScheduleMap(price);
            this.computeOffers();
            evBidders.contains(new EVObject());
            update = true;
            return true;
        } else {
            //System.out.println("No incoming vehicles!");
            finished = true;
            return false;
        }
    }

    public void computeOffers () {
        this.offersCharged();
        if (!waiting.isEmpty())
            this.offersNotCharged(this.pricing);
        //else
            //System.out.println("There are no pending vehicles!");
    }

    public void offersCharged () {
        int[] whoCharged = cp.getWhoCharges();
        int[][] scheduleMap = cp.getScheduleMap();
        for (int e = 0; e < whoCharged.length; e++) {
            EVObject ev = evBidders.get(e);
            if (whoCharged[e] == 1 && waiting.contains(ev)) {
                this.computeOffer(ev, scheduleMap[e]);
                waiting.remove(ev);
                //messageReceivers.add(ev);
            }
        }
    }



    /**
     * When the station cannot charge an ev then create the suitable offer
     * Integer.MAX
     */
    public void addNotAvailableMessage (EVObject ev) {
        Suggestion suggestion = new Suggestion();
        suggestion.setStartEndSlots(Integer.MAX_VALUE, Integer.MAX_VALUE);
        suggestion.setEnergy(0);
        suggestion.setCost(0);
        suggestion.findSlotsAffected(schedule.getRemainingChargers());
        ev.setSuggestion(suggestion);
        ev.setFinalSuggestion();
    }

    public void sendOfferMessages () {

        if (!messageReceivers.isEmpty()) {
            System.out.print("Sending messages to: ");
            StringBuilder receivers = new StringBuilder();
            for (int e = 0; e < messageReceivers.size(); e++) {
                EVObject ev = messageReceivers.get(e);
                SuggestionMessage message = new SuggestionMessage(info, ev.getFinalSuggestion());
                message.setCost(ev.getFinalPayment());
                ev.getObjectAddress().addSuggestion(message);

                if (e == messageReceivers.size() - 1)
                    receivers.append("ev_" + ev.getId() + "\n");
                else
                    receivers.append("ev_" + ev.getId() + ", ");
            }
            System.out.println(receivers);
        } else {
            System.out.println("There are no messages to send!");
        }
        messageReceivers.clear();
        waiting.clear();
    }

    /**
     * sets flags: accepted and waiting
     * @param id
     * @param state
     */
    public void markEVBidder (int id, int state) {
        System.out.print("\t\t\tStation_" + info.getId() + ": ");
        for (int e = 0; e < evBidders.size(); e++) {
            EVObject ev = evBidders.get(e);
            if (ev.getId() == id) {
                if (state == IntegerConstants.EV_EVALUATE_ACCEPT) {
                    System.out.println("ev_" + id + " accepted offer!");
                    ev.setFinalPreferences();
                    break;
                } else if (state == IntegerConstants.EV_EVALUATE_WAIT) {
                    System.out.println("ev_" + id + " waits for new offer!");
                    waiting.add(ev);
                    messageReceivers.add(ev);
                    break;
                } else if (state == IntegerConstants.EV_EVALUATE_REJECT) {
                    System.out.println("ev_" + id + " rejected offer!");
                    evBidders.remove(e);
                    break;
                }
            }
        }
    }

    public void updateBiddersLists () {
        id_counter = evBidders.size();
        for (int e = 0; e < evBidders.size(); e++) {
            EVObject ev = evBidders.get(e);
            ev.setStationId(e);
        }

        if (evBidders.isEmpty())
            id_counter = 0;

        update = false;
        this.resetChargers();
    }

    public String printEVBidders () {
        StringBuilder str = new StringBuilder();
        for (int e = 0; e < evBidders.size(); e++) {
            EVObject ev = evBidders.get(e);
            str.append(e).append(")").append(ev.printEV());
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

    protected void resetChargers () {
        schedule.resetChargers();
    }

    protected int computePrice (Suggestion suggestion) {
        if (suggestion.getSlotsAfected() == null) {
            return 0;
        } else
            return pricing.computeCost(suggestion.getSlotsAfected());

        /*
        int cost = 0;
        if (suggestion.getSlotsAfected() == null)
            return cost;
        else {
            int[] slots_affected = suggestion.getSlotsAfected();
            for (int s = 0; s < price.length; s++) {
                if (slots_affected[s] == 1) {
                    cost += price[s];
                }
            }
            return cost;
        */
    }


    public StationInfo getInfo() {
        return info;
    }

    public boolean isFinished () {
        if (messageReceivers.isEmpty() || evBidders.isEmpty()) {
            return true;
        }
        return false;
    }

    public void printScheduleMap () {
        schedule.printScheduleMap(price);
    }
}
