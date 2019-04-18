package agents.station.communication;

import agents.evs.EVInfo;
import agents.evs.Preferences;
import agents.station.EVObject;

import java.util.ArrayList;
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

    // I was thinking of integrating those two lists and have only one handler
    // but then I realised that it would be inconvinient to search everytime for requests/answers
    // then again I could create one handler but two lists
    private ArrayList<EVObject> incomingRequests;
    // Integer is for the answer
    // Instead of EVObject I could keep only the ID, but I thought that later maybe
    // the agents.evs would be able to change their preferences
    private HashMap<EVObject, Integer> incomingAnswers;

    public StationReceiver(int id, ArrayList<EVObject> incomingRequests, HashMap<EVObject, Integer> incomingAnswers) {
        this.id = id;
        this.incomingRequests = incomingRequests;
        this.incomingAnswers = incomingAnswers;
    }

    public void receiveRequest (EVInfo evInfo, Integer message) {
        incomingAnswers.put(createEVObject(evInfo), message);
    }

    // just for housekeeping
    private EVObject createEVObject (EVInfo evInfo) {
        Preferences p = evInfo.getPreferences();
        EVObject ev = new EVObject();
        ev.addEVPreferences(p.getStart(), p.getEnd(), evInfo.getBid(), p.getEnergy());
        ev.setID(evInfo.getId()); // this is the universal id - like the license plate
        ev.setXY(evInfo.getLocationX(), evInfo.getLocationY());
        ev.setStationId(incomingRequests.size());
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
