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

    protected abstract void initialize();

    public abstract void execute();


    protected void evsRequestStations() {
        for (EV ev : evs) {
            ev.requestStation(s_infos);
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
            station.computeSchedule();
            if (station.isFinished()) {
                finished_stations[stationID] = true;
            }
        } else
            System.out.println("Station has no more duties!");
    }

    protected boolean checkFinished() {
        for (int s = 0; s < finished_stations.length; s++) {
            if (!finished_stations[s])
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
}
