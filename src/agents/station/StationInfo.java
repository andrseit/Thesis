package agents.station;

import agents.station.communication.StationReceiver;

public class StationInfo {

    private int id;
    // where the agents.station is on the map so that the distance
    // is going to be calculated
    private int location_x;
    private int location_y;
    private int charger_number;
    private StationReceiver communicationPort;

    public StationInfo(int id, int location_x, int location_y, int charger_number, StationReceiver communicationPort) {
        this.id = id;
        this.location_x = location_x;
        this.location_y = location_y;
        this.charger_number = charger_number;
        this.communicationPort = communicationPort;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLocationX() {
        return location_x;
    }

    public int getLocationY() {
        return location_y;
    }

    public String toString() {
        return ("-> Station_" + id) +
                " (" + location_x + ", " + location_y + ") : " +
                charger_number + " chargers.";
    }

    public int getId() {
        return id;
    }

    public StationReceiver getCommunicationPort () { return communicationPort; }

    public int getChargerNumber() {
        return charger_number;
    }
}
