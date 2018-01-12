package station;

import evs.Preferences;

/**
 * Created by Thesis on 9/1/2018.
 * It contains basic information of suggestion - exactly what hte ev should
 * be aware of
 * also it contains the address of the station object so that an
 * accept/refuse message should be delivered
 */
public class SuggestionMessage extends Preferences {

    private StationInfo station;
    private int cost;

    public SuggestionMessage(StationInfo station, Preferences preferences) {
        this.station = station;
        this.start = preferences.getStart();
        this.end = preferences.getEnd();
        this.energy = preferences.getEnergy();
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public StationInfo getStationAddress() {
        return station;
    }

    public String toString () {
        return "Station_" + station.getId() + ": " +
                start + "-" + end + "/" + energy + " -cost: " + cost;
    }
}
