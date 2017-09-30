package station.negotiations;

/**
 * Created by Darling on 7/8/2017.
 */
public class NegotiationComputer {

    private int energy;
    private int[][] slots;
    private int chargers;
    private int[] occupancy;

    private int final_start; // new value to return
    private int final_end; // new value to return


    public void computeOffer (int energy, int[][] slots, int chargers, int[] occupancy) {

        this.energy = energy;
        this.slots = slots;
        this.chargers = chargers;
        this.occupancy = occupancy;

        this.shiftWindow(-1);
    }

    private int[] minimunEnergyInInitialRange () {
        int max_energy = -1; // max energy the station can offer in the given slots
        int[] results = new int[3];
        for (int b = 0; b < slots.length; b++) {

            int start = slots[b][0];
            int end = slots[b][1];

            int available_energy = this.availableEnergyInRange(start, end);
            if (available_energy > max_energy) {
                max_energy = available_energy;
                results[0] = max_energy; // energy
                results[1] = start; // final start
                results[2] = end; // final end
            }

        }
        //System.out.println("I can offer to you: " + max_energy + " energy points.");
        return results;
    }

    public void lessEnergy () {

        int[] results = minimunEnergyInInitialRange();

        final_start = results[1];
        final_end = results[2];
    }


    public void shiftWindow (int step) {

        int energy_available = -1;
        final_start = 0;
        final_end = 0;
        for (int b = 0; b < slots.length; b++) {

            int start = slots[b][0];
            int end = slots[b][1];

            // end < occupancy.length
            // start >= 0

            while (availableEnergyInRange(start, end) != energy) {
                if (start+step < 0 || end+step > occupancy.length) {
                    System.out.println("break");
                    break;
                }

                start += step;
                end += step;
                //System.out.println(availableEnergyInRange(start, end));
                int energy_in_range = availableEnergyInRange(start, end);
                if (energy_in_range > energy_available) {
                    energy_available = availableEnergyInRange(start, end);
                    final_start = start;
                    final_end = end;
                }

            }
        }
        if (energy_available > minimunEnergyInInitialRange()[0])
            System.out.println("Final -> Start: " + final_start + ", end: " + final_end + " energy: " + energy_available);
        else
            System.out.println("Go with less energy");

    }

    public void widenWindow () {

        int step = 1;
        final_start = 0;
        final_end = 0;
        int max_energy = -1;
        for (int b = 0; b < slots.length; b++) {

            int start = slots[b][0];
            int end = slots[b][1];

            while (max_energy < energy) {
                if (start - step < 0)
                    break;
                start -= step;
                int found_energy = availableEnergyInRange(start, end);
                if (found_energy >= energy) {
                    final_start = start;
                    final_end = end;
                    max_energy = found_energy;
                }
            }


            // if you didn't find the energy you wanted then move to the other direction
            if (max_energy == -1) {
                while (max_energy < energy) {
                    if (end + step >= occupancy.length)
                        break;
                    end += step;
                    int found_energy = availableEnergyInRange(start, end);
                    if (found_energy > max_energy) {
                        final_start = start;
                        final_end = end;
                        max_energy = found_energy;
                    }
                }
            }

        }

        // clear unnecessary slots
        for (int s = final_start; s < final_end; s++) {
            if (occupancy[s] == chargers) {
                final_start++;
            }
            else {
                break;
            }
        }

        for (int s = final_end; s >= final_start; s--) {
            System.out.println(s);
            if (occupancy[s] == chargers) {
                final_end--;
            }
            else {
                break;
            }
        }
        if (max_energy != -1)
            System.out.println("Final -> start: " + final_start + ", end: " + final_end + " with available energy: " + max_energy);
        else
            System.out.println("Not found!");
    }

    public int availableEnergyInRange (int start, int end) {
        int energy = 0;
        for (int s = start; s < end + 1; s++) {
            if (occupancy[s] < chargers)
                energy++;
        }
        return energy;
    }

    public int getNewStartSlot() {
        return final_start;
    }

    public int getNewEndSlot() {
        return final_end;
    }
}
