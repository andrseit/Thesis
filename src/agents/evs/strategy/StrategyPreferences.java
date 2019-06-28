package agents.evs.strategy;

/**
 * Created by Thesis on 12/1/2018.
 */
public class StrategyPreferences {

    private final int informSlot;
    private int energy;
    private int start;
    private int end;
    private final int rounds;
    private final int probability;
    private final double range;
    private final String priority;
    private boolean delay;


    public StrategyPreferences(int informSlot,
                               int energy,
                               int start,
                               int end,
                               double range,
                               int rounds,
                               int probability,
                               String priority,
                               boolean delay) {
        this.informSlot = informSlot;
        this.energy = energy;
        this.start = start;
        this.end = end;
        this.range = range;
        this.rounds = rounds;
        this.probability = probability;
        this.priority = priority;
        this.delay = delay;
    }

    public int getInformSlot() { return informSlot; }

    public int getEnergy() {
        return energy;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public double getRange() { return range; }

    public int getRounds() {
        return rounds;
    }

    public int getProbability() {
        return probability;
    }

    public String getPriority() {
        return priority;
    }

    public boolean isDelay() { return delay; }

    public void setBounds (int start, int end, int energy) {
        this.start = start;
        this.end = end;
        this.energy = energy;
    }
}
