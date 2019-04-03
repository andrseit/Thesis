package user_interface;

import java.util.ArrayList;

/**
 * Created by Thesis on 2/4/2019.
 *
 * For testing, keeps the state of the agents.station
 * which agents.evs he is communicating with and what are their answers
 */
public class StationState {

    private String station;
    private ArrayList<EVView>[] evs;

    public StationState (int stationID, int slotsNumber) {
        station = "agents/station" + stationID;
        evs = new ArrayList[slotsNumber];
    }

    public void addStateEV (int currentSlot, int evID, String state, String preferences) {
        String evIDStr = "ev" + evID;
        if (evs[currentSlot] == null) {
            evs[currentSlot] = new ArrayList<>();
        }
        int evIndex = getEVIndex(evs[currentSlot], evIDStr);
        if (evIndex != -1){
            EVView currentEV = evs[currentSlot].get(evIndex);
            currentEV.setState(currentEV.getState() + "->" + state);
            currentEV.setPreferences(currentEV.getPreferences() + "->" + "(" + preferences + ")");
        } else
            evs[currentSlot].add(new EVView(evIDStr, state, "(" + preferences + ")"));
    }

    public String getState (int currentSlot) {
        StringBuilder str = new StringBuilder();
        for (EVView ev: evs[currentSlot]) {
            str.append(ev).append("\n");
        }
        return str.toString();
    }

    public String getStates (int currentSlot) {
        StringBuilder str = new StringBuilder("|------------- " + station + " ---------------------\n\n");
        for (int s = 0; s < currentSlot + 1; s++) {
            str.append("|\t*Slot: ").append(s).append("\n");
            if (evs[s] != null) {
                for (EVView ev : evs[s]) {
                    str.append("|\t>").append(ev).append("\n");
                }
            } else
                str.append("|\t>Nothing new happened!" + "\n");
        }
        str.append("\n|----------------------------------------------");
        return str.toString();
    }

    private int getEVIndex (ArrayList<EVView> list, String evID) {
        for (int e = 0; e < list.size(); e++)
            if (list.get(e).getEvID().equals(evID))
                return e;
        return -1;
    }
}
