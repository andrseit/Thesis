package main;

import agents.evs.EV;
import agents.station.statistics.StationStatistics;
import io.JSONFileParser;
import agents.station.Station;
import agents.station.communication.StationReceiver;
import agents.evs.communication.EVMessage;
import io.StatisticsWriter;
import main.experiments.parameters.SystemParameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Created by Thesis on 13/3/2019.
 */
public class ExecutionFlow {

    private final String statisticsPath = "files/statistics.csv";

    private ArrayList<Station> stations;
    private ArrayList<EV> evs;
    private int slotsNumber;

    private boolean useDelays;

    // first generate stations, later the evs - because of bad design - gonna fix it
    public ExecutionFlow(String stationsPath, String evsPath, String systemPath) {
        JSONFileParser parser = new JSONFileParser();
        SystemParameters systemParameters = parser.readSystemParameters(systemPath);

        //stations = parser.readStations(stationsPath);
        stations = parser.parseStations(stationsPath);
        stations.forEach(System.out::println);

        //evs = parser.readEVsData(evsPath);
        evs = parser.parseEVs(evsPath);
        slotsNumber = systemParameters.getSlotsNumber();
        useDelays = true;
        evs.forEach(System.out::println);
        deleteStatisticsFile();
    }

    public void runOffline() {
        offline(stations, evs, 0);
    }

    public void runOnline() {
        online(stations, evs, slotsNumber);
    }

    private void offline(ArrayList<Station> stations, ArrayList<EV> evs, int currentSlot) {
        // a simulation should have a board with the stations' communication ports, so that agents.evs are able to send them their requests
        StationReceiver[] communicationPorts = new StationReceiver[stations.size()];
        boolean[] finishedStations = new boolean[stations.size()];
        for (int s = 0; s < stations.size(); s++) {
            communicationPorts[s] = stations.get(s).getCommunicationPort();
            finishedStations[s] = false;
            stations.get(s).getStrategy().setCurrentSlot(currentSlot);
        }

        // now each ev searches in the table of communication ports and sends requests
        //System.out.println("\n----- EVs are searching for available stations -----");
        for (EV ev : evs) {
            if (!ev.getStrategy().isToBeServiced())
                for (StationReceiver c : communicationPorts)
                    ev.getMessenger().sendMessage(ev.getInfo(), EVMessage.EV_MESSAGE_REQUEST, c);

                // if an ev has already made an agreement with a agents.station then check if it
                // is up to a delay
            else {
                if (useDelays) {
                    EVMessage delayStatus = ev.getStrategy().checkDelay(ev.getInfo(), currentSlot, slotsNumber);
                    if (delayStatus == EVMessage.EV_UPDATE_DELAY) {
                        ev.getStrategy().computeDelay(ev.getInfo(), slotsNumber);
                        ev.getMessenger().sendMessage(ev.getInfo(), EVMessage.EV_UPDATE_DELAY, ev.getStrategy().getAcceptedStation());
                        // make a function to inform the agents.station
                    } else if (delayStatus == EVMessage.EV_UPDATE_CANCEL) {
                        ev.getMessenger().sendMessage(ev.getInfo(), EVMessage.EV_UPDATE_CANCEL, ev.getStrategy().getAcceptedStation());
                    }
                }
            }
        }
        //System.out.println("-----------------------------------");


        // now stations will computeSuggestions the schedule based on their list
        System.out.println("\n----- Stations are computing the schedules and sending suggestions -----");
        for (Station station : stations) {
            station.getStrategy().handleAnswers(station.getMessenger());
            System.out.println(station.getState().getStates(currentSlot));
            // the agents.evs have requested, when agents.evs request from a agents.station, the agents.station has to insert them into a list
            // please make sure that you take care of that
            /*
            agents.station.printList();
            */
            station.getStrategy().computeSuggestions(station.getInfo());

            station.getState().printTemporaryScheduleMap();


            // don't forget to create temporary used charged (allocated but not yet accepted)

            // after the stations have computed their schedules they should send their offers to the agents.evs
            station.getMessenger().sendSuggestions(station.getStrategy().getSuggestionReceivers());
        }

        // agents.evs will evaluate the stations' suggestions
        System.out.println("\n----- EVs are evaluating the suggestions -----");
        for (EV ev : evs) {
            if (ev.hasSuggestions()) {
                //ev.printMessagesList();
                ev.getStrategy().evaluate(ev.getMessenger().getMessages(), ev.getInfo());
                ev.getMessenger().sendAnswers(ev.getInfo(), ev.getStrategy().getAnswers());
                System.out.println(ev.getState());
            }
        }

        // stations will handle the answers
        for (int station = 0; station < stations.size(); station++) {
            Station current = stations.get(station);
            current.getStrategy().handleAnswers(current.getMessenger());
            System.out.println(current.getState().getStates(currentSlot));
            //System.out.println("----------- Accepted EVs ------------");
            //current.printAcceptedEVs();

            //System.out.println("----------- Waiting EVs ------------");
            //current.printList();

            current.getState().printScheduleMap();

            if (current.getStrategy().hasFinished()) {
                finishedStations[station] = true;
                System.out.println("Station No " + station + " has no more requests!");
            }
        }


        while (!checkFinishedStations(finishedStations)) {
            // stations computeSuggestions alternative suggestions and send to the EVs
            // however some agents.station's may have
            for (Station current : stations) {
                if (!current.getStrategy().hasFinished()) {
                    current.getStrategy().computeSuggestions(current.getInfo());

                    // based on the strategy compute alternatives or not
                    if (current.getStrategy().isUseAlternatives())
                        current.getStrategy().computeAlternatives(current.getInfo());

                    current.getState().printTemporaryScheduleMap();

                    current.getMessenger().sendSuggestions(current.getStrategy().getSuggestionReceivers());
                }
            }

            System.out.println("\n----- EVs are evaluating the alternatives -----");
            for (EV ev : evs) {
                if (ev.hasSuggestions()) {
                    //ev.printMessagesList();
                    ev.getStrategy().evaluate(ev.getMessenger().getMessages(), ev.getInfo());
                    ev.getMessenger().sendAnswers(ev.getInfo(), ev.getStrategy().getAnswers());
                    System.out.println(ev.getState());
                }
            }

            // stations will handle the answers
            for (int station = 0; station < stations.size(); station++) {
                Station current = stations.get(station);
                if (!current.getStrategy().hasFinished()) {
                    current.getStrategy().handleAnswers(current.getMessenger());
                    //System.out.println(current.getState().getStates(currentSlot));
                    /*
                    System.out.println("----------- Accepted EVs ------------");
                    current.printAcceptedEVs();

                    System.out.println("----------- Waiting EVs ------------");
                    current.printList();
                    */

                    current.getState().printScheduleMap();


                    if (current.getStrategy().hasFinished()) {
                        finishedStations[station] = true;
                        System.out.println("Station No " + station + " has no more requests!");
                    }
                }
            }
        }

        System.out.println("^^^^^^^^^^^^^^^^^^^^ Station Stats ^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        for (int s = 0; s < stations.size(); s++) {
            Station station = stations.get(s);
            System.out.println("Station " + s);
            System.out.println(station.getStatistics().getSlotStatistics(currentSlot) + "\n");
            System.out.println(station.getStatistics().getSlotStatistics(currentSlot).toCSV());
        }
        System.out.println("################################################");
    }

    private void online(ArrayList<Station> stations, ArrayList<EV> evs, int slotsNumber) {
        // for online execution find EVs that their inform slot is equal to the current slot
        // add them in the EVs list and run the offline mode
        // don't forget to add a lower bound to the computation of the alternatives

        // order the EVs list by inform slot
        evs.sort(Comparator.comparingInt(o -> o.getStrategy().getStrategyPreferences().getInformSlot()));
        //evs.forEach(System.out::println);
        Scanner scanner = new Scanner(System.in);
        for (int slot = 0; slot < slotsNumber; slot++) {
            System.out.println("::::::::::::::::: Slot No " + slot + " :::::::::::::::::");
            // the list which contains the EVs that inform the stations in the current slot
            ArrayList<EV> currentEVs = new ArrayList<>();
            for (EV ev : evs) {
                if (ev.getStrategy().getStrategyPreferences().getInformSlot() == slot || (!ev.getStrategy().isServiced() && ev.getStrategy().isToBeServiced())) // ev.isServiced() is redundant but I put it for safety
                    currentEVs.add(ev);
            }
            if (!currentEVs.isEmpty())
                offline(stations, currentEVs, slot);
            //scanner.nextLine();
        }
    }

    private static boolean checkFinishedStations(boolean[] stationStatus) {
        for (boolean status : stationStatus)
            if (!status)
                return false;
        return true;
    }

    private boolean deleteStatisticsFile() {
        File file = new File(statisticsPath);
        return file.delete();

    }

    public void useDelays (boolean useDelays) { this.useDelays = useDelays; }

    /**
     * Returns a list with the statistics for each station
     * @return
     */
    public ArrayList<StationStatistics> getStationStatistics () {
        ArrayList<StationStatistics> stationStatistics = new ArrayList<>();
        for (Station station: stations)
            stationStatistics.add(station.getStatistics());
        return stationStatistics;
    }
}
