package main;

import evs.EV;
import station.StationInfo;
import station.offline.AbstractStation;
import statistics.StationData;

import java.util.ArrayList;

/**
 * Created by Thesis on 19/1/2018.
 */
public abstract class Execution {

    protected ArrayList<AbstractStation> stations;
    protected int slotsNumber;
    protected ArrayList<EV> evs;
    protected ArrayList<StationInfo> s_infos;
    protected boolean[] finished_stations; // the stations that have no duties
    protected boolean online;

    protected abstract void initialize();

    public abstract void execute();


    protected void evsRequestStations() {
        for (EV ev : evs) {
            ev.requestStation(s_infos, online);
        }
    }

    protected void evsEvaluateOffers() {
        for (EV ev : evs) {
            if (ev.hasSuggestions()) {
                ev.printSuggestionsList();
                ev.evaluateSuggestions();
            }

        }
    }

    protected void computeInitialOffers(AbstractStation station) {
        station.computeSchedule();
    }

    public void stationsSendOfferMessages(AbstractStation station) {
        station.sendOfferMessages();
    }

    protected void stationCheckInWhile(AbstractStation station, int stationID) {

        if (!finished_stations[stationID]) {
            station.updateBiddersLists();
            System.out.println("Ev Bidders:");
            System.out.println(station.printEVBidders());
            System.out.println("Waiting:");
            System.out.println(station.printEVWaiting());
            station.computeSchedule();
            if (station.isFinished()) {
                finished_stations[stationID] = true;
            }
        } else
            System.out.println("Station has no more duties!");
    }

    protected boolean checkFinished() {
        for (boolean finished_station : finished_stations) {
            if (!finished_station)
                return false;
        }
        return true;
    }

    protected void printEVs() {
        int counter = 0;
        for (EV ev : evs) {
            System.out.print("ev_" + counter + ": ");
            System.out.println(ev.toString() + "\n");
            counter++;
        }
        System.out.println("---------------------------------------------------------------");
    }

    public ArrayList<StationData> getStationData () {
        ArrayList<StationData> stationData = new ArrayList<>();
        for (AbstractStation station: stations) {
            stationData.add(new StationData(station.getInfo().getId(), station.getRejections(), station.getScheduleMap(), station.getInfo().getChargerNumber(), station.getChargedEVs()));
        }
        return stationData;
    }

    public int getEVsNumber () {
        return evs.size();
    }
}
