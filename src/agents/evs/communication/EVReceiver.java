package agents.evs.communication;

import agents.station.SuggestionMessage;

import java.util.ArrayList;

/**
 * Created by Thesis on 25/1/2019.
 * This is used by a station in order to send messages to the EVs
 * By using this class the station can send a message, without having
 * access to the whole EVMessenger object
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
