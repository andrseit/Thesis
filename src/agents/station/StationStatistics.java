package agents.station;

import user_interface.EVView;
import various.ConstantVariables;
import various.EVStateEnumeration;

import java.util.HashMap;

/**
 * Created by Thesis on 29/3/2019.
 */
public class StationStatistics {

    private HashMap<Integer, EVView> evs;

    private int chargersNumber;
    private int slotsNumber;

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

    public StationStatistics (int chargersNumber, int slotsNumber) {
        evs = new HashMap<>();
        requests = 0;
        accepted = 0;
        charged = 0;
        rejected = 0;
        delays = 0;
        delayRejected = 0;
        cancellations = 0;
        slotsUsed = 0;
        this.chargersNumber = chargersNumber;
        this.slotsNumber = slotsNumber;
    }

    private void calculateStatistics () {
        for (Integer evID: evs.keySet()) {
            EVView currentEV = evs.get(evID);
            boolean delayed = false;
            boolean alternative = false;
            for (EVStateEnumeration state: currentEV.getStates()) {
                if (state.equals(EVStateEnumeration.EV_STATE_REQUESTED))
                    requests++;
                else if (state.equals(EVStateEnumeration.EV_STATE_DELAYED)) {
                    delayed = true;
                    delays++;
                    charged--;
                    if (alternative)
                        acceptedAlternative--;
                    alternative = false;
                }
                else if (state.equals(EVStateEnumeration.EV_STATE_CANCELLED)) {
                    cancellations++;
                    charged--;
                }
                else if (state.equals(EVStateEnumeration.EV_STATE_ACCEPTED_INITIAL)) {
                    if (!delayed)
                        accepted++;
                    charged++;
                }
                else if (state.equals(EVStateEnumeration.EV_STATE_ACCEPTED_ALTERNATIVE)) {
                    alternative = true;
                    if (delayed)
                        acceptedAlternativeDelay++;
                    else {
                        accepted++;
                        acceptedAlternative++;
                    }
                    charged++;
                }
                else if (state.equals(EVStateEnumeration.EV_STATE_REJECTED)) {
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
        double y = (double) (chargersNumber * slotsNumber);
        double division = (x/y) * 100;
        return Math.round(division * 100.0) / 100.0;
    }

    private double getPercentage (int x, int y) {
        double division = ((double) x / (double) y) * 100;
        return Math.round(division * 100.0) / 100.0;
    }

    public void addEV (int evID, EVStateEnumeration state, String preferences, int slots) {
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

    public String toString () {
        StringBuilder str = new StringBuilder();
        for (Integer key: evs.keySet())
            str.append(evs.get(key) + "\n");
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
