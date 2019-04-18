package user_interface;

import agents.station.EVObject;

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

    private int[][] schedule;
    private int[] chargers;
    private int[] remainingChargers;
    ArrayList<EVObject> chargedEVs;

    public StationState (int stationID, int slotsNumber, int chargersNumber) {
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

    public void addScheduleState (int[][] map, int[] chargers,
                                  int[] remainingChargers, ArrayList<EVObject> chargedEVs) {
        if (map == null)
            this.schedule = new int[0][0];
        else
            this.schedule = map.clone();
        this.chargers = chargers.clone();
        this.remainingChargers = remainingChargers.clone();
        this.chargedEVs = (ArrayList<EVObject>) chargedEVs.clone();
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


    public void printTemporaryScheduleMap() {
        System.out.println(printAnyMap("Temporary"));
    }

    public void printScheduleMap() {
        System.out.println(printAnyMap("Main"));
    }

    private String printAnyMap (String name) {
        //if (agents.evs.isEmpty())
        // return "----------------\nCannot generate " + name + " map!\n-------------------";
        int slotsNumber = 0;
        if (schedule.length > 0)
            slotsNumber = schedule[0].length;
        StringBuilder str = new StringBuilder();
        str.append("--------------- ").append(name).append(" Schedule Map -------------------\n");
        for (int s = 0; s < slotsNumber; s++)
            str.append(s).append("  ");
        str.append(" : Slots number\n-------------------------------------\n");

        if (name.equals("Temporary")){
            for (int s = 0; s < chargers.length; s++) {
                str.append(remainingChargers[s]).append("  ");
            }
            str.append(" : Main Remaining Chargers\n");
        }

        for (int slot : chargers) {
            str.append(slot).append("  ");
        }
        str.append(" : ").append(name).append(" Remaining Chargers\n--------------------------------\n");
        if (!chargedEVs.isEmpty()) {
            for (int ev = 0; ev < schedule.length; ev++) {
                for (int slot = 0; slot < schedule[ev].length; slot++) {
                    str.append(schedule[ev][slot]).append("  ");
                }
                str.append(" : EV No ").append(chargedEVs.get(ev).getId()).append(" (").append(chargedEVs.get(ev).getStationId()).append(")\n");
            }
        } else {
            str.append("No entries in map!\n");
        }
        str.append("-----------------------------------\n");
        return str.toString();
    }

}
