package agents.evs.communication;

import agents.station.SuggestionMessage;

import java.util.ArrayList;

/**
 * Created by Thesis on 25/1/2019.
 */
public class EVMessenger {

    private EVSender sender;
    private EVReceiver receiver;

    public EVMessenger(ArrayList<SuggestionMessage> messages) {
        sender = new EVSender();
        receiver = new EVReceiver(messages);
    }

    public EVReceiver getReceiver () {
        return receiver;
    }

    public EVSender getSender() {
        return sender;
    }
}
