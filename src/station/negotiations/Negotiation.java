package station.negotiations;

import evs.EV;

import java.util.ArrayList;

/**
 * Created by Darling on 28/9/2017.
 */
public class Negotiation {

    private int[][] initial_schedule;
    private int[] occupancy;
    private ArrayList<EV> evs;


    public Negotiation(int[][] initial_schedule, int[] occupancy, ArrayList<EV> evs) {
        this.initial_schedule = initial_schedule;
        this.occupancy = occupancy;
        this.evs = evs;
    }


    public void getSuggestions () {

        EV current = evs.get(0);
        int[][] slots = current.getSlotsArray();
        int energy = current.getEnergy();

        int available_energy = 0;
        // auto mallon paei array transformations
        for (int i = 0; i < slots.length; i++) {
            for (int s = slots[i][0]; s < slots[i][1]; s++) {

            }
        }
    }

}
