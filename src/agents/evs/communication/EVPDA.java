package agents.evs.communication;

import agents.evs.EVInfo;
import agents.station.SuggestionMessage;
import agents.station.communication.StationReceiver;

import java.util.ArrayList;

/**
 * Created by Thesis on 21/1/2019.
 */
public class EVPDA {

    private EVMessenger messenger;

    public EVPDA(ArrayList<SuggestionMessage> messages) {
        this.messenger = new EVMessenger(messages);
    }

    public EVMessenger getMessenger () {
        return messenger;
    }

    public void sendRequest (EVInfo info, Integer messageType, StationReceiver station) {
        messenger.getSender().sendRequest(info, messageType, station);
    }



}
