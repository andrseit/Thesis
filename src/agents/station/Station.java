package agents.station;

import agents.station.communication.StationMessenger;
import agents.station.optimize.Optimizer;
import agents.station.statistics.StationStatistics;
import agents.station.strategy.StationStrategy;
import user_interface.StationState;
import agents.station.communication.StationReceiver;

/**
 * Created by Thesis on 21/1/2019.
 */
public class Station {

    private StationStatistics statistics;
    private StationState state;

    private StationInfo info;
    private StationMessenger messenger;
    private StationStrategy strategy;

    public Station (int id, int x, int y, int chargersNumber, Optimizer optimizer,
                    Optimizer alternativesOptimizer, boolean usesAlternatives, int[] price, int slotsNumber) {

        messenger = new StationMessenger(id);
        info = new StationInfo(id, x, y, chargersNumber, messenger.getReceiver());

        statistics = new StationStatistics(info.getId(), chargersNumber, slotsNumber);
        state = new StationState(info.getId(), slotsNumber);

        strategy = new StationStrategy(optimizer, alternativesOptimizer, price, slotsNumber,
                    chargersNumber, usesAlternatives, statistics, state);
    }

    public StationMessenger getMessenger() { return messenger; }

    public StationReceiver getCommunicationPort () {
        return messenger.getReceiver();
    }

    public StationInfo getInfo() { return info; }

    public StationStrategy getStrategy() { return strategy; }

    public StationStatistics getStatistics () {
        return statistics;
    }

    public StationState getState () {
        return state;
    }

    public String toString () {
        return info.toString();
    }

}