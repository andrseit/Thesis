package main;

import evs.EV;
import io.JSONFileParser;
import station.*;
import station.online.AbstractOnlineStation;
import station.online.SimpleOnlineStation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Thesis on 19/1/2018.
 */
public class OnlineExecution extends Execution {

    private ArrayList<AbstractOnlineStation> stations;
    private PriorityQueue<EV> orderedEVs;

    @Override
    protected void initialize() {
        JSONFileParser parser = new JSONFileParser();
        stations = new ArrayList<>();
        s_infos = parser.readStationData("station.json");
        this.slotsNumber = parser.getSlotsNumber();
        for (StationInfo s : s_infos) {
            SimpleOnlineStation station = new SimpleOnlineStation(s, slotsNumber);
            s.setStation(station);
            stations.add(station);
        }
        evs = parser.readEVsData("evs.json");
        orderedEVs = this.orderEVs();
        printEVs();
    }

    @Override
    public void execute() {

        System.out.println("Online Execution is starting. Initializing...");
        this.initialize();

        for (int slot = 0; slot < slotsNumber; slot++) {
            System.out.println("------> Slot: " + slot);
            this.resetFinishedStations();

            System.out.println("EVs request from the stations:\n");
            EV ev;
            // if there are no evs
            while ((ev = orderedEVs.peek()) != null && ev.getInformSlot() == slot) {
                orderedEVs.poll();
                ev.requestStation(s_infos);
            }


            System.out.println("Stations computing and sending initial offers...\n");
            int countOffers = 0; // if offers == 0 do not send messages
            for (int st = 0; st < stations.size(); st++) {
                AbstractOnlineStation station = stations.get(st);
                System.out.println("----------------- Station_" + station.getInfo().getId() + " ---------------------");
                station.transferBidders();
                if (station.hasOffers(slot)) {
                    System.out.println("Ev Bidders:");
                    System.out.println(station.printEVBidders());
                    System.out.println("Waiting:");
                    System.out.println(station.printEVWaiting());
                    this.computeInitialOffers(station);
                    countOffers++;
                } else {
                    System.out.println("Station has NO offers\n");
                    finished_stations[st] = true;
                }
            }
            for (int st = 0; st < stations.size(); st++) {
                System.out.println("----------------- Station_" + stations.get(st).getInfo().getId() + " ---------------------");
                if (countOffers > 0)
                    this.stationsSendOfferMessages(stations.get(st));
            }


            while (!checkFinished()) {
                System.out.println("\n\n2.1 Evs evaluate the offers");
                this.evsEvaluateOffers();
                // negotiations

                System.out.println("\n\n2.2 Stations compute new offers or update schedule");
                for (int s = 0; s < stations.size(); s++) {
                    System.out.println("----------------- Station_" + stations.get(s).getInfo().getId() + " ---------------------");
                    AbstractOnlineStation station = stations.get(s);
                    //if (station.hasOffers(slot)) {
                    this.stationCheckInWhile(station, s);
                    //}
                    if (!finished_stations[s])
                        this.stationsSendOfferMessages(station);
                }
            }


            System.out.println("\n\nStations updating their data...");
            for (int st = 0; st < stations.size(); st++) {
                AbstractOnlineStation station = stations.get(st);
                System.out.println("----------------- Station_" + station.getInfo().getId() + " ---------------------");
                if (station.isUpdate()) {
                    System.out.println("Full update");
                    station.updateStationData();
                } else {
                    System.out.println("Simple Update");
                    station.updateStationDataNoSchedule();
                }

            }
            System.out.println("\n\n\n\n");
            resetEVsRounds();
        }

        System.out.println("------------\n\nExecution is over!");
        System.out.println("These are the final schedules:");
        for (int s = 0; s < stations.size(); s++) {
            System.out.println("----------------- Station_" + stations.get(s).getInfo().getId() + " ---------------------");
            stations.get(s).printScheduleMap();
        }
    }

    private void resetFinishedStations() {
        finished_stations = new boolean[stations.size()];
        for (int st = 0; st < finished_stations.length; st++) {
            finished_stations[st] = false;
        }
    }

    private void resetEVsRounds () {
        for (EV ev: evs) {
            ev.resetRounds();
        }
    }

    private PriorityQueue<EV> orderEVs() {
        PriorityQueue<EV> orderedEVs = new PriorityQueue<>(10, new Comparator<EV>() {
            @Override
            public int compare(EV ev1, EV ev2) {
                return ev1.getInformSlot() - ev2.getInformSlot();
            }
        });
        for (EV ev : evs) {
            orderedEVs.offer(ev);
        }
        return orderedEVs;
    }

}
