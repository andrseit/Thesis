package agents.station.statistics;

import agents.evs.Preferences;
import user_interface.EVView;
import user_interface.EVStateEnum;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Thesis on 29/3/2019.
 */
public class StationStatistics {

    private StationSlotStatistics[] slotStatistics;
    private StationSystemValues systemValues;


    public StationStatistics (int stationID, int chargersNumber, int slotsNumber) {

        systemValues = new StationSystemValues(stationID, chargersNumber, slotsNumber);
        slotStatistics = new StationSlotStatistics[slotsNumber];
        for (int i = 0; i < slotStatistics.length; i++)
            slotStatistics[i] = new StationSlotStatistics(systemValues, i);
    }

    public void addEV (int evID, EVStateEnum state, Preferences preferences, int currentSlot, int slots) {
        for (int i = systemValues.getSlotsNumber() - 1; i >= currentSlot; i--)
            slotStatistics[i].addEV(evID, state, preferences, slots);
    }


    /**
     * return the number of the statistics
     * @return the slots number, actually
     */
    public int size() {
        return slotStatistics.length;
    }

    public StationSlotStatistics getSlotStatistics (int slot) {
        return slotStatistics[slot];
    }
}
