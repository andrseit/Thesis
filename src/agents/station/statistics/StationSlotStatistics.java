package agents.station.statistics;

import user_interface.EVStateEnum;
import user_interface.EVView;

import java.util.HashMap;

/**
 * Keeps statistics for a single slot
 */
public class StationSlotStatistics {

    private StationSystemValues systemValues;
    private HashMap<Integer, EVView> evs;
    private int slot;

    private int requests; // total requests received (no delay requests) a
    private int accepted; // accepted the initial offer b
    private int charged; // the final number of EVs that will charged (not equal to accepted) c
    private int acceptedAlternative; // the number of evs that did not charge in their initial preferences d
    private int rejected; // rejected the initial offers (no cancellations or rejections after delay request) e
    private int delays; // number of delay requests f
    private int acceptedAlternativeDelay; // number of evs that accepted an alternative suggestion after they claimed delay g
    private int delayRejected; // agents.evs that informed about delaying but where not able to be charged thereafter h
    private int cancellations; // number of cancellations - not added to rejections i
    private int slotsUsed;

    public StationSlotStatistics (StationSystemValues systemValues, int slot) {
        evs = new HashMap<>();
        requests = 0;
        accepted = 0;
        charged = 0;
        rejected = 0;
        delays = 0;
        delayRejected = 0;
        cancellations = 0;
        slotsUsed = 0;
        this.slot = slot;

        this.systemValues = systemValues;
    }

    private void calculateStatistics () {
        for (Integer evID: evs.keySet()) {
            EVView currentEV = evs.get(evID);
            boolean delayed = false;
            boolean alternative = false;
            for (EVStateEnum state: currentEV.getStates()) {
                if (state.equals(EVStateEnum.EV_STATE_REQUESTED))
                    requests++;
                else if (state.equals(EVStateEnum.EV_STATE_DELAYED)) {
                    delayed = true;
                    delays++;
                    charged--;
                    if (alternative)
                        acceptedAlternative--;
                    alternative = false;
                }
                else if (state.equals(EVStateEnum.EV_STATE_CANCELLED)) {
                    cancellations++;
                    charged--;
                }
                else if (state.equals(EVStateEnum.EV_STATE_ACCEPTED_INITIAL)) {
                    if (!delayed)
                        accepted++;
                    charged++;
                }
                else if (state.equals(EVStateEnum.EV_STATE_ACCEPTED_ALTERNATIVE)) {
                    alternative = true;
                    if (delayed)
                        acceptedAlternativeDelay++;
                    else {
                        accepted++;
                        acceptedAlternative++;
                    }
                    charged++;
                }
                else if (state.equals(EVStateEnum.EV_STATE_REJECTED)) {
                    if (delayed)
                        delayRejected++;
                    else
                        rejected++;
                }
            }
            slotsUsed += currentEV.getSlotsUsed();
        }
    }

    private void resetStatistics () {
        requests = 0;
        accepted = 0;
        acceptedAlternative = 0;
        acceptedAlternativeDelay = 0;
        charged = 0;
        rejected = 0;
        delays = 0;
        delayRejected = 0;
        cancellations = 0;
        slotsUsed = 0;
    }

    private double slotsUsedPercentage () {
        double x = (double) slotsUsed;
        double y = (double) (systemValues.getChargersNumber() * systemValues.getSlotsNumber());
        double division = (x/y) * 100;
        return Math.round(division * 100.0) / 100.0;
    }

    private double getPercentage (int x, int y) {
        double division = ((double) x / (double) y) * 100;
        return Math.round(division * 100.0) / 100.0;
    }

    public void addEV (int evID, EVStateEnum state, String preferences, int slots) {
        if (evs.keySet().contains(evID)) {
            EVView currentView = evs.get(evID);
            currentView.getStates().add(state);
            currentView.getPreferencesStates().add(preferences);
            currentView.setSlotsUsed(slots);
        } else {
            EVView view = new EVView("ev" + evID, state, preferences);
            view.setSlotsUsed(slots);
            evs.put(evID, view);
        }
    }

    public HashMap<Integer, EVView> getEvs() { return evs; }

    public String toCSV () {
        resetStatistics();
        calculateStatistics();
        String comma = ",";
        StringBuilder builder = new StringBuilder();
        builder.append(systemValues.getStationID())
                .append(comma).append(slot)
                .append(comma).append(accepted)
                .append(comma).append(acceptedAlternative)
                .append(comma).append(rejected)
                .append(comma).append(delays)
                .append(comma).append(acceptedAlternativeDelay)
                .append(comma).append(delayRejected)
                .append(comma).append(cancellations)
                .append(comma).append(charged)
                .append(comma).append(slotsUsedPercentage())
                .append("\n");
        return builder.toString();
    }

    public String toString () {
        StringBuilder str = new StringBuilder();
        for (Integer key: evs.keySet())
            str.append(evs.get(key)).append("\n");
        //setCharged();
        resetStatistics();
        calculateStatistics();
        System.out.println(slotsUsed);
        return "#Accepted: " + accepted + "(" + getPercentage(accepted, requests) + "% of requests)" + "\n" +
                "#Alternatives: " + acceptedAlternative + "(" + getPercentage(acceptedAlternative, accepted) + "% of accepted)" + "\n" +
                "#Rejections: " + rejected + "(" + getPercentage(rejected, requests) + "% of requests)" + "\n" +
                "#Delays: " + delays + "(" + getPercentage(delays, accepted) + "% of accepted)" +"\n" +
                "#Alternatives (Delay): " + acceptedAlternativeDelay + "(" + getPercentage(acceptedAlternativeDelay, accepted) + "% of accepted)" + "\n" +
                "#Rejected (after delay request): " + delayRejected + "(" + getPercentage(delayRejected, accepted) + "% of accepted)" +"\n" +
                "#Cancellations: " + cancellations + "(" + getPercentage(cancellations, accepted) + "% of accepted)" + "\n" +
                "#Charged: " + charged + "(" + getPercentage(charged, requests) + "% of requests)" + "\n" +
                "Slots used: " + slotsUsedPercentage() + "%" + "\n" +
                "-------------------\n" + str;
    }
}
