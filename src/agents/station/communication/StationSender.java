package agents.station.communication;

import agents.evs.communication.EVReceiver;
import agents.station.SuggestionMessage;

/**
 * Created by Thesis on 21/1/2019.
 */
public class StationSender {

    public void sendSuggestion (SuggestionMessage message, EVReceiver receiver) {
        receiver.receiveSuggestion(message);
    }

}
