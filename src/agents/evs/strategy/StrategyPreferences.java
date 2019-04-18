package agents.evs.strategy;

/**
 * Created by Thesis on 12/1/2018.
 */
public class StrategyPreferences {

    private final int informSlot;
    private final int energy;
    private final int start;
    private final int end;
    private final int rounds;
    private final int probability;
    private final double range;
    private final String priority;


    public StrategyPreferences(int informSlot,
                               int energy,
                               int start,
                               int end,
                               double range,
                               int rounds,
                               int probability,
                               String priority) {
        this.informSlot = informSlot;
        this.energy = energy;
        this.start = start;
        this.end = end;
        this.range = range;
        this.rounds = rounds;
        this.probability = probability;
        this.priority = priority;
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

}
