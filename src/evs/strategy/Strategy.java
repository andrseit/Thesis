package evs.strategy;

import evs.EVInfo;
import evs.Preferences;
import station.SuggestionMessage;
import various.IntegerConstants;

import java.util.*;

/**
 * Created by Thesis on 18/12/2017.
 */
public class Strategy {

    private ArrayList<SuggestionMessage> suggestions;

    private StrategyPreferences strategyPreferences;
    private int s_rounds;

    public Strategy(int energy, int start, int end, int probability, int rounds) {
        suggestions = new ArrayList<>();
        strategyPreferences = new StrategyPreferences(energy, start, end, 0, rounds, probability, "price");
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
        System.out.println("SUGGESTIONS SIZE: " + suggestions.size());
        StrategyComputer computer = new StrategyComputer(info, strategyPreferences);
        ArrayList<ComparableSuggestion> comparable_suggestions = computer.produceComparableSuggestions(suggestions);
        System.out.println("    Comparable suggestions");
        for (ComparableSuggestion s: comparable_suggestions) {
            System.out.println("        " + s.toString());
        }
        if (!comparable_suggestions.isEmpty()) {
            int[] states = this.compareSuggestions(comparable_suggestions);
            for (int s = 0; s < states.length; s++) {
                comparable_suggestions.get(s).getStationAddress().checkIn(info, states[s]);
            }
            suggestions.clear();
            s_rounds++;
        }



        /*
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
        */
    }

    private int[] compareSuggestions (ArrayList<ComparableSuggestion> comparableSuggestions) {
        // in which station it accpeted/rejected/asked for better suggestion
        int[] states = new int[comparableSuggestions.size()];
        for (int s = 0; s < states.length; s++) {
            states[s] = -1;
        }
        if (s_rounds > strategyPreferences.getRounds()) {
            for (int s = 0; s < states.length; s++) {
                states[s] = IntegerConstants.EV_EVALUATE_REJECT;
            }
            return states;
        }


        for (int s = 0; s < comparableSuggestions.size(); s++) {
            ComparableSuggestion suggestion = comparableSuggestions.get(s);
            if (suggestion.getPreferencesDistance() == -1) {
                states[s] = IntegerConstants.EV_EVALUATE_REJECT;
            }
            else if (suggestion.getPreferencesDistance() < Integer.MAX_VALUE ) { //&& suggestion.getPreferencesDistance() > 0) {
                states[s] = IntegerConstants.EV_EVALUATE_ACCEPT;
                for (int i = 0; i < states.length; i++) {
                    if (i != s)
                        states[i] = IntegerConstants.EV_EVALUATE_REJECT;
                }
                break;
            } else if (suggestion.getPreferencesDistance() == Integer.MAX_VALUE)
                states[s] = IntegerConstants.EV_EVALUATE_WAIT;
        }
        return states;
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
        str.append("    Start: " + strategyPreferences.getStart() + "\n");
        str.append("    End: " + strategyPreferences.getEnd() + "\n");
        str.append("    Rounds: " + strategyPreferences.getRounds() + "\n");
        str.append("    Probability: " + strategyPreferences.getPriority());
        return str.toString();
    }
}
