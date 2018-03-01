package statistics;

import station.EVObject;
import various.ArrayTransformations;

import java.util.ArrayList;

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
        /*
        for (EVObject ev: s.getEvsCharged()) {
            if (ev.hasSuggestion())
                System.out.println(ev.getSuggestion().getTime());
        }
        */
    }

    public void printTimeStats () {
        for (StationData station: stationData) {
            System.out.println("Station_" + station.getStation_id());
            double[] time = station.getInitialScheduleTime();
            System.out.println("\tInitial schedule times: ");
            for (int s = 0; s < time.length; s++) {
                System.out.print("\t\t" + time[s] + " ");
            }
            System.out.println();
            time = station.getNegotiationsTime();
            System.out.println("\tNegotiation schedule times: ");
            for (int s = 0; s < time.length; s++) {
                System.out.print("\t\t" + time[s] + " ");
            }
            System.out.println();
            time = station.getNegotiators();
            System.out.println("\tNegotiators: ");
            for (int s = 0; s < time.length; s++) {
                System.out.print("\t\t" + (int) time[s] + " ");
            }
            System.out.println();
            time = station.getRoundsCount();
            System.out.println("\tRounds count: ");
            for (int s = 0; s < time.length; s++) {
                System.out.print("\t\t" + (int)time[s] + " ");
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
        if (map.length > 0) {
            ArrayTransformations t = new ArrayTransformations();
            //t.printIntArray(map);
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
        int acceptedSuggestion = 0;
        for (EVObject ev: station.getEvsCharged()) {
            int currentPreferencesLoss = ev.getPreferencesLoss();
            if (currentPreferencesLoss != 0) {
                totalLoss += currentPreferencesLoss;
                acceptedSuggestion += 1;
            }
        }
        station.setPreferencesLoss(totalLoss);
        station.setAcceptedSuggestion(acceptedSuggestion);
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

    public String fileStationsString () {
        StringBuilder str = new StringBuilder();
        for (StationData station: stationData) {
            str.append(station.fileString()).append("\n");
        }
        return str.toString();
    }

    public String timesString () {
        double allInitial = 0.0;
        double allNegotiation  = 0.0;
        for (StationData station: stationData) {
            allInitial += station.getTotalInitialTime();
            allNegotiation += station.getTotalNegotiationTime();
        }
        allInitial = allInitial/4;
        allNegotiation = allNegotiation/4;

        String initial = String.valueOf(allInitial).replace('.', ',');
        String negotiation = String.valueOf(allNegotiation).replace('.', ',');
        return initial + ", " + negotiation;
    }

}
