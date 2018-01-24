package evs;

import evs.strategy.Strategy;
import station.StationInfo;
import station.SuggestionMessage;

import java.util.ArrayList;

/**
 * Created by Thesis on 8/12/2017.
 */
public class EV {

    private int informSlot;
    private EVInfo info;
    private Strategy strategy;
    private int bid;

    public EV (int id, int informSlot, int x, int y, int finalX, int finalY, int start, int end, int energy, int bid, int max_distance, Strategy strategy) {
        this.informSlot = informSlot;
        info = new EVInfo(id, x, y, finalX, finalY, start, end, energy, bid, max_distance);
        info.setObjectAddress(this);
        this.bid = bid;
        this.strategy = strategy;
    }

    public void addSuggestion (SuggestionMessage suggestion) {
        strategy.addSuggestion(suggestion);
    }

    public void evaluateSuggestions () {
        strategy.evaluate(info);
    }

    public boolean hasSuggestions() { return !strategy.isEmpty(); }

    public void requestStation (ArrayList<StationInfo> stations) {

        for (StationInfo s_info : stations) {
            int distance = this.computeDistance(info.getLocationX(), info.getLocationY(),
                    s_info.getLocationX(), s_info.getLocationY());

            if (distance <= info.getPreferences().getMaxDistance()) {
                s_info.request(info);
                System.out.println("ev_" + info.getId() + " requested from station_" + s_info.getId());
            }
        }
    }

    private int computeDistance (int x1, int y1, int x2, int y2) {
        return Math.abs(x1-x2) + Math.abs(y1-y2);
    }

    public void printSuggestionsList () {
        System.out.println("ev_" + info.getId() + "'s list:");
        strategy.printSuggestionsList();
    }

    public int getInformSlot() {
        return informSlot;
    }

    public String toString () {
        return info.toString() + "\n\t*" + strategy.toString();
    }

}
