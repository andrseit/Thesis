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
    }

    @Override
    public void execute() {

        this.initialize();

        for (int slot = 0; slot < slotsNumber; slot++) {
            System.out.println("------> Slot: " + slot);
            this.resetFinishedStations();

            EV ev;
            while ((ev = orderedEVs.peek()) != null && ev.getInformSlot() == slot) {
                orderedEVs.poll();
                ev.requestStation(s_infos);
            }

            for (int st = 0; st < stations.size(); st++) {
                AbstractOnlineStation station = stations.get(st);
                if (!station.hasOffers(slot)) {
                    finished_stations[st] = true;
                } else {
                    this.sendInitialOffers(station);
                }
            }

            /*
            this.evsEvaluateOffers();
            for (int s = 0; s < stations.size(); s++) {
                this.stationCheckInWhile(stations.get(s), s);
            }
            */
            while (!checkFinished()) {
                this.evsEvaluateOffers();
                // negotiations
                for (int s = 0; s < stations.size(); s++) {
                    this.stationCheckInWhile(stations.get(s), s);
                }
            }

            for (int st = 0; st < stations.size(); st++) {
                AbstractOnlineStation station = stations.get(st);
                if (station.isUpdate()) {
                    System.out.println("Updating");
                    station.updateStationData();
                }
            }

        }
    }

    private void resetFinishedStations () {
        finished_stations = new boolean[stations.size()];
        for (int st = 0; st < finished_stations.length; st++) {
            finished_stations[st] = false;
        }
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

}
