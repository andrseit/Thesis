package agents.evs;

import agents.evs.communication.EVMessenger;
import agents.evs.strategy.Strategy;
import agents.evs.strategy.StrategyPreferences;
import user_interface.EVState;

public class EV {

    private EVInfo info;
    private EVState state;
    private EVMessenger messenger;
    private Strategy strategy;


    public EV(EVParameters evParameters, StrategyPreferences strategyPreferences) {
        messenger = new EVMessenger();

        info = new EVInfo(evParameters.getId(), evParameters.getX(), evParameters.getY(), evParameters.getFinalX(),
                evParameters.getFinalY(), evParameters.getStart(), evParameters.getEnd(), evParameters.getEnergy(),
                evParameters.getBid(), evParameters.getMax_distance(), messenger.getReceiver());

        state = new EVState(evParameters.getId());

        strategy = new Strategy(strategyPreferences, state);

    }

    public boolean hasSuggestions() {
        return !messenger.getMessages().isEmpty();
    }

    public EVInfo getInfo () { return info; }

    public Strategy getStrategy () { return strategy; }

    public EVMessenger getMessenger () { return messenger; }

    public EVState getState () {
        return state;
    }

    public String toString() {
        return "**************" + "\n" + info.toString() + "\n\t*" + strategy.toString() + "\n\t* Informs: " + "\n\t\tSlot: " + strategy.getStrategyPreferences().getInformSlot();
    }
}
