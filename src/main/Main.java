package main;

/**
 * Created by Darling on 28/8/2017.
 */
public class Main {

    public static void main(String[] args) {

        ExecutionFlow exe = new ExecutionFlow(false, true);
        exe.runOnline();
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
