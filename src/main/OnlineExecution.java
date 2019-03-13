package main;

import evs.EV;
import io.JSONFileParser;
import station.offline.AbstractStation;
import station.online.AbstractOnlineStation;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class OnlineExecution extends Execution {

    private PriorityQueue<EV> orderedEVs;

    public OnlineExecution (boolean makeSuggestions) {
        super(makeSuggestions);
        online = true;
    }

    @Override
    protected void initialize() {
        JSONFileParser parser = new JSONFileParser();
        stations = new ArrayList<>();
        for (AbstractOnlineStation station: parser.readOnlineStations("station.json")) {
            stations.add(station);
        }

        s_infos = new ArrayList<>();
        for (AbstractStation station: stations) {
            s_infos.add(station.getInfo());
        }
        this.slotsNumber = parser.getSlotsNumber();

        evs = parser.readEVsData("evs.json");
        orderedEVs = this.orderEVs();
        times = new double[stations.size()][4][slotsNumber];
        //printEVs();
    }

    @Override
    public void execute() {

        //System.out.println("Online Execution is starting. Initializing...");
        this.initialize();

        for (int slot = 0; slot < slotsNumber; slot++) {
            //System.out.println("------> Slot: " + slot);
            this.resetFinishedStations();
            for (int st = 0; st < stations.size(); st++) {
                AbstractOnlineStation station = (AbstractOnlineStation) stations.get(st);
                station.setCurrentSlot(slot);
            }

            //System.out.println("EVs request from the stations:\n");
            EV ev;
            // if there are no evs
            while ((ev = orderedEVs.peek()) != null && ev.getInformSlot() == slot) {
                orderedEVs.poll();
                ev.requestStation(s_infos, online);
            }


            //System.out.println("Stations computing and sending initial offers...\n");
            int countOffers = 0; // if offers == 0 do not send messages
            for (int st = 0; st < stations.size(); st++) {
                AbstractOnlineStation station = (AbstractOnlineStation) stations.get(st);
                //System.out.println("----------------- Station_" + station.getInfo().getId() + " ---------------------");
                station.transferBidders();
                if (station.hasOffers(slot)) {
                    //System.out.println("Ev Bidders:");
                    //System.out.println(station.printEVBidders());
                    //System.out.println("Waiting:");
                    //System.out.println(station.printEVWaiting());
                    timer.startTimer();
                    this.computeInitialOffers(station);
                    timer.stopTimer();
                    countOffers++;
                } else {
                    //System.out.println("Station has NO offers\n");
                    finished_stations[st] = true;
                }
                times[st][0][slot] = timer.getMillis();
            }
            for (int st = 0; st < stations.size(); st++) {
                //System.out.println("----------------- Station_" + stations.get(st).getInfo().getId() + " ---------------------");
                if (countOffers > 0)
                    this.stationsSendOfferMessages(stations.get(st));
            }


            while (!checkFinished()) {
                //System.out.println("\n\n2.1 Evs evaluate the offers");
                this.evsEvaluateOffers();
                // negotiations

               //System.out.println("\n\n2.2 Stations computeSuggestions new offers or update schedule");
                for (int s = 0; s < stations.size(); s++) {
                    //System.out.println("----------------- Station_" + stations.get(s).getInfo().getId() + " ---------------------");
                    AbstractOnlineStation station = (AbstractOnlineStation) stations.get(s);
                    //if (station.hasOffers(slot)) {
                    timer.startTimer();
                    this.stationCheckInWhile(station, s);
                    timer.stopTimer();
                    times[s][1][slot] = timer.getMillis();
                    times[s][2][slot] = station.getNegotiators();
                    times[s][3][slot] = station.getRoundsCount();
                    //}
                    if (!finished_stations[s])
                        this.stationsSendOfferMessages(station);
                }
            }


            //System.out.println("\n\nStations updating their data...");
            for (int st = 0; st < stations.size(); st++) {
                AbstractOnlineStation station = (AbstractOnlineStation) stations.get(st);
                //System.out.println("----------------- Station_" + station.getInfo().getId() + " ---------------------");
                if (station.isUpdate()) {
                    //System.out.println("Full update");
                    station.updateStationData();
                } else {
                    //System.out.println("Simple Update");
                    station.updateStationDataNoSchedule();
                }
                station.updateBeforeNextSlot();
            }
            //System.out.println("\n\n\n\n");
            resetEVsRounds();
        }

        /*
        System.out.println("------------\n\nExecution is over!");
        System.out.println("These are the final schedules:");
        for (int s = 0; s < stations.size(); s++) {
            System.out.println("----------------- Station_" + stations.get(s).getInfo().getId() + " ---------------------");
            stations.get(s).printScheduleMap();
        }
        */
    }

    private void resetFinishedStations() {
        finished_stations = new boolean[stations.size()];
        for (int st = 0; st < finished_stations.length; st++) {
            finished_stations[st] = false;
        }
    }

    private void resetEVsRounds() {
        for (EV ev : evs) {
            ev.resetRounds();
        }
    }

    private PriorityQueue<EV> orderEVs() {
        PriorityQueue<EV> orderedEVs = new PriorityQueue<>(10, (ev1, ev2) -> ev1.getInformSlot() - ev2.getInformSlot());
        for (EV ev : evs) {
            orderedEVs.offer(ev);
        }
        return orderedEVs;
    }

}
