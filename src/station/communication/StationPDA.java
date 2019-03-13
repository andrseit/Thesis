package station.communication;

import evs.communication.EVReceiver;
import station.EVObject;
import station.SuggestionMessage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Thesis on 21/1/2019.
 * This class represents the interface that is the intermediary between the EVs and the station
 * It receives requests and redirects them to the station
 * It also contains the station's information
 */
public class StationPDA {

    private int location_x;
    private int location_y;
    private StationMessenger messenger;

    public StationPDA (int id, int location_x, int location_y, ArrayList<EVObject> incomingMessages, HashMap<EVObject, Integer> incomingAnswers) {
        messenger = new StationMessenger(id, incomingMessages, incomingAnswers);
        this.location_x = location_x;
        this.location_y = location_y;
    }

    public StationMessenger getMessenger () {
        return messenger;
    }

    public void sendMessage (SuggestionMessage message, EVReceiver ev) {
        messenger.getSender().sendSuggestion(message, ev);
    }
}
