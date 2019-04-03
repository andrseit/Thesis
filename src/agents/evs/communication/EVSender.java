package agents.evs.communication;

import agents.evs.EVInfo;
import agents.station.communication.StationReceiver;

/**
 * Created by Thesis on 25/1/2019.
 */
public class EVSender {

    public void sendRequest (EVInfo info, Integer messageType, StationReceiver station) {
        station.receiveRequest(info, messageType);
    }
}
