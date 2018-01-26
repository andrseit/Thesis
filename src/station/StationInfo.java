package station;

import evs.EVInfo;
import evs.Preferences;
import station.offline.AbstractStation;

/**
 * Created by Thesis on 8/1/2018.
 */
public class StationInfo {

    private int id;
    // where the station is on the map so that the distance
    // is going to be calculated
    private int location_x;
    private int location_y;
    private int charger_number;

    private AbstractStation station;

    public StationInfo(int id, int location_x, int location_y, int charger_number) {
        this.id = id;
        this.location_x = location_x;
        this.location_y = location_y;
        this.charger_number = charger_number;
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
        return ("Station_" + id) +
                " (" + location_x + ", " + location_y + ") : " +
                charger_number + " chargers.";
    }

    public void setStation(AbstractStation station) {
        this.station = station;
    }

    public int getId() {
        return id;
    }

    public void request(EVInfo info) {
        Preferences p = info.getPreferences();
        EVObject ev = new EVObject();
        ev.addEVPreferences(p.getStart(), p.getEnd(), info.getBid(), p.getEnergy());
        ev.setID(info.getId());
        ev.setXY(info.getLocationX(), info.getLocationY());
        ev.setEVAddress(info.getObjectAddress());
        station.addEVBidder(ev);
    }

    /**
     * This is used to receive a message, if an ev accepted the offer or not
     */
    public void checkIn(EVInfo ev, int state) {
        station.markEVBidder(ev.getId(), state);
    }

    public int getChargerNumber() {
        return charger_number;
    }
}
