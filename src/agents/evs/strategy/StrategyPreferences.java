package agents.evs.strategy;

/**
 * Created by Thesis on 12/1/2018.
 */
class StrategyPreferences {

    private int energy;
    private int start;
    private int end;
    private int rounds;
    private int probability;
    private double range;
    private String priority;


    public StrategyPreferences(int energy, int start, int end, double range, int rounds, int probability, String priority) {
        this.energy = energy;
        this.start = start;
        this.end = end;
        this.range = range;
        this.rounds = rounds;
        this.probability = probability;
        this.priority = priority;
    }

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
