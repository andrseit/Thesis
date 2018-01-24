package evs;

/**
 * Created by Thesis on 8/1/2018.
 */
public class EVInfo {

    private int id, bid;
    private Preferences preferences;
    private int locationX, locationY, final_locationX, final_locationY;
    private EV object_address;

    public EVInfo(int id, int x, int y, int finalX, int finalY, int start, int end, int energy, int bid, int max_distance) {
        this.locationX = x;
        this.locationY = y;
        this.final_locationX = finalX;
        this.final_locationY = finalY;
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
        return "Initial Location: <" + locationX + ", " + locationY + ">"
                +" Destination: <" + final_locationX + ", " + final_locationY + ">"
                + "\n\t*Preferences:\n\t\t" + "Start: " + preferences.getStart() + " End: " + preferences.getEnd()
                + " Energy: " + preferences.getEnergy() + " Distance: " + preferences.getMaxDistance();
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

    public int getFinalLocationX() {
        return final_locationX;
    }

    public int getFinalLocationY() {
        return final_locationY;
    }
}
