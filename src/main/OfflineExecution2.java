package main;

import io.JSONFileParser;
import station.AbstractStation;
import station.SimpleStation;
import station.StationInfo;

import java.util.ArrayList;

/**
 * Created by Thesis on 19/1/2018.
 */
public class OfflineExecution2 extends Execution2 {

    private ArrayList<AbstractStation> stations;

    @Override
    public void initialize() {
        JSONFileParser parser = new JSONFileParser();
        stations = new ArrayList<>();
        s_infos = parser.readStationData("station.json");
        this.slotsNumber = parser.getSlotsNumber();
        for (StationInfo s: s_infos) {
            SimpleStation station = new SimpleStation(s, slotsNumber);
            s.setStation(station);
            stations.add(station);
        }
        finished_stations = new boolean[stations.size()];
        for (int s = 0; s < finished_stations.length; s++) {
            finished_stations[s] = false;
        }
        evs = parser.readEVsData("evs.json");
    }

    @Override
    public void execute() {
        // initialize variables
        this.initialize();
        //evs request from stations
        this.evsRequestStations();
        // stations sending initial offers
        for (AbstractStation station: stations) {
            this.sendInitialOffers(station);
        }
        /*
        // evs evaluate offers
        this.evsEvaluateOffers();
        // stations compute new offers and send new messages
        for (int s = 0; s < stations.size(); s++) {
            this.stationCheckInWhile(stations.get(s), s);
        }
        */
        // conversation
        while (!checkFinished()) {
            this.evsEvaluateOffers();
            // negotiations
            for (int s = 0; s < stations.size(); s++) {
                this.stationCheckInWhile(stations.get(s), s);
            }
        }
    }
}
