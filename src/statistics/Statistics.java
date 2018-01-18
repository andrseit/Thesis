package statistics;

import various.ArrayTransformations;

/**
 * Created by Thesis on 4/1/2018.
 */
class Statistics {

    public void occupancyPercentage (int[][] map, int chargers) {
        ArrayTransformations at = new ArrayTransformations();
        int[][] occupancy_map = at.shrinkArray(map, chargers);

        at.printIntArray(occupancy_map);
        double occupancy = 0;
        double all = occupancy_map.length * occupancy_map[0].length;

        for (int c = 0; c < occupancy_map.length; c++) {
            for (int s = 0; s < occupancy_map[c].length; s++) {
                    occupancy += occupancy_map[c][s];
            }
        }

        System.out.println(occupancy/all);
    }

    public void evsChargedPercentage (int[][] schedule) {

        double count = 0;
        for (int ev = 0; ev < schedule.length; ev++) {
            for (int s = 0; s < schedule[ev].length; s++) {
                if (schedule[ev][s] == 1){
                    count++;
                    break;
                }
            }
        }
        System.out.println(count/schedule.length);
    }




}
