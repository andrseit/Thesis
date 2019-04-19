package agents.station;

import agents.evs.Preferences;
import agents.station.communication.StationReceiver;

/**
 * It contains basic information of suggestion - exactly what hte ev should
 * be aware of
 * also it contains the address of the agents.station object so that an
 * accept/refuse messageType should be delivered
 */
public class SuggestionMessage extends Preferences {

    private StationInfo station;
    private int cost;
    private int messageType; // a messageType that accompanies the offer - e.g. STATION_MESSAGE_REJECT

    public SuggestionMessage(StationInfo station, Preferences preferences, int cost, Integer messageType) {
        super(preferences.getStart(), preferences.getEnd(), preferences.getEnergy());
        this.station = station;
        this.cost = cost;
        this.messageType = messageType;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public StationInfo getStationInfo() {
        return station;
    }

    public Integer getMessageType () { return messageType; }

    public StationReceiver getStationAddress () { return station.getCommunicationPort(); }

    public String preferencesToString () {
        return start + "-" + end + "/" + energy;
    }

    public String toString() {
        return "Station_" + station.getId() + ": " +
                start + "-" + end + "/" + energy + " -cost: " + cost;
    }
}
