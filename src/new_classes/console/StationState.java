package new_classes.console;

import java.util.ArrayList;

/**
 * Created by Thesis on 2/4/2019.
 *
 * For testing, keeps the state of the station
 * which evs he is communicating with and what are their answers
 */
public class StationState {

    private String station;
    private ArrayList<EVView>[] evs;

    public StationState (int stationID, int slotsNumber) {
        station = "station" + stationID;
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
            str.append(ev + "\n");
        }
        return str.toString();
    }

    public String getStates (int currentSlot) {
        StringBuilder str = new StringBuilder("|------------- " + station + " ---------------------\n\n");
        for (int s = 0; s < currentSlot + 1; s++) {
            str.append("|\t*Slot: " + s + "\n");
            if (evs[s] != null) {
                for (EVView ev : evs[s]) {
                    str.append("|\t>" + ev + "\n");
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
