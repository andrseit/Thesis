package evs.strategy;

import evs.EVInfo;
import evs.Preferences;
import station.SuggestionMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Thesis on 12/1/2018.
 */
class StrategyComputer {

    private EVInfo info;
    private StrategyPreferences strategy_preferences;

    public StrategyComputer(EVInfo info, StrategyPreferences strategy_preferences) {
        this.info = info;
        this.strategy_preferences = strategy_preferences;
    }


    public ArrayList<ComparableSuggestion> produceComparableSuggestions (ArrayList<SuggestionMessage> messages) {

        Preferences initial_prefs = info.getPreferences();
        ArrayList<ComparableSuggestion> comparable_suggestions = new ArrayList<>();

        for (SuggestionMessage message: messages) {
            int preferences_distance = 0;
            if (!hasSuggestion(message)) {
                preferences_distance = Integer.MAX_VALUE;
                comparable_suggestions.add(new ComparableSuggestion(0, 0, preferences_distance, message.getStationAddress()));
            }
            else
            {
                boolean withingRange = true;
                if (!this.isWithinInitial(message, initial_prefs)) {
                    withingRange = this.checkWithinRange(message);
                    if (withingRange)
                        preferences_distance = this.computePreferencesDistance(message, initial_prefs);
                    else
                       comparable_suggestions.add(new ComparableSuggestion(0,0, -1, message.getStationAddress()));
                }
                if (withingRange) {
                    int price = message.getCost();
                    int total_distance = this.computeTotalDistance(message);
                    comparable_suggestions.add(new ComparableSuggestion(total_distance, price, preferences_distance, message.getStationAddress()));
                }
            }
        }
        System.out.println(comparable_suggestions.size());
        orderMessages(comparable_suggestions, strategy_preferences.getPriority());

        return comparable_suggestions;
    }

    /**
     * Checks if there is a suggestion or the values are max int so it will ask
     * for a new one
     * @param message
     * @return
     */
    private boolean hasSuggestion (SuggestionMessage message) {
        return !(message.getStart() == Integer.MAX_VALUE && message.getEnd() == Integer.MAX_VALUE
                && message.getEnergy() == 0);
    }

    private int computePreferencesDistance (SuggestionMessage message, Preferences initial) {
        int start_dif = Math.abs(message.getStart() - initial.getStart());
        int end_dif = Math.abs(message.getEnd() - initial.getEnd());
        int energy_dif = Math.abs(message.getEnergy() - initial.getEnergy());
        return start_dif + end_dif + energy_dif;
    }

    private boolean isWithinInitial (SuggestionMessage message, Preferences initial) {
        return message.getStart() >= initial.getStart() && message.getEnd() <= initial.getEnd() && message.getEnergy() == initial.getEnergy();
    }

    /**
     * Sorts the messages of the stations based on the priority of the ev (price or distance)
     * @param messages
     * @param priority
     */
    private void orderMessages (ArrayList<ComparableSuggestion> messages, String priority) {
        if (priority.equals("price"))
        {
            Collections.sort(messages, Comparator.comparing((ComparableSuggestion s)->s.getPreferencesDistance())
                    .thenComparing(p->p.getPrice())
                    .thenComparingInt(p->p.getTotalDistance()));
        } else {
            Collections.sort(messages, Comparator.comparing((ComparableSuggestion s)->s.getPreferencesDistance())
                    .thenComparing(p->p.getTotalDistance())
                    .thenComparingInt(p->p.getPrice()));
        }
    }

    /**
     * Checks if a suggestion message is in the given bounds of the strategy,
     * if it is of, reject it immediately
     * @return
     */
    private boolean checkWithinRange (SuggestionMessage message) {
        return message.getStart() >= strategy_preferences.getStart()
                && message.getEnd() <= strategy_preferences.getEnd()
                && message.getEnergy() >= strategy_preferences.getEnergy();
    }

    private int computeTotalDistance (SuggestionMessage message) {
        int startX = info.getLocationX();
        int startY = info.getLocationY();
        int endX = info.getFinalLocationX();
        int endY = info.getFinalLocationY();
        int stationX = message.getStationAddress().getLocationX();
        int stationY = message.getStationAddress().getLocationY();

        int to_station_distance = Math.abs(startX - stationX) + Math.abs(startY - stationY);
        int to_destination_distance = Math.abs(stationX - endX) + Math.abs(stationY - endY);

        return to_station_distance + to_destination_distance;
    }
}
