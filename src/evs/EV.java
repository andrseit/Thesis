package evs;

import evs.strategy.Strategy;
import station.StationInfo;
import station.SuggestionMessage;

import java.util.ArrayList;

public class EV {

    private int informSlot;
    private EVInfo info;
    private Strategy strategy;
    private int bid;

    public EV(int id, int informSlot, int x, int y, int finalX, int finalY, int start, int end, int energy, int bid, int max_distance, Strategy strategy) {
        this.informSlot = informSlot;
        info = new EVInfo(id, x, y, finalX, finalY, start, end, energy, bid, max_distance);
        info.setObjectAddress(this);
        this.bid = bid;
        this.strategy = strategy;
    }

    public void addSuggestion(SuggestionMessage suggestion) {
            strategy.addSuggestion(suggestion);
    }

    public void evaluateSuggestions() {
        strategy.evaluate(info);
    }

    public boolean hasSuggestions() {
        return !strategy.isEmpty();
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


                if (distance <= Math.min(minDistance, maxDistance)) {
                    s_info.request(info);
                    //System.out.println("ev_" + info.getId() + " requests from station_" + s_info.getId());
                    requested = true;
                }
            }
            if (!requested) {
                maxDistance++;
                if (maxDistance > minDistance)
                    break;
            }
        }
        //if (requested)
            //System.out.println();
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
        return info.toString() + "\n\t*" + strategy.toString();
    }

    public void resetRounds() {
        strategy.resetRounds();
    }

}
