package evs;

import station.SuggestionMessage;
import station.negotiation.Suggestion;
import various.IntegerConstants;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Thesis on 18/12/2017.
 */
public class Strategy {

    private ArrayList<SuggestionMessage> suggestions;

    private int energy;
    private int start;
    private int end;
    private int movement; // orio metakinisis, na proste8ei argotera
    private int rounds;
    private int probability;

    private int s_rounds; // how many suggestions have been made

    public Strategy(int energy, int start, int end, int probability, int rounds) {
        suggestions = new ArrayList<>();
        this.energy = energy;
        this.start = start;
        this.end = end;
        this.rounds = rounds;
        this.probability = probability;
        s_rounds = 0;
    }

    public void addSuggestion (SuggestionMessage suggestion) {
        suggestions.add(suggestion);
    }


    /*
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
    */

    public void evaluate (EVInfo info) {

        if (!suggestions.isEmpty()) {
            // the decision for every station suggestion
            int[] states = new int[suggestions.size()];
            for (int i = 0; i < states.length; i++) {
                states[i] = -1;
            }
            int choice = -1;
            for (int s = 0; s < suggestions.size(); s++) {
                int state;
                Scanner scanner = new Scanner(System.in);
                Random random = new Random();
                state = scanner.nextInt(); //random.nextInt(3);
                if (state == IntegerConstants.EV_EVALUATE_ACCEPT &&
                        (suggestions.get(s).getStart() < Integer.MAX_VALUE)) {
                    choice = s;
                    states[s] = state;
                    break;
                }
                states[s] = state;
            }
            if (choice != -1) {
                for (int s = 0; s < suggestions.size(); s++) {
                    if (states[s] == IntegerConstants.EV_EVALUATE_ACCEPT) {
                        SuggestionMessage suggestion = suggestions.get(s);
                        suggestion.getStationAddress().checkIn(info, IntegerConstants.EV_EVALUATE_ACCEPT);
                    } else {
                        SuggestionMessage suggestion = suggestions.get(s);
                        suggestion.getStationAddress().checkIn(info, IntegerConstants.EV_EVALUATE_REJECT);
                    }
                }
            } else {
                for (int s = 0; s < suggestions.size(); s++) {
                    SuggestionMessage suggestion = suggestions.get(s);
                    suggestion.getStationAddress().checkIn(info, states[s]);
                }
            }

            suggestions.clear();
        }
    }

    /**
     * this method checks if a suggestion is inside the
     * initial preferences slots range and same energy
     * @return
     */
    private boolean isOK () {
        return true;
    }

    public boolean isEmpty () { return suggestions.isEmpty(); }

    public void printSuggestionsList () {
        for (Preferences p: suggestions) {
            System.out.println("    " + p.toString());
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
