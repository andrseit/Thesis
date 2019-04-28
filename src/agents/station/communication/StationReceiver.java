package agents.station.communication;

import agents.evs.EVInfo;
import agents.evs.Preferences;
import agents.station.EVObject;
import agents.evs.communication.EVMessage;

import java.util.HashMap;

/**
 * Created by Thesis on 21/1/2019.
 * Not exactly receiver, more like communicator, intermediary
 */
public class StationReceiver {

    /**
     * Convert into agents.station info which can be abstract
     */
    private int id;
    private int location_x;
    private int location_y;

    // Integer is for the answer
    // Instead of EVObject I could keep only the ID, but I thought that later maybe
    // the agents.evs would be able to change their preferences
    private HashMap<EVObject, EVMessage> incomingAnswers;

    public StationReceiver(int id, HashMap<EVObject, EVMessage> incomingAnswers) {
        this.id = id;
        this.incomingAnswers = incomingAnswers;
    }

    public void receiveRequest (EVInfo evInfo, EVMessage message) {
        incomingAnswers.put(createEVObject(evInfo), message);
    }

    // just for housekeeping
    private EVObject createEVObject (EVInfo evInfo) {
        Preferences p = evInfo.getPreferences();
        EVObject ev = new EVObject(p);
        //ev.addEVPreferences(p.getStart(), p.getEnd(), evInfo.getBid(), p.getEnergy());
        ev.setID(evInfo.getId()); // this is the universal id - like the license plate
        ev.setXY(evInfo.getLocationX(), evInfo.getLocationY());
        ev.setStationId(incomingAnswers.size());
        ev.setEvReceiver(evInfo.getCommunicationPort());
        return ev;
    }

    /**
     * Returns the id of the agents.station, so that the EV knows with who it communicates
     * @return
     */
    public int getStationId () {
        return id;
    }

}
