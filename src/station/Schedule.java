package station;

import various.ArrayTransformations;

import java.util.ArrayList;

/**
 * Created by Darling on 29/7/2017.
 */
public class Schedule {

    // it is used to store information about when an ev is charging
    private class EVs_data {
        private int id;
        private int start_slot;
        private int end_slot;

        public int getId() { return id; }
        public int getStart_slot() { return start_slot; }
        public int getEnd_slot() { return end_slot; }
    }

    private int[][] schedule_map; // rows: chargers, columns: slots
    private int[][] full_schedule_map; // contains the output of cplex - rows: evs, columns: slots
    private int[] map_occupancy; // how many evs charging at each slot
    private ArrayList<EVs_data> evs_slots; // contains info for when ev is charging. id-start-end
    private int num_chargers;
    private int num_slots;
    private int[] remaining_chargers;

    public Schedule(int num_slots, int num_chargers) {
        this.num_chargers = num_chargers;
        this.num_slots = num_slots;
        schedule_map = new int[num_chargers][num_slots];
        full_schedule_map = new int[0][num_slots];
        evs_slots = new ArrayList<EVs_data>();
        map_occupancy = new int[num_slots];
        remaining_chargers = new int[num_slots];
        for (int s = 0; s < num_slots; s++) {
            remaining_chargers[s] = num_chargers;
        }
    }


    public void printSchedule() {
        for(int e = 0; e < schedule_map.length; e++) {
            for(int s = 0; s < schedule_map[0].length; s++) {
                System.out.print(schedule_map[e][s] + " ");
            }
            System.out.println();
        }
    }


    public void setOccupancy (int slots, int chargers) {
        map_occupancy = new int[slots];
        for(int c = 0; c < chargers; c++) {
            for(int s = 0; s < slots; s++) {
                map_occupancy[s] += schedule_map[c][s];
            }
        }
    }

    public void setRemainingChargers (int slots) {

        for (int s = 0; s < slots; s++) {
            remaining_chargers[s] -= map_occupancy[s];
        }
    }

    // concatenates old and new map
    public void saveInitialScheduleMap (int[][] initial) {

        ArrayTransformations t = new ArrayTransformations();
        if (initial.length != 0) {
            //this.full_schedule_map = initial;
            int map_length = full_schedule_map.length;
            int[][] new_full_schedule_map = new int[map_length + initial.length][num_slots];
            System.arraycopy(full_schedule_map, 0, new_full_schedule_map, 0, full_schedule_map.length);
            System.arraycopy(initial, 0, new_full_schedule_map, full_schedule_map.length, initial.length);
            full_schedule_map = new_full_schedule_map;
            //System.out.println("Rows: " + full_schedule_map.length + ", Columns: " + full_schedule_map[0].length);
        }
        schedule_map = t.shrinkArray(initial, num_chargers);

        this.setOccupancy(num_slots, num_chargers);
    }


    public void updateFullScheduleMap (EVInfo ev, int start, int end, int row) {
        ArrayTransformations t = new ArrayTransformations();
        t.updateArray(full_schedule_map, start, end, row);
    }

    /*
    public void printFullScheduleMap() {
        for(int e = 0; e < full_schedule_map.length; e++) {
            for(int s = 0; s < full_schedule_map[0].length; s++) {
                System.out.print(full_schedule_map[e][s] + " ");
            }
            System.out.println();
        }
    }
    */


    public String printFullScheduleMap (int[] price) {

        StringBuilder str = new StringBuilder();
        str.append("------------------------ Slots ------------------------\n");
        str.append("       ");
        for (int s = 0; s < num_slots; s++) {
            str.append(s + " ");
        }
        str.append("\n");

        str.append("occup: ");
        for (int s = 0; s < num_slots; s++) {
            str.append(map_occupancy[s] + " ");
        }
        str.append("\n\n");

        str.append("price: ");
        for (int s = 0; s < num_slots; s++) {
            str.append(price[s] + " ");
        }
        str.append("\n\n");

        str.append("charg: ");
        for (int s = 0; s < num_slots; s++) {
            str.append(remaining_chargers[s] + " ");
        }
        str.append("\n\n");

        for (int e = 0; e < full_schedule_map.length; e++) {
            str.append("ev_" + e + ":  ");
            for (int s = 0; s < full_schedule_map[0].length; s++) {
                str.append(full_schedule_map[e][s] + " ");
            }
            str.append("\n");
        }
        str.append("-------------------------------------------------------\n");
        return str.toString();
    }

    public int[][] getScheduleMap() {
        return schedule_map;
    }

    public int[][] getFullScheduleMap () { return full_schedule_map; }

    public int[] getMapOccupancy () { return map_occupancy; }

    public int[] getRemainingChargers () { return remaining_chargers; }
}
