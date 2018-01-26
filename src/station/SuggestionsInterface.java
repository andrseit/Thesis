package station;

import java.util.ArrayList;

/**
 * Created by Thesis on 19/1/2018.
 */
public interface SuggestionsInterface {

    void findSuggestions(ArrayList<EVObject> evs, int[][] scheduleMap, int[] remainingChargers, int[] price);
}
