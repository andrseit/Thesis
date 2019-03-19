package main;
import evs.Preferences;
import new_classes.ExecutionFlow;
import new_classes.Optimizer;
import new_classes.TestingClass;
import optimize.AlternativesCPLEX;
import station.EVObject;
import various.ArrayTransformations;

import java.util.ArrayList;

/**
 * Created by Darling on 28/8/2017.
 */
public class Main {

    public static void main(String[] args) {

        ExecutionFlow exe = new ExecutionFlow();
        exe.runOnline();

    }

    private static void testStrategy () {

        /*
        ArrayList<StationReceiver> receivers = new ArrayList<>();
        Station station = new Station(new StationInfo(0, 1, 2, 3));
        receivers.add(station.getCommunicationPort());
        EV ev = new EV(0, 0, 2, 2, 3, 3, 1, 5, 3, 1, 3, new Strategy(1, 1, 1, 1, 1, 1, "none"));
        ev.newRequest(receivers);
        station.printList();
        station.sendSuggestions();
        ev.printMessages();
        ev.sendAnswer(receivers);
        station.printList();
        */
    }

    private static void testAlternativeCP () {
        ArrayList<EVObject> list = new ArrayList<>();
        EVObject ev = new EVObject();
        ev.addEVPreferences(3, 5, 0, 3);
        list.add(ev);
        ev = new EVObject();
        ev.addEVPreferences(6, 8, 0, 3);
        list.add(ev);
        ev = new EVObject();
        ev.addEVPreferences(3, 7, 0, 5);
        list.add(ev);
        int[] chargers = {1, 1, 0, 0, 1, 1, 1, 1, 1, 1};
        Optimizer cp = new AlternativesCPLEX();
        ArrayTransformations t = new ArrayTransformations();
        t.printIntArray(cp.optimize(10, 0, list, chargers, new int[10]));
        t.printIntArray(cp.optimize(10, 1, list, chargers, new int[10]));
        t.printIntArray(cp.optimize(10, 2, list, chargers, new int[10]));
        t.printIntArray(cp.optimize(10, 3, list, chargers, new int[10]));
        t.printIntArray(cp.optimize(10, 4, list, chargers, new int[10]));
        t.printIntArray(cp.optimize(10, 5, list, chargers, new int[10]));
    }
}
