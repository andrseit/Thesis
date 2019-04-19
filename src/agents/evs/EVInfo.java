package agents.evs;

import agents.evs.communication.EVReceiver;

public class EVInfo {

    private int id, bid;
    private Preferences preferences;
    private int locationX, locationY, final_locationX, final_locationY;
    private EVReceiver communicationPort;

    public EVInfo(int id, int x, int y, int finalX, int finalY, int start, int end, int energy, int bid, int max_distance, EVReceiver communicationPort) {
        this.locationX = x;
        this.locationY = y;
        this.final_locationX = finalX;
        this.final_locationY = finalY;
        this.id = id;
        this.bid = bid;
        this.communicationPort = communicationPort;
        preferences = new Preferences(start, end, energy);
        preferences.setMaxDistance(max_distance);
    }

    public int getId() {
        return id;
    }

    public void updatePreferences (int start, int end, int energy) {
        preferences.setPreferences(start, end, energy);
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public String toString() {
        return "-------| EV No. " + id + " |--------\n"
                + "Initial Location: <" + locationX + ", " + locationY + ">"
                + " Destination: <" + final_locationX + ", " + final_locationY + ">"
                + "\n\t*Preferences:\n\t\t" + "Start: " + preferences.getStart() + ", " + " End: " + preferences.getEnd()+ ", "
                + " Energy: " + preferences.getEnergy() + ", " + " Distance: " + preferences.getMaxDistance();
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

    public EVReceiver getCommunicationPort() {
        return communicationPort;
    }

}
