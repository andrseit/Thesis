package agents.evs;

import agents.evs.strategy.Strategy;
import agents.evs.communication.EVPDA;
import user_interface.EVState;
import agents.station.communication.StationReceiver;
import agents.evs.communication.EVReceiver;
import agents.station.StationInfo;
import agents.station.SuggestionMessage;
import various.IntegerConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class EV {

    private EVState state;

    private EVPDA pda;
    // maybe this should go to the pda
    private ArrayList<SuggestionMessage> messages;

    private StationReceiver acceptedStation; // the agents.station with which the EV came to an agreement
    private boolean toBeServiced; // the EV is to be charged, but the time has not yet arrived
    private boolean serviced;// the ev has been successfully serviced
    private boolean delayed; // shows if the ev has made a deferral - we assume that an EV can only do that once, however it can be easily changed
                            // so that this can happen more than one time
                            // rename it to something that represents both deferral and cancellation


    private int informSlot;
    private EVInfo info;
    private Strategy strategy;
    private int bid;



    public EV(int id, int informSlot, int x, int y, int finalX, int finalY, int start, int end, int energy, int bid, int max_distance, Strategy strategy) {
        messages = new ArrayList<>();
        pda = new EVPDA(messages);
        this.informSlot = informSlot;
        info = new EVInfo(id, x, y, finalX, finalY, start, end, energy, bid, max_distance);
        info.setObjectAddress(this);
        info.setCommunicationPort(pda.getMessenger().getReceiver());
        this.bid = bid;
        this.strategy = strategy;

        state = new EVState(id);
    }

    public void evaluateSuggestions() {
        for (SuggestionMessage message: messages)
            state.addSuggestion(message.getStationInfo().getId(), message.preferencesToString());
        strategy.evaluate(messages, info);
    }

    public boolean hasSuggestions() {
        return !messages.isEmpty();
    }

    /**
     * the name means that this is a new method for the request
     * ev gets the agents.station list and sends a request
     * make it more sophisticated like the old one in the future
     * @param stations
     */
    public void newRequest (StationReceiver[] stations) {
        for (StationReceiver s: stations) {
            //System.out.println("EV No. " + info.getId() + " requests from Station No. " + s.getStationId());
            pda.sendRequest(info, IntegerConstants.EV_MESSAGE_REQUEST, s);
        }
    }

    /**
     * This method is used by the execution flow to determine if an EV is able to make a deferral
     * This is determined by a probability and the boolean variable: delayed
     * If the current time slot is equal to the EVs arrival time, then the EV is serviced
     * @param currentSlot
     * @param slotsNumber
     * @return
     */
    public Integer checkDelay(int currentSlot, int slotsNumber) {
        System.out.println("EV No " + info.getId() + "( " + currentSlot + ", " + info.getPreferences().getStart() + " )");
        Preferences preferences = info.getPreferences();
        System.out.println("Initial: " + preferences.toString());
        Random random = new Random();
        if (toBeServiced && random.nextInt(100) < 50 && !delayed) {
            System.out.println("I will delay or cancel!");
            if (random.nextInt() < 20) {
                // cancel
                System.out.println("I shall CANCEL my reservation!");
                delayed = true;
                toBeServiced = false;
                strategy.resetCharged();

                state.addSuggestion(acceptedStation.getStationId(), "-");
                state.addAnswer(acceptedStation.getStationId(), IntegerConstants.EV_UPDATE_CANCEL);
                return IntegerConstants.EV_UPDATE_CANCEL;
            } else {
                System.out.println("I shall DELAY my reservation!");
                // delay
                if (!(slotsNumber - currentSlot <= 0)) {
                    delayed = true;
                    strategy.resetRounds();
                    strategy.resetCharged();

                    state.addSuggestion(acceptedStation.getStationId(), "-");
                    state.addAnswer(acceptedStation.getStationId(), IntegerConstants.EV_UPDATE_DELAY);
                    return IntegerConstants.EV_UPDATE_DELAY;
                }
            }
        } else {
            System.out.println("I'll check in normally!");
            if (currentSlot == preferences.getStart()) {
                serviced = true;
                toBeServiced = false;
            }
        }
        System.out.println("Serviced: " + serviced);
        return -1;
    }

    public boolean isServiced () {
        return serviced;
    }

    public boolean isToBeServiced () {
        return toBeServiced;
    }

    /**
     * When an ev informs for a delay the new preferences should be within the accepted bounds
     * @param currentSlot: the time slot in which the execution is at the moment
     * @return preferences with delay
     */
    public void computeDelay (int currentSlot, int slotsNumber) {

        Preferences initial = info.getPreferences();
        int upperStartBound = Math.max((slotsNumber - initial.getEnergy()), (initial.getStart() + 1));
        int lowerStartBound = initial.getStart() + 1;

        System.out.println("Lower start: " + lowerStartBound + ", Max start: " + upperStartBound);
        Random random = new Random();
        int newStart = random.nextInt(upperStartBound - lowerStartBound + 1) + lowerStartBound;
        int energy = Math.min(initial.getEnergy(), (slotsNumber - newStart));
        int newEnd = newStart + Math.min(energy, slotsNumber - newStart) - 1;

        initial.setPreferences(newStart, newEnd, energy);
        System.out.println("After: " + initial.toString());
    }


    public void sendAnswers () {
        HashMap<StationInfo, Integer> answers = strategy.getAnswers();
        if (!answers.isEmpty()) {
            for (StationInfo s : answers.keySet()) {
                pda.sendRequest(info, answers.get(s), s.getCommunicationPort());

                state.addAnswer(s.getId(), answers.get(s));
                // if the EV accepts the suggestion of a agents.station, then save this agents.station's communication port
                if (answers.get(s) == IntegerConstants.EV_EVALUATE_ACCEPT) {
                    acceptedStation = s.getCommunicationPort();
                    toBeServiced = true;
                }
            }
        }
    }

    public void sendDeferralMessage () {
        pda.sendRequest(info, IntegerConstants.EV_UPDATE_DELAY, acceptedStation);
    }

    public void sendCancellationMessage () {
        pda.sendRequest(info, IntegerConstants.EV_UPDATE_CANCEL, acceptedStation);
    }

    public int getInformSlot() {
        return informSlot;
    }

    public String toString() {
        return "**************" + "\n" + info.toString() + "\n\t*" + strategy.toString() + "\n\t* Informs: " + "\n\t\tSlot: " + informSlot;
    }

    public void resetRounds() {
        strategy.resetRounds();
    }

    public EVReceiver getCommunicationPort () {
        return pda.getMessenger().getReceiver();
    }

    public EVState getState () {
        return state;
    }

}
