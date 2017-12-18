package evs;

import station.negotiation.Suggestion;

import java.util.Random;

/**
 * Created by Thesis on 18/12/2017.
 */
public class Strategy {

    private int energy;
    private int start;
    private int end;
    private int movement; // orio metakinisis, na proste8ei argotera
    private int rounds;
    private int probability;

    private int s_rounds; // how many suggestions have been made

    public Strategy(int energy, int start, int end, int probability, int rounds) {
        this.energy = energy;
        this.start = start;
        this.end = end;
        this.rounds = rounds;
        this.probability = probability;
        s_rounds = 0;
    }

    public int evaluate (Preferences suggestion) {

        Random generator = new Random();
        int accept_probability = generator.nextInt(99) + 1;
        s_rounds++;

        int s_energy = suggestion.getEnergy();
        int s_start = suggestion.getStart();
        int s_end = suggestion.getEnd();

        if (s_energy >= energy && s_start >= start && s_end <= end) {
            if (accept_probability <= probability) {
                return 1;
            } else {
                if (s_rounds == rounds) {
                    return 3; // telos
                } else {
                    return 2; // zita kai alli protasi
                }
            }
        } else {
            if (s_rounds == rounds) {
                return 3; // telos
            } else {
                return 2; // zita kai alli protasi
            }
        }
    }

    public String toString () {
        StringBuilder str = new StringBuilder();
        str.append("Bounds: \n");
        str.append("    Start: " + start + "\n");
        str.append("    End: " + end + "\n");
        str.append("    Rounds: " + rounds + "\n");
        str.append("    Probability: " + probability);
        return str.toString();
    }
}
