package agents.station.optimize;

import agents.station.EVObject;

import java.util.ArrayList;

/**
 * Created by Thesis on 21/1/2019.
 */
public interface Optimizer {

    // we want an optimizer which produces the optimal schedule
    // maybe it is going to need a few arguments
    int[][] optimize(int slotsNumber, int currentSlot, ArrayList<EVObject> evs, int[] remainingChargers, int[] price);
}
