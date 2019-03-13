package evs;

import evs.strategy.Strategy;
import evs.communication.EVPDA;
import new_classes.Station;
import station.communication.StationReceiver;
import evs.communication.EVReceiver;
import station.StationInfo;
import station.SuggestionMessage;
import various.IntegerConstants;

import java.util.ArrayList;
import java.util.HashMap;

public class EV {

    private EVPDA pda;
    // maybe this should go to the pda
    private ArrayList<SuggestionMessage> messages;

    private int informSlot;
    private EVInfo info;
    private Strategy strategy;
    private int bid;



    public EV(int id, int informSlot, int x, int y, int finalX, int finalY, int start, int end, int energy, int bid, int max_distance, Strategy strategy) {
        messages = new ArrayList<>();
        pda = new EVPDA(messages);
        this.informSlot = informSlot;
        info = new EVInfo(id, x, y, finalX, finalY, start, end, energy, bid, max_distance);
        info.setObjectAddress(this);
        info.setCommunicationPort(pda.getMessenger().getReceiver());
        this.bid = bid;
        this.strategy = strategy;
    }

    /*
    public void addSuggestion(SuggestionMessage suggestion) {
            strategy.addSuggestion(suggestion);
    }
    */

    public void printMessagesList () {
        System.out.println("EV No. " + info.getId() + " will evaluate suggestions: ");
        for (SuggestionMessage message: messages) {
            System.out.println("\t*" + message);
        }
    }

    public void evaluateSuggestions() {
        strategy.evaluate(messages, info);
    }

    public boolean hasSuggestions() {
        return !messages.isEmpty();
    }

    /**
     * the name means that this is a new method for the request
     * ev gets the station list and sends a request
     * make it more sophisticated like the old one in the future
     * @param stations
     */
    public void newRequest (StationReceiver[] stations) {
        for (StationReceiver s: stations) {
            System.out.println("EV No. " + info.getId() + " requests from Station No. " + s.getStationId());
            pda.sendRequest(info, IntegerConstants.EV_MESSAGE_REQUEST, s);
        }
    }

    public void sendAnswers () {
        HashMap<StationInfo, Integer> answers = strategy.getAnswers();
        if (!answers.isEmpty()) {
            for (StationInfo s : answers.keySet()) {
                pda.sendRequest(info, answers.get(s), s.getCommunicationPort());
            }
        }
    }

    public void requestStation(ArrayList<StationInfo> stations, boolean online) {

        boolean requested = false;
        int maxDistance = info.getPreferences().getMaxDistance();
        int minDistance = Integer.MAX_VALUE;
        if (online)
            minDistance = info.getPreferences().getStart() - informSlot; // it is the distance between inform and start - how soon it must charger - e.g. if
        // the distance is 3 but the minDistance is 2, the station won't be selected
        // how many slots i need to start charging at the start time
        while (!requested) {
            for (StationInfo s_info : stations) {
                int distance = this.computeDistance(info.getLocationX(), info.getLocationY(),
                        s_info.getLocationX(), s_info.getLocationY());

                if (distance <= Math.min(minDistance, maxDistance))
                    requested = true;
            }
            if (!requested) {
                maxDistance++;
                if (maxDistance > minDistance)
                    break;
            }
        }
    }

    private int computeDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public void printSuggestionsList() {
        System.out.println("ev_" + info.getId() + "'s list:");
        //strategy.printSuggestionsList();
    }

    public int getInformSlot() {
        return informSlot;
    }

    public String toString() {
        return "**************" + "\n" + info.toString() + "\n\t*" + strategy.toString() + "\n\t* Informs: " + "\n\t\tSlot: " + informSlot;
    }

    public void resetRounds() {
        strategy.resetRounds();
    }

    public EVReceiver getCommunicationPort () {
        return pda.getMessenger().getReceiver();
    }

    public void printMessages () {
        for (SuggestionMessage m: messages) {
            System.out.println(m.toString());
        }
    }

    public void sendAnswer (ArrayList<StationReceiver> stations) {

        for (StationReceiver s: stations) {
            pda.sendRequest(info, IntegerConstants.EV_EVALUATE_REJECT, s);
        }
    }

}
