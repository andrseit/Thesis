package evs.strategy;

import evs.EVInfo;
import evs.Preferences;
import station.StationInfo;
import station.SuggestionMessage;
import various.IntegerConstants;

import java.util.*;

/**
 * Created by Thesis on 18/12/2017.
 */
public class Strategy {

    private ArrayList<SuggestionMessage> suggestions;
    private ArrayList<StationInfo> pendingStations;
    private boolean rejectPendingStations;

    private StrategyPreferences strategyPreferences;
    private int s_rounds;

    public Strategy(int energy, int start, int end, int probability, int rounds) {
        suggestions = new ArrayList<>();
        pendingStations = new ArrayList<>();
        strategyPreferences = new StrategyPreferences(energy, start, end, 0, rounds, probability, "price");
        s_rounds = 0;
    }

    public void addSuggestion (SuggestionMessage suggestion) {
        suggestions.add(suggestion);
    }

    public void evaluate (EVInfo info) {
        StrategyComputer computer = new StrategyComputer(info, strategyPreferences);
        ArrayList<ComparableSuggestion> comparable_suggestions = computer.produceComparableSuggestions(suggestions);
        System.out.println("    Comparable suggestions");
        for (ComparableSuggestion s: comparable_suggestions) {
            System.out.println("        " + s.toString());
        }
        if (!comparable_suggestions.isEmpty()) {
            int[] states = this.compareSuggestions(comparable_suggestions);
            for (int s = 0; s < states.length; s++) {
                StationInfo station = comparable_suggestions.get(s).getStationAddress();
                if (states[s] != IntegerConstants.EV_EVALUATE_PENDING) {
                    if (pendingStations.contains(station))
                        pendingStations.remove(station);
                    station.checkIn(info, states[s]);
                } else
                    pendingStations.add(station);
            }
            suggestions.clear();
            if (rejectPendingStations) {
                for (StationInfo station: pendingStations)
                    station.checkIn(info, IntegerConstants.EV_EVALUATE_REJECT);
                rejectPendingStations = false;
            }
            s_rounds++;
        }
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
            if (suggestion.getPreferencesDistance() == -2) {
                states[s] = IntegerConstants.EV_EVALUATE_PENDING;
            } else if (suggestion.getPreferencesDistance() == -1) {
                states[s] = IntegerConstants.EV_EVALUATE_REJECT;
            }
            else if (suggestion.getPreferencesDistance() < Integer.MAX_VALUE ) { //&& suggestion.getPreferencesDistance() > 0) {
                states[s] = IntegerConstants.EV_EVALUATE_ACCEPT;
                rejectPendingStations = true;
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

    public boolean isEmpty () { return suggestions.isEmpty(); }

    public void printSuggestionsList () {
        for (Preferences p: suggestions) {
            System.out.println("    " + p.toString());
        }
    }

    public String toString () {
        String str = "Strategy: \n" +
                "\t\tStart: " + strategyPreferences.getStart() +
                " End: " + strategyPreferences.getEnd() +
                " Rounds: " + strategyPreferences.getRounds() +
                " Probability: " + strategyPreferences.getPriority();
        return str;
    }
}
