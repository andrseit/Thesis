package agents.station;

/**
 * Created by Thesis on 29/3/2019.
 */
public class StationStatistics {

    private int chargersNumber;
    private int slotsNumber;

    private int requests; // total requests received (no delay requests)
    private int accepted; // accepted the initial offer
    private int charged; // the final number of EVs that will charged (not equal to accepted)
    private int rejected; // rejected the initial offers (no cancellations or rejections after delay request)
    private int delays; // number of delay requests
    private int delayRejeceted; // agents.evs that informed about delaying but where not able to be charged thereafter
    private int cancellations; // number of cancellations - not added to rejections
    private int slotsUsed;

    public StationStatistics (int chargersNumber, int slotsNumber) {
        requests = 0;
        accepted = 0;
        charged = 0;
        rejected = 0;
        delays = 0;
        delayRejeceted = 0;
        cancellations = 0;
        slotsUsed = 0;
        this.chargersNumber = chargersNumber;
        this.slotsNumber = slotsNumber;
    }

    public void updateRequests (int add) {
        requests += add;
    }

    public void updateAccepted (int add) {
        accepted += add;
    }

    public void updateRejected (int add) {
        rejected += add;
    }

    public void updateDelays (int add) {
        delays += add;
    }

    public void updateDelayRejected (int add) {
        delayRejeceted += add;
    }

    public void updateCancellations (int add) {
        cancellations += add;
    }

    public void updateSlotsUsed (int add) {
        slotsUsed += add;
    }

    private void setCharged () {
        charged = accepted - delayRejeceted - cancellations;
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

    public String toString () {
        setCharged();
        return "#Accepted: " + accepted + "(" + getPercentage(accepted, requests) + "% of requests)" + "\n" +
                "#Rejections: " + rejected + "(" + getPercentage(rejected, requests) + "% of requests)" + "\n" +
                "#Delays: " + delays + "(" + getPercentage(delays, accepted) + "% of accepted)" +"\n" +
                "#Rejected (after delay request): " + delayRejeceted + "(" + getPercentage(delayRejeceted, accepted) + "% of accepted)" +"\n" +
                "#Cancellations: " + cancellations + "(" + getPercentage(cancellations, accepted) + "% of accepted)" + "\n" +
                "#Charged: " + charged + "(" + getPercentage(charged, requests) + "% of requests)" + "\n" +
                "Slots used: " + slotsUsedPercentage() + "%";
    }
}
