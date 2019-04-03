package new_classes;

import station.EVObject;
import station.negotiation.Suggestion;
import various.ArrayTransformations;

import java.util.ArrayList;

/**
 * Created by Thesis on 21/1/2019.
 */
public class Schedule {
    /**
     * an evs*slots array, each cell (i, t) represents if ev i is charging at time slot t
     */
    private int[][] scheduleMap;
    private int[][] temporaryScheduleMap;

    private int[] whoCharges;
    private int[] remainingChargers, temporaryChargers; // temporary chargers are used after the computation of the initial program and before the computation of the alternatives
    private int chargersNumber, slotsNumber;

    public Schedule (int slotsNumber, int chargersNumber) {
        this.slotsNumber = slotsNumber;
        this.chargersNumber = chargersNumber;

        remainingChargers = new int[slotsNumber];
        temporaryChargers = new int[slotsNumber];
        for (int s = 0; s < slotsNumber; s++) {
            remainingChargers[s] = chargersNumber;
            temporaryChargers[s] = chargersNumber;
        }
    }

    public void addEVtoScheduleMap (EVObject ev) {
        ArrayList<Integer> slotsAllocated = ev.getSuggestion().getSlotsAllocated();
        int[][] evRow = new int[1][slotsNumber];
        for (int slot = 0; slot < slotsAllocated.size(); slot++) {
            evRow[0][slotsAllocated.get(slot)] = 1;
            // also update available chargers
            remainingChargers[slotsAllocated.get(slot)]--;
            if (remainingChargers[slotsAllocated.get(slot)] < 0) {
                System.err.println("Something is wrong with the scheduler");
                System.exit(1);
            }
        }
        // make this static
        ArrayTransformations t = new ArrayTransformations();
        scheduleMap = t.concatMaps(scheduleMap, evRow, slotsNumber);
    }

    public void setScheduleMap (int[][] scheduleMap) {
        this.scheduleMap = scheduleMap;
    }

    public void increaseRemainingChargers(EVObject ev) {
        ArrayList<Integer> slotsAllocated = ev.getSuggestion().getSlotsAllocated();
        for (int slot = 0; slot < slotsAllocated.size(); slot++)
            remainingChargers[slotsAllocated.get(slot)]++;
    }

    public int[][] getTemporaryScheduleMap() {
        return temporaryScheduleMap;
    }

    public void setTemporaryScheduleMap(int[][] temporaryScheduleMap) {
        this.temporaryScheduleMap = temporaryScheduleMap;
        whoCharges = new int[temporaryScheduleMap.length];
        for (int ev = 0; ev < temporaryScheduleMap.length; ev++) {
            for (int slot = 0; slot < temporaryScheduleMap[ev].length; slot++) {
                if (temporaryScheduleMap[ev][slot] == 1) {
                    whoCharges[ev] = 1;
                    break;
                }
            }
        }
    }

    // add map entries after alternatives' computation
    public void updateTemporaryScheduleMap (int[][] alternatives, ArrayList<EVObject> evs) {
        ArrayTransformations t = new ArrayTransformations();
        //t.printIntArray(temporaryScheduleMap);
        for (int e = 0; e < evs.size(); e++) {
            EVObject ev = evs.get(e);
            for (int s = 0; s < slotsNumber; s++) {
                temporaryScheduleMap[ev.getStationId()][s] = alternatives[e][s];
                if (temporaryScheduleMap[ev.getStationId()][s] == 1)
                    whoCharges[ev.getStationId()] = 1;
            }
        }
        //t.printIntArray(temporaryScheduleMap);
    }

    public Suggestion getChargingSlots (int ev) {
        Suggestion preferences = new Suggestion();
        ArrayList<Integer> slotsAllocated = new ArrayList<>(); // making this an array list because its size is unknown,
        // if a smart way to find its size before energy count, then do it an array
        if (whoCharges[ev] == 1) {
            for (int slot = 0; slot < temporaryScheduleMap[ev].length; slot++) {
                if (temporaryScheduleMap[ev][slot] == 1) {
                    preferences.setStart(slot);
                    break;
                }
            }
            for (int slot = slotsNumber - 1; slot >= 0; slot--) {
                if (temporaryScheduleMap[ev][slot] == 1) {
                    preferences.setEnd(slot);
                    break;
                }
            }
            int energy = 0;
            for (int slot = preferences.getStart(); slot <= preferences.getEnd(); slot++) {
                if (temporaryScheduleMap[ev][slot] == 1) {
                    energy += 1;
                    slotsAllocated.add(slot);
                }
            }
            preferences.setEnergy(energy);
            // just a simple test - it is not that useful!
            if (preferences.getEnergy() != slotsAllocated.size()) {
                System.err.println("Something is wrong with allocated slots! (ev station id: " + ev + ", energy: " + preferences.getEnergy() + ", slots: " + slotsAllocated.size() + ")");
                System.exit(1);
            }
        }
        preferences.setSlotsAllocated(slotsAllocated);
        return preferences;
    }

    public void updateTemporaryChargers (ArrayList<EVObject> temporaryEVs) {
        for (EVObject ev: temporaryEVs) {
            for (Integer slot: ev.getSuggestion().getSlotsAllocated()) {
                temporaryChargers[slot]--;
                if (temporaryChargers[slot] < 0) {
                    System.err.println("Something is wrong with the scheduler");
                    System.exit(1);
                }
            }
        }
    }

    public int[] getTemporaryChargers () {
        return temporaryChargers;
    }

    public void printTemporaryChargers () {
        System.out.println("-*-*-*-*-*-*-*-*-*-*-");
        for (int slot = 0; slot < temporaryChargers.length; slot++) {
            System.out.print(temporaryChargers[slot] + "  ");
        }
        System.out.println();
    }

    public int[] getWhoCharges () {
        return whoCharges;
    }

    public int[] getRemainingChargers () {
        return remainingChargers;
    }

    public int[][] getScheduleMap() {
        return scheduleMap;
    }

    public void printRemainingChargers () {
        ArrayTransformations t = new ArrayTransformations();
        t.printOneDimensionArray("Remaining chargers", remainingChargers);
    }

    public void updateStationIDs() {
        for (int slot = 0; slot < slotsNumber; slot ++) {
            temporaryChargers[slot] = remainingChargers[slot];
        }
    }

}
