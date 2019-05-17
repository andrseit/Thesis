package main.experiments.parameters;

public class StationsParameters {

    private int stationsNumber;
    private int maxChargers;

    public StationsParameters(int stationsNumber, int maxChargers) {
        this.stationsNumber = stationsNumber;
        this.maxChargers = maxChargers;
    }

    public int getStationsNumber() {
        return stationsNumber;
    }

    public int getMaxChargers() {
        return maxChargers;
    }
}
