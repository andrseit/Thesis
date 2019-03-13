package station;

import evs.Preferences;
import station.communication.StationReceiver;

/**
 * It contains basic information of suggestion - exactly what hte ev should
 * be aware of
 * also it contains the address of the station object so that an
 * accept/refuse messageType should be delivered
 */
public class SuggestionMessage extends Preferences {

    private StationInfo station;
    private int cost;
    private int messageType; // a messageType that accompanies the offer - e.g. STATION_MESSAGE_REJECT

    public SuggestionMessage(StationInfo station, Preferences preferences, int cost, Integer messageType) {
        this.station = station;
        this.start = preferences.getStart();
        this.end = preferences.getEnd();
        this.energy = preferences.getEnergy();
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

    public String toString() {
        return "Station_" + station.getId() + ": " +
                start + "-" + end + "/" + energy + " -cost: " + cost;
    }
}
