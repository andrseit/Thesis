package agents.evs.communication;

import agents.station.SuggestionMessage;

import java.util.ArrayList;

/**
 * Created by Thesis on 25/1/2019.
 */
public class EVReceiver {

    private ArrayList<SuggestionMessage> messages;

    public EVReceiver(ArrayList<SuggestionMessage> messages) {
        this.messages = messages;
    }

    public void receiveSuggestion (SuggestionMessage message) {
        //System.out.println("EV receives agents.station's suggestion: " + message.toString());
        messages.add(message);
    }
}
