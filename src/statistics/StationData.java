package statistics;

import station.EVObject;

import java.util.ArrayList;

/**
 * Created by Thesis on 1/2/2018.
 * This class is used to gather data from the stations
 * such as: schedule, evs charged.
 * It is going to be used from the statistics class
 */
public class StationData {
    private int station_id;
    private int rejections;
    private int[][] schedule;
    private int chargersNumber;
    private ArrayList<EVObject> evsCharged;

    private int evsChargedNumber;
    private int requests;
    private int chargersUsed;
    private int profit;
    private int preferencesLoss;
    private double chargedPercentage;
    private double rejectedPercentage;
    private double chargersUsedPercentage;

    private double[] initialScheduleTime;
    private double[] negotiationsTime;
    private double[] negotiators;
    private double[] roundsCount;

    public StationData(int station_id, int rejections, int[][] schedule, int chargersNumber, ArrayList<EVObject> evsCharged) {
        this.station_id = station_id;
        this.rejections = rejections;
        this.schedule = schedule;
        this.chargersNumber = chargersNumber;
        this.evsCharged = evsCharged;
        requests = evsCharged.size() + rejections;
        evsChargedNumber = evsCharged.size();
    }

    public int getStation_id() {
        return station_id;
    }

    public int getRejections() {
        return rejections;
    }

    public int getRequests() {
        return requests;
    }

    public int getEvsChargedNumber() {
        return evsChargedNumber;
    }

    public int[][] getScheduleMap() {
        return schedule;
    }

    public int getChargersNumber() {
        return chargersNumber;
    }

    public ArrayList<EVObject> getEvsCharged() {
        return evsCharged;
    }

    public void setChargedPercentage(double chargedPercentage) {
        this.chargedPercentage = chargedPercentage;
    }

    public void setRejectedPercentage(double rejectedPercentage) {
        this.rejectedPercentage = rejectedPercentage;
    }

    public void setChargersUsedPercentage(double chargersUsedPercentage) {
        this.chargersUsedPercentage = chargersUsedPercentage;
    }

    public void setChargersUsed(int chargersUsed) {
        this.chargersUsed = chargersUsed;
    }

    public void setProfit(int profit) {
        this.profit = profit;
    }

    public void setPreferencesLoss(int preferencesLoss) {
        this.preferencesLoss = preferencesLoss;
    }

    public void setTimeStats (double[] initialScheduleTime, double[] negotiationsTime, double[] negotiators, double[] roundsCount) {
        this.initialScheduleTime = initialScheduleTime;
        this.negotiationsTime = negotiationsTime;
        this.negotiators = negotiators;
        this.roundsCount = roundsCount;
    }

    public double[] getInitialScheduleTime() {
        return initialScheduleTime;
    }

    public double[] getNegotiationsTime() {
        return negotiationsTime;
    }

    public double[] getNegotiators() {
        return negotiators;
    }

    public double[] getRoundsCount() {
        return roundsCount;
    }

    public String toString () {
        return "Station_" + station_id + " -> chargers: " + chargersNumber +
                "\n\t*Requests: " + requests +
                "\n\t*EVs charged: " + evsCharged.size() + "(" + chargedPercentage + "%)" +
                "\n\t*EVs rejected: " + rejections + "(" + rejectedPercentage + "%)" +
                "\n\t*Chargers used: " + chargersUsed +  "(" + chargersUsedPercentage + "%)" +
                "\n\t*Profit: " + profit +
                "\n\t*Total loss: " + preferencesLoss;
    }
}
