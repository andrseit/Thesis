package evs.strategy;

import station.StationInfo;

class ComparableSuggestion {

    private int total_distance;
    private int price;
    private int windowRange;
    private int preferences_distance;

    private StationInfo station;

    public ComparableSuggestion(int total_distance, int price, int windowRange, int preferences_distance, StationInfo station) {
        this.total_distance = total_distance;
        this.price = price;
        this.windowRange = windowRange;
        this.preferences_distance = preferences_distance;
        this.station = station;
    }

    public int getTotalDistance() {
        return total_distance;
    }

    public int getPrice() {
        return price;
    }

    public int getWindowRange () {
        return windowRange;
    }

    public int getPreferencesDistance() {
        return preferences_distance;
    }

    public StationInfo getStationAddress() {
        return station;
    }

    public String toString() {
        return "Station_" + station.getId() + ": Price: " + price + " Window Range: " + windowRange + "  Distance: " + total_distance +
                "  Preferences distance: " + preferences_distance;
    }
}
