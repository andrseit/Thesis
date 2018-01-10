package main;

import evs.EV;
import evs.EVInfo;
import io.JSONFileParser;
import station.NewStation;
import station.Station;
import station.StationInfo;

import java.util.ArrayList;

/**
 * Created by Thesis on 8/1/2018.
 */
public class Thesis2 {

    private int slots_number = 10;
    private ArrayList<NewStation> stations;
    private ArrayList<StationInfo> s_infos;
    private ArrayList<EV> evs;

    private boolean[] finished_stations; // the stations that have no duties

    public void start () {
        createStations();
    }

    private void createStations () {




        stations = new ArrayList<>();
        s_infos = new ArrayList<>();
        JSONFileParser parser = new JSONFileParser();
        s_infos = parser.readStationData("station.json");
        for (StationInfo s : s_infos) {
            NewStation station = new NewStation(s, parser.getSlotsNumber());
            s.setStation(station);
            stations.add(station);
        }
        finished_stations = new boolean[stations.size()];
        for (int s = 0; s < finished_stations.length; s++) {
            finished_stations[s] = false;
        }

        evs = parser.readEVsData("evs.json");
        for (EV ev: evs) {
            ev.requestStation(s_infos);
            //System.out.println();
        }

        // Stations send initial offers
        for (NewStation station: stations) {
            System.out.println(" ==================  Station " + station.getInfo().getId() +
            "  =================");
            System.out.println(station.printEVBidders());
            station.computeSchedule();
            station.sendInitialOffer();
            System.out.println();
            //break;
        }

        for (EV ev: evs) {
            ev.printSuggestionsList();
            ev.evaluateSuggestions();
            //System.out.println();
        }
        System.out.println("-------- Phase 1 -------");




        // compute new offers
        for (NewStation station: stations) {
            System.out.println(" ======= Station_" + station.getInfo().getId() + " =======");
            station.updateBiddersLists();
            System.out.println(station.printEVBidders());
            System.out.println("Waiting...");
            System.out.println(station.printEVWaiting());
            station.resetChargers();
            boolean computed = station.computeSchedule();
            if (computed) {
                station.checkWaiting();
                station.findSuggestions();
                station.sendNewSuggestionMessage();
            }
            break;
        }

        // answer to these offers



        while (!checkFinished()) {

            for (EV ev: evs) {
                ev.printSuggestionsList();
                ev.evaluateSuggestions();
                //System.out.println();
            }

            // negotiations
            for (int s = 0; s < stations.size(); s++) {
                NewStation station = stations.get(s);
                System.out.println(" ======= Station_" + station.getInfo().getId() + " =======");
                station.updateBiddersLists();
                station.updateNegotiationSchedule();
                System.out.println(station.printEVBidders());
                if (!station.isWaitingEmpty()) {
                    System.out.println("Waiting...");
                    System.out.println(station.printEVWaiting());
                    station.findSuggestions();
                    station.sendNewSuggestionMessage();
                }
                if (station.isFinished()) {
                    finished_stations[s] = true;
                    // WARNING: auto na fygei, einai apla gia debugging
                    for (int i = 0; i < finished_stations.length; i++) {
                        finished_stations[i] = true;
                    }
                }
                else
                    System.out.println("You have failed Andrias");
                break;
            }

        }
    }


    private boolean checkFinished () {
        for (int s = 0; s < finished_stations.length; s++) {
            if (finished_stations[s] == false)
                return false;
        }
        return true;
    }



}
