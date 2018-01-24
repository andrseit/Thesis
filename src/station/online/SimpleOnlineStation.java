package station.online;

import optimize.CPLEX;
import station.EVObject;
import station.StationInfo;
import station.auction.OptimalSchedule;
import station.negotiation.Negotiations;
import station.negotiation.Suggestion;
import station.online.AbstractOnlineStation;
import station.pricing.Pricing;
import station.pricing.SimplePricing;

/**
 * Created by Thesis on 22/1/2018.
 */
public class SimpleOnlineStation extends AbstractOnlineStation {

    /**
     * Constructor
     *
     * @param info
     * @param slotsNumber
     */
    public SimpleOnlineStation(StationInfo info, int slotsNumber) {
        super(info, slotsNumber);
        pricing = new SimplePricing(price);
    }

    @Override
    public int[][] compute() {
        System.out.println("Starting -- Computing initial optimal schedule");
        cp = new CPLEX();
        OptimalSchedule optimal = new OptimalSchedule(evBidders, slotsNumber, price, schedule.getRemainingChargers(), cp);
        return optimal.computeOptimalSchedule();
    }

    @Override
    public void computeOffer(EVObject ev, int[] evRow) {
        System.out.println("Computing offer for ev_" + ev.getId());
        Suggestion suggestion = new Suggestion();
        int start = ev.getStartSlot();
        int end = ev.getEndSlot();
        int energy = ev.getEnergy();
        suggestion.setStartEndSlots(start, end);
        suggestion.setEnergy(energy);
        suggestion.findSlotsAffected(schedule.getScheduleMap(), ev.getStationId());
        ev.setFinalPayment(this.computePrice(suggestion));
        ev.setSuggestion(suggestion);
        ev.setFinalSuggestion();
    }

    @Override
    public void findSuggestion() {

    }

    @Override
    public void offersNotCharged(Pricing pricing) {
        if (rounds != 0){
            Negotiations neg = new Negotiations(waiting, schedule.getRemainingChargers(),
                    pricing);
            neg.computeSuggestions();
            if (!neg.getFilteredSuggestionList().isEmpty()) {
                for (EVObject ev : waiting) {
                    if (neg.getFilteredSuggestionList().contains(ev) && ev.hasSuggestion()) {
                        ev.setFinalPayment(this.computePrice(ev.getSuggestion()));
                        ev.setFinalSuggestion();
                    } else {
                        Suggestion suggestion = new Suggestion();
                        suggestion.setStartEndSlots(Integer.MAX_VALUE, Integer.MAX_VALUE);
                        suggestion.setEnergy(0);
                        //suggestion.findSlotsAffected(schedule.getRemainingChargers());
                        ev.setSuggestion(suggestion);
                        ev.setFinalSuggestion();
                    }
                    //messageReceivers.add(ev);
                }
            } else if (neg.getFilteredSuggestionList().isEmpty() && waiting.isEmpty()) {
                System.out.println("I am doing it right here");
                finished = true;
            }
        } else {
            for (EVObject ev: waiting) {
                this.addNotAvailableMessage(ev);
                //messageReceivers.add(ev);
            }
        }
        rounds++;
    }

    @Override
    public boolean hasOffers(int slot) {
        //System.out.println("Min slot from check: " + minSlot + " result: " + (minSlot == slot));
        return minSlot == slot;
    }

    @Override
    public int setLastSlot(EVObject ev) {
        int distance = ev.getSlotsNeeded();
        int start = ev.getStartSlot();
        int lastSlot = start - distance;
        ev.setLastSlot(lastSlot);
        System.out.println("Last slot: " + lastSlot);
        return lastSlot;
    }
}