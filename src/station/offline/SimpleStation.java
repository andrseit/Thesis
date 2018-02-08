package station.offline;

import optimize.ProfitCPLEX;
import optimize.ServiceCPLEX;
import station.EVObject;
import station.StationInfo;
import station.StationStrategies;
import station.auction.OptimalSchedule;
import station.negotiation.Negotiations;
import station.negotiation.Suggestion;
import station.pricing.Pricing;
import station.pricing.SimplePricing;
import various.IntegerConstants;

import java.util.HashMap;

/**
 * Created by Thesis on 19/1/2018.
 */
public class SimpleStation extends AbstractStation {

    private int rounds;

    /**
     * Constructor
     *
     * @param info
     * @param slotsNumber
     */
    public SimpleStation(StationInfo info, int slotsNumber, int[] price, int[] renewables, HashMap<String, Integer> strategyFlags) {
        super(info, slotsNumber, price, renewables, strategyFlags);
        this.info.setStation(this);
        rounds = 0;
        //pricing = new SimplePricing(price);
    }

    @Override
    public int[][] compute() {
        if (strategyFlags.get("cplex").equals(IntegerConstants.CPLEX_PROFIT))
            cp = new ProfitCPLEX();
        else if (strategyFlags.get("cplex").equals(IntegerConstants.CPLEX_SERVICE))
            cp = new ServiceCPLEX();
        OptimalSchedule optimal = new OptimalSchedule(evBidders, slotsNumber, price, schedule.getRemainingChargers(), cp);
        return optimal.computeOptimalSchedule();
    }

    @Override
    public void computeOffer(EVObject ev, int[] evRow) {
//        Suggestion suggestion = new Suggestion();
//        int start = ev.getStartSlot();
//        int end = ev.getEndSlot();
//        int energy = ev.getEnergy();
//        suggestion.setStartEndSlots(start, end);
//        suggestion.setEnergy(energy);

        Suggestion suggestion = null;
        StationStrategies strategies = new StationStrategies();
        if (strategyFlags.get("window").equals(IntegerConstants.WINDOW_STANDARD)) {
           suggestion = strategies.sameWindow(ev);
        } else if (strategyFlags.get("window").equals(IntegerConstants.WINDOW_MIN)) {
           suggestion = strategies.minWindow(ev, evRow);
        }

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
        if (rounds == 0 && strategyFlags.get("suggestion").equals(IntegerConstants.SUGGESTION_SECOND_ROUND)) {
            for (EVObject ev : waiting) {
                this.addNotAvailableMessage(ev);
                //messageReceivers.add(ev);
            }
        } else {
            Negotiations neg = new Negotiations(waiting, schedule.getRemainingChargers(),
                    pricing);
            boolean hasSuggestions = neg.computeSuggestions();
            if (hasSuggestions) {
                for (EVObject ev : waiting) {
                    if (neg.getFilteredSuggestionList().contains(ev) && ev.hasSuggestion()) {
                        ev.setFinalPayment(this.computePrice(ev.getSuggestion()));
                        ev.setFinalSuggestion();
                    } else {
                        Suggestion suggestion = new Suggestion();
                        suggestion.setStartEndSlots(Integer.MAX_VALUE, Integer.MAX_VALUE);
                        suggestion.setEnergy(0);
                        suggestion.findSlotsAffected(schedule.getRemainingChargers());
                        ev.setSuggestion(suggestion);
                        ev.setFinalSuggestion();
                    }
                    //messageReceivers.add(ev);
                }
            } else if (!hasSuggestions && waiting.isEmpty()) {
                finished = true;
            } else if (!hasSuggestions && !waiting.isEmpty() && !(roundsCount == 0)) {
                for (EVObject ev : waiting) {
                    addRejectionMessage(ev);
                }
            }
        }
        rounds++;
    }

    @Override
    protected void setStationPricing() {
        pricing = new SimplePricing(price, schedule.getDemand(), renewables);
    }

    @Override
    protected int[] computeDemand() {
        int[] demand = schedule.getDemand();
        for (EVObject ev: evBidders) {
            for (int s = ev.getStartSlot(); s <= ev.getEndSlot(); s++) {
                demand[s]++;
            }
        }
        return demand;
    }
}
