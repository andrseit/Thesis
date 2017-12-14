package evs;

/**
 * Created by Thesis on 5/12/2017.
 */
public class Preferences {

    protected int start;
    protected int end;
    protected int energy;


    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setStartEndSlots (int start, int end) { this.start = start; this.end = end; }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public String toString () {
        return "Slots: " + start + " - " + end + " / Energy: " + energy;
    }
}
