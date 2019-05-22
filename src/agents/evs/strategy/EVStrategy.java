package agents.evs.strategy;

import agents.evs.EVInfo;
import agents.evs.Preferences;
import agents.station.StationInfo;
import agents.station.SuggestionMessage;
import agents.station.communication.StationReceiver;
import user_interface.EVState;
import agents.evs.communication.EVMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class EVStrategy {

    private EVState evState;

    private ArrayList<StationInfo> pendingStations;
    private boolean rejectPendingStations;
    private boolean charged;
    private boolean isToBeServiced; // the EV is to be charged, but the time has not yet arrived
    private boolean serviced;// the ev has been successfully serviced
    private boolean delayed; // shows if the ev has made a deferral - we assume that an EV can only do that once, however it can be easily changed
    // so that this can happen more than one time
    // rename it to something that represents both deferral and cancellation

    private StrategyPreferences strategyPreferences;
    private int s_rounds;

    private HashMap<StationInfo, EVMessage> answers;
    private StationReceiver acceptedStation;
    private Preferences acceptedPreferences;

    public EVStrategy(StrategyPreferences strategyPreferences, EVState evState) {
        pendingStations = new ArrayList<>();
        this.strategyPreferences = strategyPreferences;
        s_rounds = 0;
        this.evState = evState;
    }

    public void evaluate(ArrayList<SuggestionMessage> suggestions, EVInfo info) {
        for (SuggestionMessage message: suggestions)
            evState.addSuggestion(message.getStationInfo().getId(), message.preferencesToString());

        // this hash map contains the answers to the stations' suggestions
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
                EVMessage[] states = this.compareSuggestions(comparable_suggestions);
                for (int s = 0; s < states.length; s++) {
                    StationInfo station = comparable_suggestions.get(s).getStationAddress();
                    if (states[s] != EVMessage.EV_EVALUATE_PENDING) {
                        pendingStations.remove(station);
                        /*
                        agents.station.checkIn(info, states[s]);
                        */
                        answers.put(station, states[s]);
                        if (states[s] == EVMessage.EV_EVALUATE_ACCEPT) {
                            for (SuggestionMessage sMessage : suggestions) {
                                if (sMessage.getStationInfo().getId() == station.getId()) {
                                    int start = sMessage.getStart(), end = sMessage.getEnd(), energy = sMessage.getEnergy();
                                    //info.getPreferences().setPreferences(start, end, energy);
                                    acceptedPreferences = new Preferences(start, end, energy);
                                    acceptedStation = station.getCommunicationPort();
                                }
                            }
                            isToBeServiced = true;
                        }
                    } else {
                        if (!pendingStations.contains(station))
                            pendingStations.add(station);
                    }
                }
                suggestions.clear();
                if (rejectPendingStations) {
                    for (StationInfo station : pendingStations)
                        answers.put(station, EVMessage.EV_EVALUATE_REJECT);

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
                message.getStationAddress().checkIn(info, ConstantVariables.EV_EVALUATE_REJECT);
                */
                answers.put(message.getStationInfo(), EVMessage.EV_EVALUATE_REJECT);
            }
        }

        // add answers into state
        answers.keySet().forEach(stationInfo -> evState.addAnswer(stationInfo.getId(), answers.get(stationInfo)));
    }

    // check if this can be combined with the method above
    private EVMessage[] compareSuggestions(ArrayList<ComparableSuggestion> comparableSuggestions) {
        // in which agents.station it accepted/rejected/asked for better suggestion
        EVMessage[] states = new EVMessage[comparableSuggestions.size()];
        for (int s = 0; s < states.length; s++) {
            states[s] = EVMessage.EV_EVALUATE_WAIT;
        }

        // if the ev is in the last round of the conversation based on its strategy
        if (s_rounds == strategyPreferences.getRounds()) {
            for (int s = 0; s < comparableSuggestions.size(); s++) {
                ComparableSuggestion suggestion = comparableSuggestions.get(s);
                if (suggestion.getPreferencesDistance() == -2 || suggestion.getPreferencesDistance() == -1
                        || suggestion.getPreferencesDistance() == Integer.MAX_VALUE)
                    states[s] = EVMessage.EV_EVALUATE_REJECT;
                else if (suggestion.getPreferencesDistance() == -3) {
                    states[s] = EVMessage.EV_EVALUATE_PENDING;
                } else {
                    states[s] = EVMessage.EV_EVALUATE_ACCEPT;
                    rejectPendingStations = true;
                    for (int i = s + 1; i < states.length; i++) {
                            states[i] = EVMessage.EV_EVALUATE_REJECT;
                    }
                    break;
                }
            }
        } else if (s_rounds < strategyPreferences.getRounds()) {
            for (int s = 0; s < comparableSuggestions.size(); s++) {
                ComparableSuggestion suggestion = comparableSuggestions.get(s);
                if (suggestion.getPreferencesDistance() == -2)
                    states[s] = EVMessage.EV_EVALUATE_REJECT;
                else if (suggestion.getPreferencesDistance() == 0){
                    states[s] = EVMessage.EV_EVALUATE_ACCEPT;
                    rejectPendingStations = true;
                    for (int i = 0; i < states.length; i++)
                        if (i != s)
                            states[i] = EVMessage.EV_EVALUATE_REJECT;
                    break;
                }
            }
        }
        if (!rejectPendingStations) {
            for (int s = 0; s < comparableSuggestions.size(); s++) {
                ComparableSuggestion suggestion = comparableSuggestions.get(s);
                if (suggestion.getPreferencesDistance() == -3) states[s] = EVMessage.EV_EVALUATE_PENDING;
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

    /**
     * This method is used by the execution flow to determine if an EV is able to make a deferral
     * This is determined by a probability and the boolean variable: delayed
     * If the current time slot is equal to the EVs arrival time, then the EV is serviced
     * @param currentSlot
     * @param slotsNumber
     * @return
     */
    public EVMessage checkDelay(EVInfo info, int currentSlot, int slotsNumber) {
        System.out.println("EV No " + info.getId() + "( " + currentSlot + ", " + info.getPreferences().getStart() + " )");
        System.out.println("Accepted: " + acceptedPreferences.toString());
        Random random = new Random();
        if (isToBeServiced && random.nextInt(100) < 50 && !delayed) {
            System.out.println("I will delay or cancel!");
            if (random.nextInt() < 20) {
                // cancel
                System.out.println("I shall CANCEL my reservation!");
                delayed = true;
                isToBeServiced = false;
                charged = false;

                /*
                state.addSuggestion(strategy.getAcceptedStation().getStationId(), "-");
                state.addAnswer(strategy.getAcceptedStation().getStationId(), ConstantVariables.EV_UPDATE_CANCEL);
                */

                return EVMessage.EV_UPDATE_CANCEL;
            } else {
                System.out.println("I shall DELAY my reservation!");
                // delay
                if (!(slotsNumber - currentSlot <= 0)) {
                    delayed = true;
                    s_rounds = 0;
                    charged = false;

                    /*
                    state.addSuggestion(strategy.getAcceptedStation().getStationId(), "-");
                    state.addAnswer(strategy.getAcceptedStation().getStationId(), ConstantVariables.EV_UPDATE_DELAY);
                    */
                    return EVMessage.EV_UPDATE_DELAY;
                }
            }
        } else {
            System.out.println("I'll check in normally!");
            if (currentSlot == acceptedPreferences.getStart()) {
                serviced = true;
                isToBeServiced = false;
            }
        }
        System.out.println("Serviced: " + serviced);
        return EVMessage.EV_UPDATE_EMPTY;
    }

    public void computeDelay (EVInfo info, int slotsNumber) {
        int upperStartBound = Math.max((slotsNumber - acceptedPreferences.getEnergy()), (acceptedPreferences.getStart() + 1));
        int lowerStartBound = acceptedPreferences.getStart() + 1;

        System.out.println("Lower start: " + lowerStartBound + ", Max start: " + upperStartBound);
        Random random = new Random();
        int newStart = random.nextInt(upperStartBound - lowerStartBound + 1) + lowerStartBound;
        int energy = Math.min(acceptedPreferences.getEnergy(), (slotsNumber - newStart));
        int newEnd = newStart + Math.min(energy, slotsNumber - newStart) - 1;

        Preferences initial = info.getPreferences();
        initial.setPreferences(newStart, newEnd, energy);
        System.out.println("After: " + initial.toString());
    }

    public HashMap<StationInfo, EVMessage> getAnswers () {
        return answers;
    }

    public StationReceiver getAcceptedStation() {
        return acceptedStation;
    }

    public StrategyPreferences getStrategyPreferences() { return strategyPreferences; }

    public Preferences getAcceptedPreferences() {
        return acceptedPreferences;
    }

    public boolean isToBeServiced() {
        return isToBeServiced;
    }

    public boolean isServiced() {
        return serviced;
    }

    public String toString() {
        return "EVStrategy: \n" +
                "\t\tStart: " + strategyPreferences.getStart() + ", " +
                " End: " + strategyPreferences.getEnd() + ", " +
                " Energy: " + strategyPreferences.getEnergy() + ", " +
                " Rounds: " + strategyPreferences.getRounds() + ", " +
                " Priority: " + strategyPreferences.getPriority();
    }
}
