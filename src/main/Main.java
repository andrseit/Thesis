package main;

import agents.evs.EV;
import agents.station.Station;
import io.DataGenerator;
import io.JSONFileParser;

import java.util.ArrayList;

/**
 * Created by Darling on 28/8/2017.
 */
public class Main {

    public static void main(String[] args) {

        ExecutionFlow exe = new ExecutionFlow(false, false);
        exe.runOffline();
        /*
        JSONFileParser parser = new JSONFileParser();
        DataGenerator gen = new DataGenerator(5, 5, 10, 2);
        gen.generateRandomStations(5);
        gen.generateEVsFile(2, 5, 1.8, 1.8);
        ArrayList<Station> stations = parser.readStations("station.json");
        ArrayList<EV> evs = parser.readEVsData("evs.json");
        evs.forEach(System.out::println);
        stations.forEach(System.out::println);
         */
    }

    private static void testStrategy () {

        /*
        ArrayList<StationReceiver> receivers = new ArrayList<>();
        Station agents.station = new Station(new StationInfo(0, 1, 2, 3));
        receivers.add(agents.station.getCommunicationPort());
        EV ev = new EV(0, 0, 2, 2, 3, 3, 1, 5, 3, 1, 3, new Strategy(1, 1, 1, 1, 1, 1, "none"));
        ev.newRequest(receivers);
        agents.station.printList();
        agents.station.sendSuggestions();
        ev.printMessages();
        ev.sendAnswer(receivers);
        agents.station.printList();
        */
    }

}
