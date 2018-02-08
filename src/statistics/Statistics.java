package statistics;

import station.EVObject;
import various.ArrayTransformations;

import java.util.ArrayList;

/**
 * Created by Thesis on 4/1/2018.
 */
public class Statistics {

    private ArrayList<StationData> stationData;
    int overallChargedEVs;
    int overallNotChargedEVs;
    int overallRejections;
    int overallRequests;
    int overallChargersUsed;
    int overallChargersNumber;
    int overallProfit;
    int overallLoss;

    double overallChargedEVsPercentage;
    double overallRejectionsPercentage;
    double overallChargersUsedPercentage;
    double overallNotChargedPercentage;

    public Statistics(ArrayList<StationData> stationData, int overallEVsNumber) {
        this.overallRequests = overallEVsNumber;
        this.stationData = stationData;
    }

    public void computeStats () {
        for (StationData station: stationData) {
            computeChargedRejection(station);
            computeOccupancy(station);
            computeProfit(station);
            computePreferencesLoss(station);
        }
        computeOverallStats();
    }

    public void printOverallStats () {
        System.out.println(this.toString());
    }

    public void printStationStats() {
        for (StationData station: stationData) {
            System.out.println(station.toString());
        }
        StationData s = stationData.get(0);
        for (EVObject ev: s.getEvsCharged()) {
            if (ev.hasSuggestion())
                System.out.println(ev.getSuggestion().getTime());
        }
    }

    public void printTimeStats () {
        for (StationData station: stationData) {
            double[] time = station.getInitialScheduleTime();
            System.out.println("Initial schedule times: ");
            for (int s = 0; s < time.length; s++) {
                System.out.print(time[s] + " ");
            }
            System.out.println();
            time = station.getNegotiationsTime();
            System.out.println("Negotiation schedule times: ");
            for (int s = 0; s < time.length; s++) {
                System.out.print(time[s] + " ");
            }
            System.out.println();
            time = station.getNegotiators();
            System.out.println("Negotiators: ");
            for (int s = 0; s < time.length; s++) {
                System.out.print((int) time[s] + " ");
            }
            System.out.println();
            time = station.getRoundsCount();
            System.out.println("Rounds count: ");
            for (int s = 0; s < time.length; s++) {
                System.out.print((int)time[s] + " ");
            }
            System.out.println();
        }
    }

    private void computeOverallStats() {
        overallNotChargedEVs = overallRequests - overallChargedEVs;
        overallChargedEVsPercentage = SimpleMath.getPercentage(overallChargedEVs, overallRequests);
        overallNotChargedPercentage = SimpleMath.getPercentage(overallNotChargedEVs, overallRequests);
        overallRejectionsPercentage = SimpleMath.getPercentage(overallRejections, overallRequests);
        overallChargersUsedPercentage = SimpleMath.getPercentage(overallChargersUsed, overallChargersNumber);
    }

    private void computeChargedRejection (StationData station) {
        int evsCharged = station.getEvsChargedNumber();
        int requests = station.getRequests();
        int rejections = station.getRejections();
        station.setChargedPercentage(SimpleMath.getPercentage(evsCharged, requests));
        station.setRejectedPercentage(SimpleMath.getPercentage(rejections, requests));

        overallChargedEVs += evsCharged;
    }

    private void computeOccupancy (StationData station) {
        int[][] map = station.getScheduleMap();
        ArrayTransformations t = new ArrayTransformations();
        t.printIntArray(map);
        int[] occupancy = t.getColumnsCount(map);
        int chargersUsed = 0;
        for (int anOccupancy : occupancy) {
            chargersUsed += anOccupancy;
        }

        int allChargers = station.getChargersNumber() * occupancy.length;
        station.setChargersUsed(chargersUsed);
        station.setChargersUsedPercentage(SimpleMath.getPercentage(chargersUsed, allChargers));
        overallChargersUsed += chargersUsed;
        overallChargersNumber += station.getChargersNumber() * occupancy.length;
    }

    private void computeProfit (StationData station) {
        int profit = 0;
        for (EVObject ev: station.getEvsCharged()) {
            profit += ev.getFinalPayment();
        }
        station.setProfit(profit);
        overallProfit += profit;
    }

    private void computePreferencesLoss (StationData station) {
        int totalLoss = 0;
        for (EVObject ev: station.getEvsCharged()) {
            System.out.println("get loss  " + ev.getPreferencesLoss());
            totalLoss += ev.getPreferencesLoss();
        }
        station.setPreferencesLoss(totalLoss);
        overallLoss += totalLoss;
    }

    @Override
    public String toString() {
        return "Overall Stats: " +
                "\n\t*Requests: " + overallRequests +
                "\n\t*EVs charged: " + overallChargedEVs + "(" + overallChargedEVsPercentage + "%)" +
                "\n\t*EVs not charged: " + overallNotChargedEVs + "(" + overallNotChargedPercentage + "%)" +
                "\n\t*Chargers Used: " + " " + overallChargersUsedPercentage + "%" +
                "\n\t*Profit: " + overallProfit+
                "\n\t*Total loss: " + overallLoss;
    }
}
