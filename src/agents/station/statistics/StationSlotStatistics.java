package agents.station.statistics;

import agents.evs.Preferences;
import statistics.SimpleMath;
import user_interface.EVStateEnum;
import user_interface.EVView;
import various.ArrayTransformations;

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
    private int rejectedAlternative; // how many alternatives have been rejected
    private int rejected; // rejected the initial offers (no cancellations or rejections after delay request) e
    private int delays; // number of delay requests f
    private int acceptedAlternativeDelay; // number of evs that accepted an alternative suggestion after they claimed delay g
    private int delayRejected; // agents.evs that informed about delaying but where not able to be charged thereafter h
    private int cancellations; // number of cancellations - not added to rejections i
    private int slotsUsed;

    private double evsRounds; // average of rounds of conversation with evs
    private double evsUtility; // average utility of charged evs
    private double negotiatorsRounds; // average rounds of conversation but only for those who accepted an alternative
    private double negotiatorsUtility; // average utility of evs that accepted an alternative

    int minSlot, maxSlot; // from which to which slot evs are charging. the first and the last slot that ev charging takes place
    int[] slotsOccupancy; // in each slot, how many chargers are used

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

        minSlot = Integer.MAX_VALUE;
        maxSlot = Integer.MIN_VALUE;

        slotsOccupancy = new int[systemValues.getSlotsNumber()];

        this.systemValues = systemValues;
    }

    private void calculateStatistics () {
        for (Integer evID: evs.keySet()) {
            EVView currentEV = evs.get(evID);
            Preferences initialPreferences = null, acceptedPreferences = null;
            boolean delayed = false;
            boolean alternative = false;
            boolean willCharge = false;
            int waitRounds = 0;

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
                    waitRounds = 0;
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
                    if (acceptedPreferences.getStart() < minSlot)
                        minSlot = acceptedPreferences.getStart();
                    if (acceptedPreferences.getEnd() > maxSlot)
                        maxSlot = acceptedPreferences.getEnd();

                    if (!delayed)
                        accepted++;
                    charged++;
                    willCharge = true;
                    waitRounds++;
                }
                else if (state.equals(EVStateEnum.EV_STATE_ACCEPTED_ALTERNATIVE)) {
                    acceptedPreferences = currentEV.getPreferencesStates().get(s);
                    if (acceptedPreferences.getStart() < minSlot)
                        minSlot = acceptedPreferences.getStart();
                    if (acceptedPreferences.getEnd() > maxSlot)
                        maxSlot = acceptedPreferences.getEnd();

                    alternative = true;
                    if (delayed)
                        acceptedAlternativeDelay++;
                    else {
                        accepted++;
                        acceptedAlternative++;
                    }
                    charged++;
                    willCharge = true;
                    waitRounds++;
                }
                else if (state.equals(EVStateEnum.EV_STATE_REJECTED) || state.equals(EVStateEnum.EV_STATE_REJECTED_ALTERNATIVE)) {
                    if (state.equals(EVStateEnum.EV_STATE_REJECTED_ALTERNATIVE))
                        rejectedAlternative++;
                    if (delayed)
                        delayRejected++;
                    else
                        rejected++;
                    waitRounds++;
                } else if (state.equals(EVStateEnum.EV_STATE_WAIT)) {
                    waitRounds++;
                }
            }
            if (willCharge) {
                evsUtility += getPreferencesDistance(initialPreferences, acceptedPreferences);
                evsRounds += waitRounds;
                if (alternative) {
                    negotiatorsRounds += waitRounds;
                    negotiatorsUtility += getPreferencesDistance(initialPreferences, acceptedPreferences);
                }
            }
            slotsUsed += currentEV.getSlotsUsed();
        }
        evsUtility = SimpleMath.round(evsUtility / charged, 2);
        evsRounds = SimpleMath.round(evsRounds / charged, 2);

        negotiatorsRounds = SimpleMath.round(negotiatorsRounds / (acceptedAlternative + acceptedAlternativeDelay), 2);
        negotiatorsUtility = SimpleMath.round(negotiatorsUtility / (acceptedAlternative + acceptedAlternativeDelay), 2);
    }

    private void resetStatistics () {
        requests = 0;
        accepted = 0;
        acceptedAlternative = 0;
        rejectedAlternative = 0;
        acceptedAlternativeDelay = 0;
        charged = 0;
        rejected = 0;
        delays = 0;
        delayRejected = 0;
        cancellations = 0;
        slotsUsed = 0;
        evsRounds = 0;
        evsUtility = 0;
        negotiatorsRounds = 0;
        negotiatorsUtility = 0;
        minSlot = Integer.MAX_VALUE;
        maxSlot = Integer.MIN_VALUE;
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

    public void addEV (int evID, EVStateEnum state, Preferences preferences, int slots) {
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

    public void addSlots (int[][] schedule) {
        slotsOccupancy = ArrayTransformations.getColumnsCount(schedule);
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
                .append(comma).append(rejectedAlternative)
                .append(comma).append(delays)
                .append(comma).append(acceptedAlternativeDelay)
                .append(comma).append(delayRejected)
                .append(comma).append(cancellations)
                .append(comma).append(charged)
                .append(comma).append(slotsUsedPercentage())
                .append(comma).append(minSlot)
                .append(comma).append(maxSlot)
                .append(comma).append(evsUtility)
                .append(comma).append(evsRounds)
                .append(comma).append(negotiatorsUtility)
                .append(comma).append(negotiatorsRounds)
                .append("\n");
        return builder.toString();
    }

    public String slotsToCSV () {
        String comma = ",";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < systemValues.getSlotsNumber(); i++) {
            builder.append(slotsOccupancy[i]).append(comma);
        }
        return builder.substring(0, builder.length() - 1) + "\n";
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
                "#Alternative Rejections: " + rejectedAlternative + "(" + getPercentage(rejectedAlternative, rejected) + "% of rejected)" + "\n" +
                "#Delays: " + delays + "(" + getPercentage(delays, accepted) + "% of accepted)" +"\n" +
                "#Alternatives (Delay): " + acceptedAlternativeDelay + "(" + getPercentage(acceptedAlternativeDelay, accepted) + "% of accepted)" + "\n" +
                "#Rejected (after delay request): " + delayRejected + "(" + getPercentage(delayRejected, accepted) + "% of accepted)" +"\n" +
                "#Cancellations: " + cancellations + "(" + getPercentage(cancellations, accepted) + "% of accepted)" + "\n" +
                "#Charged: " + charged + "(" + getPercentage(charged, requests) + "% of requests)" + "\n" +
                "Slots used: " + slotsUsedPercentage() + "%" + "\n" +
                "Average conversation rounds: " + evsRounds  + "\n" +
                "EVs Utility: " + evsUtility + "%" + "\n" +
                "-------------------\n" + str;
    }

    private double getPreferencesDistance (Preferences initial, Preferences accepted) {

        if (accepted == null)
            return 0.0;

        int slotsNumber = systemValues.getSlotsNumber();

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

        return SimpleMath.round(total, 2);
    }
}
