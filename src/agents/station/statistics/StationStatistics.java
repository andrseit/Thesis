package agents.station.statistics;

import agents.evs.Preferences;
import user_interface.EVView;
import user_interface.EVStateEnum;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Thesis on 29/3/2019.
 */
public class StationStatistics {

    private StationSlotStatistics[] slotStatistics;
    private StationSystemValues systemValues;

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

    private double evsUtility;

    public StationStatistics (int stationID, int chargersNumber, int slotsNumber) {
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

        systemValues = new StationSystemValues(stationID, chargersNumber, slotsNumber);
        slotStatistics = new StationSlotStatistics[slotsNumber];
        for (int i = 0; i < slotStatistics.length; i++)
            slotStatistics[i] = new StationSlotStatistics(systemValues, slotsNumber, i);
    }

    private void calculateStatistics () {
        for (Integer evID: evs.keySet()) {
            EVView currentEV = evs.get(evID);
            Preferences initialPreferences = null, acceptedPreferences = null;
            boolean delayed = false;
            boolean alternative = false;
            boolean willCharge = false;
            
            for (int s = 0; s < currentEV.getStates().size(); s++) {
                EVStateEnum state = currentEV.getStates().get(s);
                if (state.equals(EVStateEnum.EV_STATE_REQUESTED)) {
                    initialPreferences = currentEV.getPreferencesStates().get(s);
                    requests++;
                }
                else if (state.equals(EVStateEnum.EV_STATE_DELAYED)) {
                    initialPreferences = currentEV.getPreferencesStates().get(s);
                    delayed = true;
                    delays++;
                    charged--;
                    willCharge = false;
                    if (alternative)
                        acceptedAlternative--;
                    alternative = false;
                }
                else if (state.equals(EVStateEnum.EV_STATE_CANCELLED)) {
                    cancellations++;
                    charged--;
                    willCharge = false;
                }
                else if (state.equals(EVStateEnum.EV_STATE_ACCEPTED_INITIAL)) {
                    acceptedPreferences = currentEV.getPreferencesStates().get(s);
                    if (!delayed)
                        accepted++;
                    charged++;
                    willCharge = true;
                }
                else if (state.equals(EVStateEnum.EV_STATE_ACCEPTED_ALTERNATIVE)) {
                    acceptedPreferences = currentEV.getPreferencesStates().get(s);
                    alternative = true;
                    if (delayed)
                        acceptedAlternativeDelay++;
                    else {
                        accepted++;
                        acceptedAlternative++;
                    }
                    charged++;
                    willCharge = true;
                }
                else if (state.equals(EVStateEnum.EV_STATE_REJECTED)) {
                    if (delayed)
                        delayRejected++;
                    else
                        rejected++;
                } else if (state.equals(EVStateEnum.EV_STATE_WAIT)) {

                }
            }
            if (willCharge)
                evsUtility += getPreferencesDistance(initialPreferences, acceptedPreferences);
            slotsUsed += currentEV.getSlotsUsed();
        }
        evsUtility = evsUtility / charged;
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
        evsUtility = 0;
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

    public void addEV (int evID, EVStateEnum state, Preferences preferences, int currentSlot, int slots) {
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

        for (int i = slotsNumber - 1; i >= currentSlot; i--)
            slotStatistics[i].addEV(evID, state, preferences, slots);
    }

    public HashMap<Integer, EVView> getEvs() { return evs; }

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
                "EVs Utility: " + evsUtility + "%" + "\n" +
                "-------------------\n" + str;
    }

    /**
     * return the number of the statistics
     * @return the slots number, actually
     */
    public int size() {
        return slotStatistics.length;
    }

    public StationSlotStatistics getSlotStatistics (int slot) {
        return slotStatistics[slot];
    }

    public double getPreferencesDistance (Preferences initial, Preferences accepted) {

        if (accepted == null)
            return 0.0;

        int start = initial.getStart();
        int end = initial.getEnd();
        int energy = initial.getEnergy();

        int fStart = accepted.getStart();
        int fEnd = accepted.getEnd();
        int fEnergy = accepted.getEnergy();

        if (fStart >= start && fEnd <= end && fEnergy == energy)
            return 100.0;

        int maxShift;
        if (start > slotsNumber - end - 1)
            maxShift = start;
        else
            maxShift = slotsNumber - end - 1;

        int maxWiden = slotsNumber - (end - start + 1);

        int maxEnergyLoss = energy - 1;

        int shift = (start > fStart) ? start - fStart : fStart - start;
        double shiftPer = ((double) shift/(double) maxShift)*100;

        int widen = (fEnd - fStart > end - start) ? (fEnd - fStart) - (end - start) : 0;
        double widenPer = ((double) widen/(double) maxWiden)*100;

        int energyLoss = energy - fEnergy;
        double energyPer = ((double) energyLoss/(double) maxEnergyLoss)*100;

        double total = 100 - (0.5*(0.5*widenPer + 0.5*shiftPer) + 0.5*energyPer);

        if (total > 100) {
            System.err.println("Utility transcends 100!");
            System.exit(1);
        }

        return total;
    }
}
