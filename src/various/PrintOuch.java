package various;

import evs.Preferences;
import station.EVObject;
import station.negotiation.Suggestion;

import java.util.ArrayList;

/**
 * Created by Thesis on 8/12/2017.
 */
class PrintOuch {


    public void printEVs(ArrayList<EVObject> evs) {
        for (EVObject ev : evs) {
            System.out.println(ev.toString());
        }
    }

    public void comparePreferences(Preferences initial, Suggestion suggestion) {
        System.out.println();
        System.out.println("Comparing Suggestion with Preferences:");
        System.out.println("Start: " + initial.getStart() + "/" + suggestion.getStart());
        System.out.println("End: " + initial.getEnd() + "/" + suggestion.getEnd());
        System.out.println("Energy: " + initial.getEnergy() + "/" + suggestion.getEnergy());
        System.out.println("Rating: " + suggestion.getRating());
        System.out.println();
    }
}
