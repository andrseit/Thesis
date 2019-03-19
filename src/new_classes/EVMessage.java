package new_classes;

import evs.EVInfo;
import station.communication.StationReceiver;

/**
 * Created by Thesis on 18/3/2019.
 */
public class EVMessage {
    private EVInfo info;
    private Integer answer;
    private StationReceiver receiver;

    public EVMessage(EVInfo info, Integer answer, StationReceiver receiver) {
        this.info = info;
        this.answer = answer;
        this.receiver = receiver;
    }

    public String toString () {
        return "EV No " + info.getId() + ", answer: " + answer + ", station: " + receiver.getStationId();
    }
}
