package main;

import evs.EV;
import io.JSONFileParser;
import station.NewStation;
import station.OnlineStation;
import station.StationInfo;

import java.util.*;

/**
 * Created by Thesis on 8/1/2018.
 */
class Thesis2 {

    private int slots_number = 10;
    private ArrayList<NewStation> stations;
    private ArrayList<OnlineStation> onlineStations;
    private ArrayList<StationInfo> s_infos;
    private ArrayList<EV> evs;

    private boolean[] finished_stations; // the stations that have no duties

    public void start () {
        //offline();
        online();
    }

    private void initialize () {
        stations = new ArrayList<>();
        s_infos = new ArrayList<>();
        JSONFileParser parser = new JSONFileParser();
        s_infos = parser.readStationData("station.json");
        this.slots_number = parser.getSlotsNumber();
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
    }

    private void offline() {

        this.initialize();
        this.evsRequestStations();
        // Stations send initial offers
        for (NewStation station: stations) {
            this.sendInitialOffers(station);
        }



        this.evsEvaluateOffers();
        System.out.println("-------- Phase 1 -------");

        // compute new offers
        for (int s = 0; s < stations.size(); s++) {
            this.stationCheckInWhile(stations.get(s), s);
        }


        // answer to these offers
        while (!checkFinished()) {
            this.evsEvaluateOffers();
            // negotiations
            for (int s = 0; s < stations.size(); s++) {
                this.stationCheckInWhile(stations.get(s), s);
            }
        }

    }

    private void online () {
        this.initialize();
        onlineStations = new ArrayList<>();
        for (StationInfo s : s_infos) {
            OnlineStation station = new OnlineStation(s, slots_number);
            s.setStation(station);
            onlineStations.add(station);
        }
        PriorityQueue<EV> orderedEVs = this.orderEVs();




        for (int slot = 0; slot < slots_number; slot++) {
            System.out.println("------> Slot: " + slot);

            finished_stations = new boolean[stations.size()];
            for (int st = 0; st < finished_stations.length; st++) {
                finished_stations[st] = false;
            }

            // evs sending offers
            EV ev;
            while ((ev = orderedEVs.peek()) != null && ev.getInformSlot() == slot) {
                orderedEVs.poll();
                ev.requestStation(s_infos);
            }

            for (int st = 0; st < onlineStations.size(); st++) {
                OnlineStation station = onlineStations.get(st);
                if (!station.hasOffers(slot)) {
                    System.out.println("Mallon mpainei edw");
                    finished_stations[st] = true;
                } else {
                    this.sendInitialOffers(station);
                }
            }

            this.evsEvaluateOffers();
            for (int s = 0; s < onlineStations.size(); s++) {
                this.stationCheckInWhile(onlineStations.get(s), s);
            }
            while (!checkFinished()) {
                this.evsEvaluateOffers();
                // negotiations
                for (int s = 0; s < onlineStations.size(); s++) {
                    this.stationCheckInWhile(onlineStations.get(s), s);
                }
            }

            for (int st = 0; st < onlineStations.size(); st++) {
                OnlineStation station = onlineStations.get(st);
                if (station.isUpdate()) {
                    System.out.println("Updating");
                    station.updateStationData();
                }
            }
        }

    }



    // methods for the procedure

    /**
     * resets finished stations in the spesific slot
     * osoi exoun offers kane tous false osoi den exoun kane tous true
     */
    private void resetFinished () {
        for (int s = 0; s < finished_stations.length; s++) {
            finished_stations[s] = false;
        }
    }

    private void evsEvaluateOffers () {
        for (EV ev: evs) {
            if (ev.hasSuggestions()) {
                ev.printSuggestionsList();
                ev.evaluateSuggestions();
            }
            //System.out.println();
        }
    }

    private void evsRequestStations () {
        for (EV ev: evs) {
            ev.requestStation(s_infos);
            //System.out.println();
        }
    }

    private void sendInitialOffers (NewStation station) {
            System.out.println(" ==================  Station " + station.getInfo().getId() +
                    "  =================");
            System.out.println(station.printEVBidders());
            station.computeSchedule();
            station.sendSuggestionMessage();
            System.out.println();
            //break;
    }

    private void stationSendNewOffers () {
        for (int s = 0; s < stations.size(); s++) {
            NewStation station = stations.get(s);
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
            if (station.isFinished()) {
                finished_stations[s] = true;
                System.out.println(s);
            }
        }
    }


    private void stationCheckInWhile (NewStation station, int stationID) {

            System.out.println(" ======= Station_" + station.getInfo().getId() + " =======");
            if (!finished_stations[stationID]) {
                station.updateBiddersLists();
                station.resetChargers();
                station.computeSchedule();
                //station.updateNegotiationSchedule();
                System.out.println(station.printEVBidders());
                if (!station.isWaitingEmpty()) {
                    System.out.println("Waiting...");
                    System.out.println(station.printEVWaiting());
                    station.checkWaiting();
                    station.findSuggestions();
                    station.sendNewSuggestionMessage();
                }
                station.printEVWaiting();
                if (station.isFinished()) {
                    finished_stations[stationID] = true;
                } else
                    System.out.println("You have failed Andrias");
            } else
                System.out.println("Station has no more duties!");
    }

    private PriorityQueue<EV> orderEVs () {
        PriorityQueue<EV> orderedEVs = new PriorityQueue<>(10, new Comparator<EV>() {
            @Override
            public int compare(EV ev1, EV ev2) {
                return ev1.getInformSlot() - ev2.getInformSlot();
            }
        });
        for (EV ev: evs) {
           orderedEVs.offer(ev);
        }
        return orderedEVs;
    }

    private boolean checkFinished () {
        for (int s = 0; s < finished_stations.length; s++) {
            if (!finished_stations[s])
                return false;
        }
        return true;
    }



}
