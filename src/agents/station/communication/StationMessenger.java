package agents.station.communication;

import agents.station.EVObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Thesis on 21/1/2019.
 */
public class StationMessenger {

    private StationSender sender;
    private StationReceiver receiver;

    public StationMessenger(int id, ArrayList<EVObject> incomingMessages, HashMap<EVObject, Integer> incomingAnswers) {
        sender = new StationSender();
        receiver = new StationReceiver(id, incomingMessages, incomingAnswers);
    }

    public StationReceiver getAddress () {
        return receiver;
    }

    public StationSender getSender () { return sender; }

}
