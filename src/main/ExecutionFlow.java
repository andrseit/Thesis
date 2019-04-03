package main;

import agents.evs.EV;
import io.DataGenerator;
import io.JSONFileParser;
import agents.station.Station;
import agents.station.communication.StationReceiver;
import various.IntegerConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Created by Thesis on 13/3/2019.
 */
public class ExecutionFlow {

    ArrayList<Station> stations;
    ArrayList<EV> evs;
    int slotsNumber;

    public ExecutionFlow (boolean generateStations, boolean generateEVs) {
        JSONFileParser parser = new JSONFileParser();
        DataGenerator gen = new DataGenerator(2, 5, 10, 2);
        if (generateStations)
            gen.generateRandomStations(1);
        else
            gen.readStationFile();
        if (generateEVs)
            gen.generateEVsFile(2, 5, 1.8, 1.8);


        stations = parser.readStations("station.json");
        stations.forEach(System.out::println);
        evs = parser.readEVsData("evs.json");
        slotsNumber = gen.getSlotsNumber();
        //agents.evs.forEach(System.out::println);
    }

    public void runOffline () {
        offline(stations, evs, 0);
    }

    public void runOnline () {
        online(stations, evs, slotsNumber);
    }

    private void offline (ArrayList<Station> stations, ArrayList<EV> evs, int currentSlot) {
        // a simulation should have a board with the stations' communication ports, so that agents.evs are able to send them their requests
        StationReceiver[] communicationPorts = new StationReceiver[stations.size()];
        boolean[] finishedStations = new boolean[stations.size()];
        for (int s = 0; s < stations.size(); s++) {
            communicationPorts[s] = stations.get(s).getCommunicationPort();
            finishedStations[s] = false;
            stations.get(s).setCurrentSlot(currentSlot);
        }

        // now each ev searches in the table of communication ports and sends requests
        //System.out.println("\n----- EVs are searching for available stations -----");
        for (EV ev: evs) {
            if (!ev.isToBeServiced())
                ev.newRequest(communicationPorts);
            // if an ev has already made an agreement with a agents.station then check if it
            // is up to a delay
            else {
                int delayStatus = ev.checkDelay(currentSlot, slotsNumber);
                if (delayStatus == IntegerConstants.EV_UPDATE_DELAY) {
                    ev.computeDelay(currentSlot, slotsNumber);
                    ev.sendDeferralMessage();
                    // make a function to inform the agents.station
                } else if (delayStatus == IntegerConstants.EV_UPDATE_CANCEL) {
                    ev.sendCancellationMessage();
                }
            }
        }
        //System.out.println("-----------------------------------");


        // now stations will computeSuggestions the schedule based on their list
        //System.out.println("\n----- Stations are computing the schedules and sending suggestions -----");
        for (Station station: stations) {
            station.handleAnswers();
            System.out.println(station.getState().getStates(currentSlot));
            // the agents.evs have requested, when agents.evs request from a agents.station, the agents.station has to insert them into a list
            // please make sure that you take care of that
            /*
            agents.station.printList();
            */
            station.computeSuggestions();



            station.printTemporaryScheduleMap();



            // don't forget to create temporary used charged (allocated but not yet accepted)

            // after the stations have computed their schedules they should send their offers to the agents.evs
            station.sendSuggestions();
        }

        // agents.evs will evaluate the stations' suggestions
        System.out.println("\n----- EVs are evaluating the suggestions -----");
        for (EV ev: evs) {
            if (ev.hasSuggestions()) {
                //ev.printMessagesList();
                ev.evaluateSuggestions();
                ev.sendAnswers();
                System.out.println(ev.getState());
            }
        }

        // stations will handle the answers
        for (int station = 0; station < stations.size(); station++) {
            Station current = stations.get(station);
            current.handleAnswers();
            System.out.println(current.getState().getStates(currentSlot));
            //System.out.println("----------- Accepted EVs ------------");
            //current.printAcceptedEVs();

            //System.out.println("----------- Waiting EVs ------------");
            //current.printList();


            current.printScheduleMap();

            if (current.hasFinished()) {
                finishedStations[station] = true;
                System.out.println("Station No " + station + " has no more requests!");
            }
        }


        while (!checkFinishedStations(finishedStations)) {
            // stations computeSuggestions alternative suggestions and send to the EVs
            // however some agents.station's may have
            for (Station current : stations) {
                if (!current.hasFinished()) {
                    current.computeSuggestions();
                    current.computeAlternatives();

                    current.printTemporaryScheduleMap();

                    current.sendSuggestions();
                }
            }

            System.out.println("\n----- EVs are evaluating the alternatives -----");
            for (EV ev : evs) {
                if (ev.hasSuggestions()) {
                    //ev.printMessagesList();
                    ev.evaluateSuggestions();
                    ev.sendAnswers();
                    System.out.println(ev.getState());
                }
            }

            // stations will handle the answers
            for (int station = 0; station < stations.size(); station++) {
                Station current = stations.get(station);
                if (!current.hasFinished()) {
                    current.handleAnswers();
                    System.out.println(current.getState().getStates(currentSlot));
                    /*
                    System.out.println("----------- Accepted EVs ------------");
                    current.printAcceptedEVs();

                    System.out.println("----------- Waiting EVs ------------");
                    current.printList();
                    */

                    current.printScheduleMap();


                    if (current.hasFinished()) {
                        finishedStations[station] = true;
                        System.out.println("Station No " + station + " has no more requests!");
                    }
                }
            }
        }

        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        for (int s = 0; s < stations.size(); s++) {
            Station station = stations.get(s);
            System.out.println("Station " + s);
            System.out.println(station.getStatistics() + "\n");
        }
        System.out.println("################################################");
    }

    private void online (ArrayList<Station> stations, ArrayList<EV> evs, int slotsNumber) {
        // for online execution find EVs that their inform slot is equal to the current slot
        // add them in the EVs list and run the offline mode
        // don't forget to add a lower bound to the computation of the alternatives

        // order the EVs list by inform slot
        evs.sort(Comparator.comparingInt(o -> o.getInformSlot()));
        evs.forEach(System.out::println);
        Scanner scanner = new Scanner(System.in);
        for (int slot = 0; slot < slotsNumber; slot++) {
            System.out.println("::::::::::::::::: Slot No " + slot + " :::::::::::::::::");
            // the list which contains the EVs that inform the stations in the current slot
            ArrayList<EV> currentEVs = new ArrayList<>();
            for (EV ev: evs) {
                if (ev.getInformSlot() == slot || (!ev.isServiced() && ev.isToBeServiced())) // ev.isServiced() is redundant but I put it for safety
                    currentEVs.add(ev);
            }
            if (!currentEVs.isEmpty())
                offline(stations, currentEVs, slot);
            //scanner.nextLine();
        }
    }

    private static boolean checkFinishedStations (boolean[] stationStatus) {
        for (boolean status : stationStatus)
            if (!status)
                return false;
        return true;
    }

}
