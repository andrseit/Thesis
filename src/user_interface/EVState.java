package user_interface;

import agents.evs.communication.EVMessage;

import java.util.HashMap;

/**
 * Created by Thesis on 2/4/2019.
 */
public class EVState {

    private String ev;
    // key: Station ID, value: list of messages - consider making this a real list
    private HashMap<String, StringBuilder> suggestions;
    private String stationPrefix = "agents/station";

    public EVState(int evID) {
        ev = "ev_" + evID;
        suggestions = new HashMap<>();
    }

    public void addSuggestion (int stationID, String suggestion) {
        String stationStr = stationPrefix + stationID;
        String suggestionStr = suggestion;
        if (suggestion.equals("-1--1/-1"))
            suggestionStr = "Not Avail.";
        if (suggestions.keySet().contains(stationStr))
            suggestions.get(stationStr).append("->(").append(suggestionStr).append(")");
        else
            suggestions.put(stationStr, new StringBuilder("(" + suggestionStr + ")"));
    }

    public void addAnswer (int stationID, EVMessage answer) {
        String stationStr = stationPrefix + stationID;
        suggestions.get(stationStr).append(":[").append(answer.getDescription()).append("]");
    }

    public String toString () {
        StringBuilder str = new StringBuilder("---- " + ev + " ----\n");
        for (String station: suggestions.keySet()) {
            str.append(station).append(": ").append(suggestions.get(station)).append("\n");
        }
        return str.toString();
    }
}
