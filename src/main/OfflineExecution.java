package main;

import io.JSONFileParser;
import station.StationInfo;
import station.offline.AbstractStation;
import station.offline.SimpleStation;

import java.util.ArrayList;

/**
 * Created by Thesis on 19/1/2018.
 */
public class OfflineExecution extends Execution {

    private ArrayList<AbstractStation> stations;

    @Override
    public void initialize() {
        JSONFileParser parser = new JSONFileParser();
        stations = new ArrayList<>();
        for (AbstractStation station: parser.readOfflineStations("station.json")) {
            stations.add(station);
        }
        s_infos = new ArrayList<>();
        for (AbstractStation station: stations) {
            s_infos.add(station.getInfo());
        }
        this.slotsNumber = parser.getSlotsNumber();

        finished_stations = new boolean[stations.size()];
        for (int s = 0; s < finished_stations.length; s++) {
            finished_stations[s] = false;
        }
        evs = parser.readEVsData("evs.json");
        printEVs();
    }

    @Override
    public void execute() {
        System.out.println("Offline Execution is starting. Initializing...");
        // initialize variables
        this.initialize();
        //evs request from stations
        System.out.println("EVs request from the stations");
        this.evsRequestStations();
        // stations sending initial offers
        System.out.println("Stations computing and sending initial offers...");
        for (AbstractStation station : stations) {
            System.out.println("----------------- Station_" + station.getInfo().getId() + " ---------------------");
            System.out.println("Ev Bidders:");
            System.out.println(station.printEVBidders());
            System.out.println("Waiting:");
            System.out.println(station.printEVWaiting());
            this.computeInitialOffers(station);
            this.stationsSendOfferMessages(station);
        }

        System.out.println("\nConversation between evs and stations starting...");
        // conversation
        while (!checkFinished()) {
            this.evsEvaluateOffers();
            // negotiations
            for (int s = 0; s < stations.size(); s++) {
                System.out.println("----------------- Station_" + stations.get(s).getInfo().getId() + " ---------------------");
                this.stationCheckInWhile(stations.get(s), s);
                this.stationsSendOfferMessages(stations.get(s));
            }
        }
        System.out.println("------------\n\nExecution is over!");
        System.out.println("These are the final schedules:");
        for (int s = 0; s < stations.size(); s++) {
            System.out.println("----------------- Station_" + stations.get(s).getInfo().getId() + " ---------------------");
            stations.get(s).printScheduleMap();
        }
    }
}
