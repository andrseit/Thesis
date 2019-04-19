package agents.evs;

/**
 * Add a constructor!!!
 */
public class Preferences {

    protected int start;
    protected int end;
    protected int energy;
    private int max_distance;


    public Preferences(int start, int end, int energy) {
        this.start = start;
        this.end = end;
        this.energy = energy;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setStartEndSlots(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public void setPreferences (int start, int end, int energy) {
        this.start = start;
        this.end = end;
        this.energy = energy;
    }

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

    public String toString() {
        return + start + "-" + end + "/" + energy;
    }

    public int getMaxDistance() {
        return max_distance;
    }

    public void setMaxDistance(int max_distance) {
        this.max_distance = max_distance;
    }

}
