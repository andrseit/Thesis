package main;

import evs.EV;
import station.offline.AbstractStation;
import station.StationInfo;

import java.util.ArrayList;

/**
 * Created by Thesis on 19/1/2018.
 */
public abstract class Execution {

    protected int slotsNumber;
    protected ArrayList<EV> evs;
    protected ArrayList<StationInfo> s_infos;
    protected boolean[] finished_stations; // the stations that have no duties

    protected abstract void initialize ();
    public abstract void execute ();


    protected void evsRequestStations () {
        for (EV ev: evs) {
            ev.requestStation(s_infos);
            //System.out.println();
        }
    }

    protected void evsEvaluateOffers () {
        for (EV ev: evs) {
            if (ev.hasSuggestions()) {
                ev.printSuggestionsList();
                ev.evaluateSuggestions();
            }
            //System.out.println();
        }
    }

    protected void sendInitialOffers (AbstractStation station) {
        System.out.println(" ==================  Station " + station.getInfo().getId() +
                "  =================");
        System.out.println(station.printEVBidders());
        station.computeSchedule();
        station.sendOfferMessages();
        System.out.println();
        //break;
    }

    protected void stationCheckInWhile (AbstractStation station, int stationID) {

        System.out.println(" ======= Station_" + station.getInfo().getId() + " =======");
        if (!finished_stations[stationID]) {
            station.updateBiddersLists();
            station.computeSchedule();
            System.out.println(station.printEVBidders());
            //if (!station.isWaitingEmpty()) {
                System.out.println("Waiting...");
                System.out.println(station.printEVWaiting());
                station.sendOfferMessages();
            //}
            station.printEVWaiting();
            if (station.isFinished()) {
                finished_stations[stationID] = true;
            } else
                System.out.println("You have failed Andrias");
        } else
            System.out.println("Station has no more duties!");
    }

    protected boolean checkFinished () {
        for (int s = 0; s < finished_stations.length; s++) {
            if (!finished_stations[s])
                return false;
        }
        return true;
    }
}
