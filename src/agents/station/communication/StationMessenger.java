package agents.station.communication;

import agents.evs.communication.EVReceiver;
import agents.station.EVObject;
import agents.station.SuggestionMessage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Thesis on 21/1/2019.
 */
public class StationMessenger {

    private StationReceiver receiver;

    private HashMap<EVObject, Integer> incomingAnswers;

    public StationMessenger(int id) {
        incomingAnswers = new HashMap<>();
        receiver = new StationReceiver(id, incomingAnswers);
    }

    public void sendSuggestion (SuggestionMessage message, EVReceiver ev) { ev.receiveSuggestion(message); }

    public void sendSuggestions (ArrayList<EVObject> receivers) {
        for (EVObject ev: receivers)
            sendSuggestion(ev.getSuggestionMessage(), ev.getEvReceiver());
    }

    public HashMap<EVObject, Integer> getIncomingAnswers() { return incomingAnswers; }

    public StationReceiver getReceiver() {
        return receiver;
    }

}
