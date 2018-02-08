package station.offline;

import optimize.AbstractCPLEX;
import station.EVObject;
import station.Schedule;
import station.StationInfo;
import station.SuggestionMessage;
import station.negotiation.Suggestion;
import station.pricing.Pricing;
import statistics.TimeStats;
import various.ArrayTransformations;
import various.IntegerConstants;
import exceptions.NoPricingException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Thesis on 19/1/2018.
 */
public abstract class AbstractStation {

    protected StationInfo info;
    protected Pricing pricing;

    protected Schedule schedule;
    protected int[] price;
    protected int[] renewables;
    protected int slotsNumber;
    protected int rejections;
    protected int negotiators; // negotiators per slot

    protected ArrayList<EVObject> evBidders;
    protected ArrayList<EVObject> waiting;

    protected ArrayList<EVObject> messageReceivers;

    protected int id_counter;
    protected boolean finished;
    protected boolean update;
    protected boolean demandComputed;
    protected boolean renewablesUpdated;
    protected int roundsCount;

    protected HashMap<String, Integer> strategyFlags;

    protected AbstractCPLEX cp;
    protected TimeStats timer;

    /**
     * @return the schedule computed
     */
    public abstract int[][] compute();

    /**
     * This is used when a bidder is able to charge in the optimal schedule
     * If so choose the slots that the station will offer
     * e.g. his start-end slot
     * or only the actual slots he used (start:0 - end: 10/e=2, but used only 0-2, so offer 0-2)
     *
     * @param ev
     */
    public abstract void computeOffer(EVObject ev, int[] evRow);

    /**
     * For the evs that did not charged compute suggestions
     * add the suggestion to the ev
     */
    public abstract void findSuggestion();

    /**
     * As parei ti lista (waiting) o allos kai as tous kanei oti thelei
     * px na tous valei oti de tha tous kanei prosfora
     * h' na vrei suggestions
     */
    public abstract void offersNotCharged(Pricing pricing);

    /**
     * This is for the programmer not to forget to set the pricing method
     */
    protected abstract void setStationPricing();

    /**
     * Computes the demand of each slot
     */
    protected abstract int[] computeDemand ();

    /**
     * Constructor
     *
     * @param info
     * @param slotsNumber
     */
    public AbstractStation(StationInfo info, int slotsNumber, int[] price, int[] renewables, HashMap<String, Integer> strategyFlags) {
        this.info = info;
        this.slotsNumber = slotsNumber;
        this.strategyFlags = strategyFlags;

        evBidders = new ArrayList<>();
        waiting = new ArrayList<>();
        messageReceivers = new ArrayList<>();

        id_counter = 0;
        negotiators = -1;
        finished = false;
        update = false;
        roundsCount = 0;

        schedule = new Schedule(slotsNumber, info.getChargerNumber());
        this.price = price;
        this.renewables = renewables;
        timer = new TimeStats();

        try {
            setPricing();
        } catch (NoPricingException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void addEVBidder(EVObject ev) {
        ev.setStationId(id_counter);
        id_counter++;
        evBidders.add(ev);
        messageReceivers.add(ev);
        waiting.add(ev);
    }

    public boolean computeSchedule() {
        if (evBidders.size() > 0) {
            if (!demandComputed) {
                schedule.setDemand(computeDemand());
                demandComputed = true;
            }
            timer.startTimer();
            int[][] scheduleMap = this.compute();
            timer.stopTimer();
            schedule.setFullScheduleMap(scheduleMap);
            schedule.printScheduleMap(price, renewables);
            this.computeOffers();
            evBidders.contains(new EVObject());
            update = true;
            roundsCount++;
            return true;
        } else {
            //System.out.println("No incoming vehicles!");
            finished = true;
            return false;
        }
    }

    public void computeOffers() {
        this.offersCharged();
        if (!waiting.isEmpty()) {
            if (roundsCount == 1)
                negotiators = waiting.size();
            this.offersNotCharged(this.pricing);
        }
        //else
        //System.out.println("There are no pending vehicles!");
    }

    public void offersCharged() {
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
    public void addNotAvailableMessage(EVObject ev) {
        Suggestion suggestion = new Suggestion();
        suggestion.setStartEndSlots(Integer.MAX_VALUE, Integer.MAX_VALUE);
        suggestion.setEnergy(0);
        suggestion.setCost(0);
        suggestion.findSlotsAffected(schedule.getRemainingChargers());
        ev.setSuggestion(suggestion);
        ev.setFinalSuggestion();
    }

    /**
     * When the station cannot charge the evs even with suggestions
     * so it informs the ev that there is not a single chance to charge it
     * @param ev
     */
    public void addRejectionMessage(EVObject ev) {
        Suggestion suggestion = new Suggestion();
        suggestion.setStartEndSlots(-1, -1);
        suggestion.setEnergy(0);
        suggestion.setCost(0);
        suggestion.findSlotsAffected(schedule.getRemainingChargers());
        ev.setSuggestion(suggestion);
        ev.setFinalSuggestion();
    }

    public void sendOfferMessages() {

        if (!messageReceivers.isEmpty()) {
            System.out.print("Sending messages to: ");
            StringBuilder receivers = new StringBuilder();
            for (int e = 0; e < messageReceivers.size(); e++) {
                EVObject ev = messageReceivers.get(e);
                SuggestionMessage message = new SuggestionMessage(info, ev.getFinalSuggestion());
                message.setCost(ev.getFinalPayment());
                ev.getObjectAddress().addSuggestion(message);

                if (e == messageReceivers.size() - 1)
                    receivers.append("ev_").append(ev.getId()).append("\n");
                else
                    receivers.append("ev_").append(ev.getId()).append(", ");
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
     *
     * @param id
     * @param state
     */
    public void markEVBidder(int id, int state) {
        System.out.print("\t\t\tStation_" + info.getId() + ": ");
        for (int e = 0; e < evBidders.size(); e++) {
            EVObject ev = evBidders.get(e);
            if (ev.getId() == id) {
                if (state == IntegerConstants.EV_EVALUATE_ACCEPT) {
                    System.out.println("ev_" + id + " accepted offer!");
                    ev.setCharged(true);
                    ev.setFinalPreferences();
                    ev.setTotalLoss();
                    break;
                } else if (state == IntegerConstants.EV_EVALUATE_WAIT) {
                    System.out.println("ev_" + id + " waits for new offer!");
                    waiting.add(ev);
                    messageReceivers.add(ev);
                    break;
                } else if (state == IntegerConstants.EV_EVALUATE_REJECT) {
                    System.out.println("ev_" + id + " rejected offer!");
                    evBidders.remove(e);
                    rejections++;
                    break;
                }
            }
        }
    }

    public void updateBiddersLists() {
        id_counter = evBidders.size();
        for (int e = 0; e < evBidders.size(); e++) {
            EVObject ev = evBidders.get(e);
            ev.setStationId(e);
        }

        if (evBidders.isEmpty())
            id_counter = 0;

        update = false;
        this.resetChargers();
        if (isFinished() && !renewablesUpdated && update)
            this.updateRenewables();
    }

    public String printEVBidders() {
        StringBuilder str = new StringBuilder();
        for (int e = 0; e < evBidders.size(); e++) {
            EVObject ev = evBidders.get(e);
            str.append(e).append(")").append(ev.printEV());
        }
        return str.toString();
    }

    public String printEVWaiting() {
        StringBuilder str = new StringBuilder();
        for (EVObject ev : waiting) {
            str.append(ev.printEV());
        }
        return str.toString();
    }

    protected void resetChargers() {
        schedule.resetChargers();
    }

    protected int computePrice(Suggestion suggestion) {
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

    public boolean isFinished() {
        return messageReceivers.isEmpty() || evBidders.isEmpty();
    }

    public void printScheduleMap() {
        schedule.printScheduleMap(price, renewables);
    }

    public int[] getPrice() {
        return price;
    }

    public void setPrice(int[] price) {
        this.price = price;
    }

    public void updateRenewables() {
        int[][] scheduleMap = schedule.getScheduleMap();
        ArrayTransformations t = new ArrayTransformations();
        int[] columnCount = t.getColumnsCount(scheduleMap);
        for (int s = 0; s < slotsNumber; s++) {
            renewables[s] -= columnCount[s];
            if (renewables[s] < 0)
                renewables[s] = 0;
        }
        renewablesUpdated = true;
    }

    private void setPricing () throws NoPricingException {
        setStationPricing();
        if (pricing == null) {
            throw new NoPricingException();
        }
    }

    public int[][] getScheduleMap () {
        return schedule.getScheduleMap();
    }

    public ArrayList<EVObject> getChargedEVs () {
        return evBidders;
    }

    public int getRejections() {
        return rejections;
    }

    public int getNegotiators () { return negotiators; }

    public int getRoundsCount () { return roundsCount - 2; }

    public void updateBeforeNextSlot () {
        roundsCount = 0;
    }
}
