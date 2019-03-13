package new_classes;

import evs.EV;
import io.DataGenerator;
import io.JSONFileParser;
import station.communication.StationReceiver;

import java.util.ArrayList;

/**
 * Created by Thesis on 13/3/2019.
 */
public class ExecutionFlow {

    ArrayList<Station> stations;
    ArrayList<EV> evs;
    int slotsNumber;

    public ExecutionFlow () {
        DataGenerator gen = new DataGenerator(2, 5, 10, 2);
        //gen.generateRandomStations(2);
        //gen.generateEVsFile(2, 5, 1.8, 1.8);

        JSONFileParser parser = new JSONFileParser();
        stations = parser.readStations("station.json");
        stations.forEach(System.out::println);
        evs = parser.readEVsData("evs.json");
        slotsNumber = gen.getSlotsNumber();
        evs.forEach(System.out::println);
    }

    public void runOffline () {
        offline(stations, evs, 0);
    }

    public void runOnline () {
        online(stations, evs, slotsNumber);
    }

    private void offline (ArrayList<Station> stations, ArrayList<EV> evs, int currentSlot) {
        // a simulation should have a board with the stations' communication ports, so that evs are able to send them their requests
        StationReceiver[] communicationPorts = new StationReceiver[stations.size()];
        boolean[] finishedStations = new boolean[stations.size()];
        for (int s = 0; s < stations.size(); s++) {
            communicationPorts[s] = stations.get(s).getCommunicationPort();
            finishedStations[s] = false;
            stations.get(s).setCurrentSlot(currentSlot);
        }

        // now each ev searches in the table of communication ports and sends requests
        System.out.println("\n----- EVs are searching for available stations -----");
        for (EV ev: evs) {
            ev.newRequest(communicationPorts);
        }
        System.out.println("-----------------------------------");

        // the evs have requested, when evs request from a station, the station has to insert them into a list
        // please make sure that you take care of that
        stations.forEach(s -> {
            s.printList();
        });

        // now stations will computeSuggestions the schedule based on their list
        System.out.println("\n----- Stations are computing the schedules and sending suggestions -----");
        for (Station station: stations) {
            station.computeSuggestions();
            System.out.println("-------------------------------------");
            station.printTemporaryScheduleMap();
            System.out.println("-----------Temporary Chargers---------");
            station.printTemporaryChargers();
            System.out.println("-------------------------------------");
            // don't forget to create temporary used charged (allocated but not yet accepted)

            // after the stations have computed their schedules they should send their offers to the evs
            station.sendSuggestions();
        }

        // evs will evaluate the stations' suggestions
        System.out.println("\n----- EVs are evaluating the suggestions -----");
        for (EV ev: evs) {
            if (ev.hasSuggestions()) {
                ev.printMessagesList();
                ev.evaluateSuggestions();
                ev.sendAnswers();
            }
        }

        // stations will handle the answers
        for (int station = 0; station < stations.size(); station++) {
            Station current = stations.get(station);
            current.handleAnswers();
            System.out.println("----------- Accepted EVs ------------");
            current.printAcceptedEVs();

            System.out.println("----------- Waiting EVs ------------");
            current.printList();

            System.out.println("----------- Main Schedule Map ------------");
            current.printScheduleMap();
            System.out.println("----------- ------------ ------------");
            if (current.hasFinished()) {
                finishedStations[station] = true;
                System.out.println("Station No " + station + " has no more requests!");
            }
        }


        while (!checkFinishedStations(finishedStations)) {
            // stations computeSuggestions alternative suggestions and send to the EVs
            // however some station's may have
            for (int station = 0; station < stations.size(); station++) {
                Station current = stations.get(station);
                if (!current.hasFinished()) {
                    current.computeAlternatives();
                    current.sendSuggestions();
                }
            }

            System.out.println("\n----- EVs are evaluating the alternatives -----");
            for (EV ev : evs) {
                if (ev.hasSuggestions()) {
                    ev.printMessagesList();
                    ev.evaluateSuggestions();
                    ev.sendAnswers();
                }
            }

            // stations will handle the answers
            for (int station = 0; station < stations.size(); station++) {
                Station current = stations.get(station);
                if (!current.hasFinished()) {
                    current.handleAnswers();
                    System.out.println("----------- Accepted EVs ------------");
                    current.printAcceptedEVs();

                    System.out.println("----------- Waiting EVs ------------");
                    current.printList();

                    System.out.println("----------- Main Schedule Map ------------");
                    current.printScheduleMap();
                    System.out.println("----------- ------------ ------------");
                    if (current.hasFinished()) {
                        finishedStations[station] = true;
                        System.out.println("Station No " + station + " has no more requests!");
                    }
                }
            }
        }
    }

    private void online (ArrayList<Station> stations, ArrayList<EV> evs, int slotsNumber) {
        // for online execution find EVs that their inform slot is equal to the current slot
        // add them in the EVs list and run the offline mode
        // don't forget to add a lower bound to the computation of the alternatives

        // order the EVs list by inform slot
        evs.sort((o1, o2) -> o1.getInformSlot() - o2.getInformSlot());
        evs.forEach(System.out::println);
        for (int slot = 0; slot < slotsNumber; slot++) {
            System.out.println("::::::::::::::::: Slot No " + slot + " :::::::::::::::::");
            // the list which contains the EVs that inform the stations in the current slot
            ArrayList<EV> currentEVs = new ArrayList<>();
            for (EV ev: evs) {
                if (ev.getInformSlot() == slot)
                    currentEVs.add(ev);
            }
            if (!currentEVs.isEmpty())
                offline(stations, currentEVs, slot);
        }
    }

    private static boolean checkFinishedStations (boolean[] stationStatus) {
        for (int s = 0; s < stationStatus.length; s++)
            if (stationStatus[s] == false)
                return false;
        return true;
    }

}
