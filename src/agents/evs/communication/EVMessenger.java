package agents.evs.communication;

import agents.evs.EVInfo;
import agents.station.StationInfo;
import agents.station.SuggestionMessage;
import agents.station.communication.StationReceiver;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Thesis on 25/1/2019.
 */
public class EVMessenger {

    private EVReceiver receiver;
    private ArrayList<SuggestionMessage> messages;

    public EVMessenger() {
        messages = new ArrayList<>();
        receiver = new EVReceiver(messages);
    }

    public void sendMessage (EVInfo info, Integer messageType, StationReceiver station) {
        station.receiveRequest(info, messageType);
    }

    public void sendAnswers (EVInfo info, HashMap<StationInfo, Integer> answers) {
        if (!answers.isEmpty())
            for (StationInfo s : answers.keySet())
                sendMessage(info, answers.get(s), s.getCommunicationPort());
    }

    public ArrayList<SuggestionMessage> getMessages () { return messages; }

    public EVReceiver getReceiver () {
        return receiver;
    }
}
