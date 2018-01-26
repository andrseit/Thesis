package station;

import station.negotiation.Suggestion;
import various.ArrayTransformations;

import java.util.ArrayList;

/**
 * Created by Thesis on 9/1/2018.
 */
public class Schedule {

    private int[][] scheduleMap;
    private int[] remaining_chargers;
    private int slots_number;
    private int chargers_number;

    public Schedule(int slots_number, int chargers_number) {
        this.slots_number = slots_number;
        this.chargers_number = chargers_number;
        remaining_chargers = new int[slots_number];
        for (int s = 0; s < slots_number; s++) {
            remaining_chargers[s] = chargers_number;
        }
    }

    public void setFullScheduleMap(int[][] map) {
        this.scheduleMap = map;
        this.updateChargers(map);
    }

    private void updateChargers(int[][] map) {
        ArrayTransformations t = new ArrayTransformations();
        int[] occupancy = t.getColumnsCount(map);
        for (int s = 0; s < remaining_chargers.length; s++) {
            remaining_chargers[s] -= occupancy[s];
        }
    }

    public void resetChargers() {
        remaining_chargers = new int[slots_number];
        for (int s = 0; s < slots_number; s++) {
            remaining_chargers[s] = chargers_number;
        }
    }

    public void resetChargers(int[] chargers) {
        remaining_chargers = new int[slots_number];
        System.arraycopy(chargers, 0, remaining_chargers, 0, slots_number);
    }

    private void computeRemainingChargers() {

    }

    public int[][] getScheduleMap() {
        return scheduleMap;
    }

    public int[] getRemainingChargers() {
        return remaining_chargers;
    }

    public void printScheduleMap(int[] price) {

        StringBuilder str = new StringBuilder();
        str.append("       ");
        for (int s = 0; s < slots_number; s++) {
            str.append(s).append(" ");
        }
        str.append("\n");

        str.append("price: ");
        for (int s = 0; s < slots_number; s++) {
            str.append(price[s]).append(" ");
        }
        str.append("\n\n");

        str.append("charg: ");
        for (int s = 0; s < slots_number; s++) {
            str.append(remaining_chargers[s]).append(" ");
        }
        str.append("\n\n");

        for (int e = 0; e < scheduleMap.length; e++) {
            str.append("ev_").append(e).append(":  ");
            for (int s = 0; s < scheduleMap[0].length; s++) {
                str.append(scheduleMap[e][s]).append(" ");
            }
            str.append("\n");
        }
        System.out.println(str.toString());
    }

    public void printScheduleMap(int[][] map, int[] price) {

        StringBuilder str = new StringBuilder();
        str.append("       ");
        for (int s = 0; s < slots_number; s++) {
            str.append(s).append(" ");
        }
        str.append("\n");

        str.append("price: ");
        for (int s = 0; s < slots_number; s++) {
            str.append(price[s]).append(" ");
        }
        str.append("\n\n");

        str.append("charg: ");
        for (int s = 0; s < slots_number; s++) {
            str.append(remaining_chargers[s]).append(" ");
        }
        str.append("\n\n");

        for (int e = 0; e < map.length; e++) {
            str.append("ev_").append(e).append(":  ");
            for (int s = 0; s < map[0].length; s++) {
                str.append(map[e][s]).append(" ");
            }
            str.append("\n");
        }
        System.out.println(str.toString());
    }


    public void updateNegotiationChargers(ArrayList<EVObject> negotiation_evs) {
        for (EVObject ev : negotiation_evs) {
            Suggestion suggestion = ev.getSuggestion();
            int start = suggestion.getStart();
            int end = suggestion.getEnd();
            int[] affected_slots = suggestion.getSlotsAfected();
            for (int s = start; s <= end; s++) {
                if (affected_slots[s] == 1) {
                    remaining_chargers[s] -= 1;
                    scheduleMap[ev.getStationId()][s] = 1;
                }
            }
        }
    }
}
