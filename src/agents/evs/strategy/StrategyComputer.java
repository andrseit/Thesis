package agents.evs.strategy;

import agents.evs.EVInfo;
import agents.evs.Preferences;
import agents.station.SuggestionMessage;
import agents.station.communication.StationMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Thesis on 12/1/2018.
 * Used to mainly to produce a list of comparable suggestions, so then the EVStrategy class
 * decides which is the best option
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
            //if (!(message.getStart() == -1) && !(message.getStart() == -3)) {
            if (message.getMessageType() == StationMessage.STATION_HAS_SUGGESTION) {
                //System.out.println("\t-I have a suggestion from agents.station No. " + message.getStationInfo().getId());
                int preferences_distance = 0;
                /*
                if (!hasSuggestion(message)) {
                    preferences_distance = Integer.MAX_VALUE;
                    comparable_suggestions.add(new ComparableSuggestion(0, 0, 0, preferences_distance, message.getStationInfo()));
                } else { */
                    boolean withingRange = true;
                    if (!this.isWithinInitial(message, initial_prefs)) {
                        withingRange = this.checkWithinRange(initial_prefs, message); // strategy range
                        if (withingRange)
                            preferences_distance = this.computePreferencesDistance(message, initial_prefs);
                        else {
                            comparable_suggestions.add(new ComparableSuggestion(0, 0, 0, -1, message.getStationInfo()));
                        }
                    }
                    if (withingRange) {
                        int price = message.getCost();
                        int total_distance = this.computeTotalDistance(message);
                        int windowRange = this.computeWindowRange(message);
                        comparable_suggestions.add(new ComparableSuggestion(total_distance, price, windowRange, preferences_distance, message.getStationInfo()));
                    }
                //}
            } else if (message.getMessageType() == StationMessage.STATION_NEXT_ROUND_SUGGESTION) {
                //System.out.println("\t-Station No. " + message.getStationInfo().getId() + " will offer me a suggestion in the next round of conversation.");
                comparable_suggestions.add(new ComparableSuggestion(0, 0, 0, Integer.MAX_VALUE, message.getStationInfo()));
            }
            else if (message.getMessageType() == StationMessage.STATION_HAS_NO_SUGGESTION){
                //System.out.println("\t-Station No. " + message.getStationInfo().getId() + " has no suggestion.");
                comparable_suggestions.add(new ComparableSuggestion(0, 0, 0, -2, message.getStationInfo()));
            } else if (message.getMessageType() == StationMessage.STATION_FUTURE_SLOT_SUGGESTION) {
                // means no offer yet - NEO 18/2/2018
                //System.out.println("\t-Station No. " + message.getStationInfo().getId() + " will offer me a suggestion in a future slot.");
                comparable_suggestions.add(new ComparableSuggestion(0, 0, 0, -3, message.getStationInfo()));
            }
        }
        orderMessages(comparable_suggestions, strategy_preferences.getPriority());

        return comparable_suggestions;
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
            messages.sort(Comparator.comparing(ComparableSuggestion::getPreferencesDistance)
                    .thenComparing(ComparableSuggestion::getPrice)
                    .thenComparingInt(ComparableSuggestion::getTotalDistance)
                    .thenComparing(ComparableSuggestion::getWindowRange));
        } else {
            messages.sort(Comparator.comparing(ComparableSuggestion::getPreferencesDistance)
                    .thenComparing(ComparableSuggestion::getTotalDistance)
                    .thenComparingInt(ComparableSuggestion::getPrice)
                    .thenComparing(ComparableSuggestion::getWindowRange));
        }

        // check if some offers are exactly the same to put them in random order
        // so that the first agents.station won't have advantage
        ArrayList<ComparableSuggestion> randomComparableSuggestions = new ArrayList<>();
        randomComparableSuggestions.add(messages.get(0));
        for (int i = 1; i < messages.size(); i++) {
            if (sameOffer(messages.get(i), randomComparableSuggestions.get(0)))
                randomComparableSuggestions.add(messages.get(i));
        }

        Collections.shuffle(randomComparableSuggestions);
        for (int i = 0; i < randomComparableSuggestions.size(); i++) {
            messages.remove(i);
            messages.add(i, randomComparableSuggestions.get(i));
        }
    }

    private boolean sameOffer (ComparableSuggestion s1, ComparableSuggestion s2) {
        return s1.getPreferencesDistance() == s1.getPreferencesDistance() && s1.getTotalDistance() == s2.getTotalDistance()
                && s1.getPrice() == s2.getPrice() && s1.getWindowRange() == s2.getWindowRange();
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
        int stationX = message.getStationInfo().getLocationX();
        int stationY = message.getStationInfo().getLocationY();

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
