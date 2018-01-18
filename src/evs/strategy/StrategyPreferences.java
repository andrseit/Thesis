package evs.strategy;

/**
 * Created by Thesis on 12/1/2018.
 */
class StrategyPreferences {

    private int energy;
    private int start;
    private int end;
    private int movement; // orio metakinisis, na proste8ei argotera
    private int rounds;
    private int probability;
    private String priority; // order messages by price, distance


    public StrategyPreferences(int energy, int start, int end, int movement, int rounds, int probability, String priority) {
        this.energy = energy;
        this.start = start;
        this.end = end;
        this.movement = movement;
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

    public int getMovement() {
        return movement;
    }

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
