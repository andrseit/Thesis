package evs.communication;

import station.StationInfo;
import station.SuggestionMessage;

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
        //System.out.println("EV receives station's suggestion: " + message.toString());
        messages.add(message);
    }
}
