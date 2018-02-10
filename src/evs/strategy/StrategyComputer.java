package evs.strategy;

import evs.EVInfo;
import evs.Preferences;
import station.SuggestionMessage;

import java.util.ArrayList;
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


    public ArrayList<ComparableSuggestion> produceComparableSuggestions(ArrayList<SuggestionMessage> messages) {

        Preferences initial_prefs = info.getPreferences();
        ArrayList<ComparableSuggestion> comparable_suggestions = new ArrayList<>();

        for (SuggestionMessage message : messages) {
            if (!(message.getStart() == -1)) {
                int preferences_distance = 0;
                if (!hasSuggestion(message)) {
                    preferences_distance = Integer.MAX_VALUE;
                    comparable_suggestions.add(new ComparableSuggestion(0, 0, 0, preferences_distance, message.getStationAddress()));
                } else {
                    boolean withingRange = true;
                    if (!this.isWithinInitial(message, initial_prefs)) {
                        withingRange = this.checkWithinRange(initial_prefs, message);
                        if (withingRange)
                            preferences_distance = this.computePreferencesDistance(message, initial_prefs);
                        else {
                            comparable_suggestions.add(new ComparableSuggestion(0, 0, 0, -1, message.getStationAddress()));
                        }
                    }
                    if (withingRange) {
                        int price = message.getCost();
                        int total_distance = this.computeTotalDistance(message);
                        int windowRange = this.computeWindowRange(message);
                        comparable_suggestions.add(new ComparableSuggestion(total_distance, price, windowRange, preferences_distance, message.getStationAddress()));
                    }
                }
            } else {
                comparable_suggestions.add(new ComparableSuggestion(0, 0, 0, -2, message.getStationAddress()));
            }
        }
        orderMessages(comparable_suggestions, strategy_preferences.getPriority());

        return comparable_suggestions;
    }

    /**
     * Checks if there is a suggestion or the values are max int so it will ask
     * for a new one
     *
     * @param message
     * @return
     */
    private boolean hasSuggestion(SuggestionMessage message) {
        return !(message.getStart() == Integer.MAX_VALUE && message.getEnd() == Integer.MAX_VALUE
                && message.getEnergy() == 0);
    }

    private int computePreferencesDistance(SuggestionMessage message, Preferences initial) {
        int start = initial.getStart();
        int end = initial.getEnd();
        int sStart = message.getStart();
        int sEnd = message.getEnd();
        int window_diff = 0, energy_dif;
        int start_dif = 0, end_dif = 0;

        if (sEnd < end) {
            window_diff += end - sEnd;
        } else if (sStart > start) {
            window_diff += sStart - start;
        }
        int oldRange = end - start + 1;
        int newRange = sEnd - sStart + 1;
        if (newRange > oldRange)
            window_diff += newRange - oldRange;

        /*
        if (!isInRange(start, end, sStart))
            start_dif = Math.abs(message.getStart() - initial.getStart());
        if (!isInRange(start, end, sEnd))
            end_dif = Math.abs(message.getEnd() - initial.getEnd());
        */
        energy_dif = Math.abs(message.getEnergy() - initial.getEnergy());
        return window_diff + energy_dif;
    }

    /**
     * Checks if the new value is within the initial slot range
     * then the difference is 0
     *
     * @return
     */
    private boolean isInRange(int start, int end, int suggested) {
        return suggested >= start && suggested <= end;
    }

    private boolean isWithinInitial(SuggestionMessage message, Preferences initial) {
        return message.getStart() >= initial.getStart() && message.getEnd() <= initial.getEnd() && message.getEnergy() == initial.getEnergy();
    }

    /**
     * Sorts the messages of the stations based on the priority of the ev (price or distance)
     *
     * @param messages
     * @param priority
     */
    private void orderMessages(ArrayList<ComparableSuggestion> messages, String priority) {
        if (priority.equals("price")) {
            messages.sort(Comparator.comparing((ComparableSuggestion s) -> s.getPreferencesDistance())
                    .thenComparing(p -> p.getPrice())
                    .thenComparingInt(p -> p.getTotalDistance())
                    .thenComparing(p -> p.getWindowRange()));
        } else {
            messages.sort(Comparator.comparing((ComparableSuggestion s) -> s.getPreferencesDistance())
                    .thenComparing(p -> p.getTotalDistance())
                    .thenComparingInt(p -> p.getPrice())
                    .thenComparing(p -> p.getWindowRange()));
        }
    }

    /**
     * Checks if a suggestion message is in the given bounds of the strategy,
     * if it is of, reject it immediately
     *
     * @return
     */
    private boolean checkWithinRange(Preferences initial, SuggestionMessage message) {
        return message.getStart() >= strategy_preferences.getStart()
                && message.getEnd() <= strategy_preferences.getEnd()
                && message.getEnergy() >= strategy_preferences.getEnergy() && !tooWide(initial, message);
    }

    /**
     * if the window is a lot wider than desired
     * @return
     */
    private boolean tooWide (Preferences initial, SuggestionMessage message) {
        double initialRange = initial.getEnd() - initial.getStart() + 1;
        double messageRange = message.getEnd() - message.getStart() + 1;
        double sRange = strategy_preferences.getRange();
        return !((messageRange / initialRange) < sRange);
    }

    private int computeTotalDistance(SuggestionMessage message) {
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

    private int computeWindowRange (SuggestionMessage message) {
        int start = message.getStart();
        int end = message.getEnd();
        return end - start;
    }
}
