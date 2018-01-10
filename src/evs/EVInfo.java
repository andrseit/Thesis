package evs;

/**
 * Created by Thesis on 8/1/2018.
 */
public class EVInfo {

    private int id, bid;
    private Preferences preferences;
    private int locationX, locationY;
    private EV object_address;

    public EVInfo(int id, int x, int y, int start, int end, int energy, int bid, int max_distance) {
        this.locationX = x;
        this.locationY = y;
        this.id = id;
        this.bid = bid;
        preferences = new Preferences();
        preferences.setStart(start);
        preferences.setEnd(end);
        preferences.setEnergy(energy);
        preferences.setMaxDistance(max_distance);
    }

    public EV getObjectAddress() {
        return object_address;
    }

    public void setObjectAddress(EV objectAddress) {
        this.object_address = objectAddress;
    }

    public int getId() {
        return id;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public String toString () {
        return ("Ev_" + id + ": " + preferences.getStart() + " - " +
        preferences.getEnd() + " / " + preferences.getEnergy() + ", located at <" +
        locationX + ", " + locationY + ">");
    }

    public int getBid() {
        return bid;
    }

    public int getLocationX() {
        return locationX;
    }

    public int getLocationY() {
        return locationY;
    }


}
