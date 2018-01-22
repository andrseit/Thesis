package station;

import optimize.CPLEX;
import station.negotiation.Suggestion;
import various.IntegerConstants;

import java.util.ArrayList;

/**
 * Created by Thesis on 19/1/2018.
 */
public abstract class AbstractStation {

    protected StationInfo info;

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
    public abstract void offersNotCharged ();

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
        price[4] = 2;

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
        System.out.println("Standard Station's Add");
        ev.setStationId(id_counter);
        id_counter++;
        evBidders.add(ev);
        waiting.add(ev);
    }

    public boolean computeSchedule () {
        if (evBidders.size() > 0) {
            schedule.setFullScheduleMap(this.compute());
            System.out.println(schedule.printScheduleMap(price));
            this.computeOffers();
            update = true;
            return true;
        } else {
            System.out.println("No incoming vehicles!");
            finished = true;
            System.out.println("Finished = true;");
            return false;
        }
    }

    public void computeOffers () {
        this.offersCharged();
        if (!waiting.isEmpty())
            this.offersNotCharged();
        else
            System.out.println("There are no pending vehicles!");
    }

    public void offersCharged () {
        int[] whoCharged = cp.getWhoCharges();
        int[][] scheduleMap = cp.getScheduleMap();
        for (int e = 0; e < whoCharged.length; e++) {
            EVObject ev = evBidders.get(e);
            if (whoCharged[e] == 1 && waiting.contains(ev)) {
                this.computeOffer(ev, scheduleMap[e]);
                waiting.remove(ev);
                messageReceivers.add(ev);
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
        ev.setSuggestion(suggestion);
        ev.setFinalSuggestion();
    }

    public void sendOfferMessages () {
        for (int e = 0; e < messageReceivers.size(); e++) {
            EVObject ev = messageReceivers.get(e);
            SuggestionMessage message = new SuggestionMessage(info, ev.getFinalSuggestion());
            message.setCost(ev.getFinalPayment());
            ev.getObjectAddress().addSuggestion(message);
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
        for (EVObject ev: evBidders) {
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
        int[] who_charged = cp.getWhoCharges();
        ArrayList<EVObject> removed = new ArrayList<>();
        int id = 0;
        for (int e = 0; e < evBidders.size(); e++) {
            EVObject ev = evBidders.get(e);
            if (!ev.isAccepted()) {
                System.out.println("Station_" + info.getId() + " removes ev_" + ev.getId());
                removed.add(ev);
                if (waiting.contains(ev)) {
                    waiting.remove(ev);
                    System.out.println(", also from the waiting list...");
                }
            }
            else {
                ev.setStationId(id);
                if (ev.isWaiting() && !waiting.contains(ev))
                    waiting.add(ev);
                else if (waiting.contains(ev) && !ev.isWaiting()) {
                    waiting.remove(ev);
                }
                id++;
            }
        }
        for (EVObject ev: removed) {
            evBidders.remove(ev);
        }
        if (waiting.isEmpty())
            finished = true;
        update = false;
        this.resetChargers();
    }

    public boolean isWaitingEmpty () { return waiting.isEmpty(); }

    public String printEVBidders () {
        StringBuilder str = new StringBuilder();
        for (EVObject ev: evBidders) {
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

    protected void resetChargers () {
        schedule.resetChargers();
    }

    // NA GINEI ABSTRACT
    protected int computePrice (Suggestion suggestion) {
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
        }
    }

    public StationInfo getInfo() {
        return info;
    }

    public boolean isFinished () { return finished; }
}
