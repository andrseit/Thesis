package agents.station.communication;

public enum StationMessage {
    STATION_HAS_SUGGESTION ("offers_suggestion"),
    STATION_NEXT_ROUND_SUGGESTION ("will_suggest_in_next_round"),
    STATION_HAS_NO_SUGGESTION ("no_suggestion"),
    STATION_FUTURE_SLOT_SUGGESTION ("future_slot_suggestion");

    private String description;
    StationMessage (String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
