package agents.evs.strategy;

import agents.evs.EVInfo;
import agents.station.StationInfo;
import agents.station.SuggestionMessage;
import various.IntegerConstants;

import java.util.ArrayList;
import java.util.HashMap;

public class Strategy {

    //private ArrayList<SuggestionMessage> suggestions;
    private ArrayList<StationInfo> pendingStations;
    private boolean rejectPendingStations;
    private boolean charged;

    private StrategyPreferences strategyPreferences;
    private int s_rounds;

    private HashMap<StationInfo, Integer> answers;

    public Strategy(int energy, int start, int end, double range, int probability, int rounds, String priority) {
        //suggestions = new ArrayList<>();
        pendingStations = new ArrayList<>();
        strategyPreferences = new StrategyPreferences(energy, start, end, range, rounds, probability, priority);
        s_rounds = 0;
    }

    /*
    public void addSuggestion(SuggestionMessage suggestion) {
        suggestions.add(suggestion);
    }
    */

    public void evaluate(ArrayList<SuggestionMessage> suggestions, EVInfo info) {
        // this hashmap contains the answers to the stations' suggestions
        answers = new HashMap<>();
        if (!charged) {
            StrategyComputer computer = new StrategyComputer(info, strategyPreferences);
            ArrayList<ComparableSuggestion> comparable_suggestions = computer.produceComparableSuggestions(suggestions);

            /*
            System.out.println("\tComparable suggestions ev_" + info.getId());
            for (ComparableSuggestion s : comparable_suggestions) {
                System.out.println("\t\t" + s.toString());
            }
            */

            if (!comparable_suggestions.isEmpty()) {
                int[] states = this.compareSuggestions(comparable_suggestions);
                for (int s = 0; s < states.length; s++) {
                    StationInfo station = comparable_suggestions.get(s).getStationAddress();
                    if (states[s] != IntegerConstants.EV_EVALUATE_PENDING) {
                        if (pendingStations.contains(station))
                            pendingStations.remove(station);
                        /*
                        agents.station.checkIn(info, states[s]);
                        */
                        answers.put(station, states[s]);
                        if (states[s] == IntegerConstants.EV_EVALUATE_ACCEPT) {
                            for (SuggestionMessage sMessage : suggestions) {
                                if (sMessage.getStationInfo().getId() == station.getId()) {
                                    SuggestionMessage suggestion = sMessage;
                                    int start = suggestion.getStart(), end = suggestion.getEnd(), energy = suggestion.getEnergy();
                                    info.getPreferences().setPreferences(start, end, energy);
                                }
                            }

                        }
                    } else {
                        if (!pendingStations.contains(station))
                            pendingStations.add(station);
                    }
                }
                suggestions.clear();
                if (rejectPendingStations) {
                    for (StationInfo station : pendingStations)
                        /*
                        agents.station.checkIn(info, IntegerConstants.EV_EVALUATE_REJECT);
                        */
                        answers.put(station, IntegerConstants.EV_EVALUATE_REJECT);
                    rejectPendingStations = false;
                    charged = true;
                }
                s_rounds++;
            }
        } else {
            //System.out.println("Already charged! " + suggestions.size());
            for (SuggestionMessage message: suggestions) {
                //System.out.println("Rejecting");
                /*
                message.getStationAddress().checkIn(info, IntegerConstants.EV_EVALUATE_REJECT);
                */
                answers.put(message.getStationInfo(), IntegerConstants.EV_EVALUATE_REJECT);
            }
        }
    }

    private int[] compareSuggestions(ArrayList<ComparableSuggestion> comparableSuggestions) {
        // in which agents.station it accepted/rejected/asked for better suggestion
        int[] states = new int[comparableSuggestions.size()];
        for (int s = 0; s < states.length; s++) {
            states[s] = IntegerConstants.EV_EVALUATE_WAIT;
        }

        // if the ev is in the last round of the conversation based on its strategy
        if (s_rounds == strategyPreferences.getRounds()) {
            for (int s = 0; s < comparableSuggestions.size(); s++) {
                ComparableSuggestion suggestion = comparableSuggestions.get(s);
                if (suggestion.getPreferencesDistance() == -2 || suggestion.getPreferencesDistance() == -1
                        || suggestion.getPreferencesDistance() == Integer.MAX_VALUE)
                    states[s] = IntegerConstants.EV_EVALUATE_REJECT;
                else if (suggestion.getPreferencesDistance() == -3) {
                    states[s] = IntegerConstants.EV_EVALUATE_PENDING;
                } else {
                    states[s] = IntegerConstants.EV_EVALUATE_ACCEPT;
                    rejectPendingStations = true;
                    for (int i = s + 1; i < states.length; i++) {
                            states[i] = IntegerConstants.EV_EVALUATE_REJECT;
                    }
                    break;
                }
            }
        } else if (s_rounds < strategyPreferences.getRounds()) {
            for (int s = 0; s < comparableSuggestions.size(); s++) {
                ComparableSuggestion suggestion = comparableSuggestions.get(s);
                if (suggestion.getPreferencesDistance() == -2)
                    states[s] = IntegerConstants.EV_EVALUATE_REJECT;
                else if (suggestion.getPreferencesDistance() == 0){
                    states[s] = IntegerConstants.EV_EVALUATE_ACCEPT;
                    rejectPendingStations = true;
                    for (int i = 0; i < states.length; i++) {
                        if (i != s)
                            states[i] = IntegerConstants.EV_EVALUATE_REJECT;
                    }
                    break;
                }
            }
        }
        if (!rejectPendingStations) {
            for (int s = 0; s < comparableSuggestions.size(); s++) {
                ComparableSuggestion suggestion = comparableSuggestions.get(s);
                if (suggestion.getPreferencesDistance() == -3) {
                    states[s] = IntegerConstants.EV_EVALUATE_PENDING;
                }
            }
        }
        /*
        for (int s = 0; s < states.length; s++) {
            System.out.print(states[s] + " ");
        }
        System.out.println();
        */
        return states;
    }

    /*
    public boolean isEmpty() {
        return suggestions.isEmpty();
    }
    */

    /*
    public void printSuggestionsList() {
        for (Preferences p : suggestions) {
            System.out.println("    " + p.toString());
        }
        System.out.println();
    }
    */

    public void resetRounds() {
        s_rounds = 0;
    }

    public void resetCharged () { charged = false; }

    public HashMap<StationInfo, Integer> getAnswers () {
        return answers;
    }

    public String toString() {
        return "Strategy: \n" +
                "\t\tStart: " + strategyPreferences.getStart() + ", " +
                " End: " + strategyPreferences.getEnd() + ", " +
                " Energy: " + strategyPreferences.getEnergy() + ", " +
                " Rounds: " + strategyPreferences.getRounds() + ", " +
                " Priority: " + strategyPreferences.getPriority();
    }
}
