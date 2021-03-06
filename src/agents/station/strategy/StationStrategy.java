package agents.station.strategy;

import agents.evs.Preferences;
import agents.station.*;
import agents.station.communication.StationMessenger;
import agents.station.optimize.Optimizer;
import agents.station.statistics.StationStatistics;
import user_interface.StationState;
import various.ArrayTransformations;
import agents.evs.communication.EVMessage;
import agents.station.communication.StationMessage;
import user_interface.EVStateEnum;

import java.util.ArrayList;

public class StationStrategy {

    private StationStatistics statistics;
    protected StationState state;

    int slotsNumber;
    int currentSlot;
    protected int[] price;

    protected Schedule schedule;

    // EVs
    // this may be redundant - this list is connected to PDA, when a request is received,
    // it is added in this list, then the agents.station reads the messages from the list
    // and adds them to the proper list e.g. evBidders
    protected ArrayList<EVObject> incomingRequests;
    protected ArrayList<EVObject> acceptedEVs; // the agents.evs that accepted a agents.station's offer - maybe this could go into schedule
    protected ArrayList<EVObject> notChargedEVs; // the EVs that could not charge - to computeSuggestions alternatives - needed only for the strategy

    protected Optimizer optimizer; // it computes the optimal schedule
    protected Optimizer alternativesOptimizer;
    private boolean useAlternatives;

    public StationStrategy(Optimizer optimizer, Optimizer alternativesOptimizer,
                           int[] price, int slotsNumber, int chargersNumber, boolean useAlternatives,
                           StationStatistics statistics, StationState state) {
        this.slotsNumber = slotsNumber;
        this.price = price;
        currentSlot = 0;

        this.useAlternatives = useAlternatives;
        schedule = new Schedule(slotsNumber, chargersNumber);

        incomingRequests = new ArrayList<>();
        acceptedEVs = new ArrayList<>();

        this.optimizer = optimizer;
        this.alternativesOptimizer = alternativesOptimizer;

        this.statistics = statistics;
        this.state = state;
    }

    public void handleAnswers(StationMessenger messenger) {
        ArrayList<EVObject> toBeRemoved = new ArrayList<>();
        for (EVObject ev : messenger.getIncomingAnswers().keySet()) {
            EVMessage answer = messenger.getIncomingAnswers().get(ev);
            // if it is a request then add it to the incoming requests
            if (answer == EVMessage.EV_MESSAGE_REQUEST) {
                incomingRequests.add(ev);

                Preferences p = ev.getPreferences();
                statistics.addEV(ev.getId(), EVStateEnum.EV_STATE_REQUESTED, new Preferences(p.getStart(), p.getEnd(), p.getEnergy()), currentSlot, 0);
                state.addStateEV(currentSlot, ev.getId(), EVStateEnum.EV_STATE_REQUESTED, ev.getPreferences());

            } else if (answer == EVMessage.EV_UPDATE_DELAY || answer == EVMessage.EV_UPDATE_CANCEL) {
                ArrayList<EVObject> removeAccepted = new ArrayList<>();
                for (int e = 0; e < acceptedEVs.size(); e++) {
                    EVObject evAnswer = acceptedEVs.get(e);
                    if (evAnswer.getId() == ev.getId()) {

                        // remove the ev's row from schedule and update chargers
                        schedule.setScheduleMap(ArrayTransformations.removeRowFromArray(schedule.getScheduleMap(), e));
                        removeAccepted.add(evAnswer);
                        schedule.increaseRemainingChargers(evAnswer);

                        if (answer == EVMessage.EV_UPDATE_DELAY) {
                            // handle deferral
                            Preferences updatedPreferences = ev.getPreferences();
                            evAnswer.getPreferences().setPreferences(updatedPreferences.getStart(), updatedPreferences.getEnd(), updatedPreferences.getEnergy());
                            evAnswer.setDelayed(true);
                            incomingRequests.add(evAnswer);

                            Preferences p = evAnswer.getPreferences();
                            statistics.addEV(ev.getId(), EVStateEnum.EV_STATE_DELAYED, new Preferences(p.getStart(), p.getEnd(), p.getEnergy()), currentSlot, 0);
                            state.addStateEV(currentSlot, evAnswer.getId(), EVStateEnum.EV_STATE_DELAYED, new Preferences(p.getStart(), p.getEnd(), p.getEnergy()));
                            System.out.println("We have a deferral here! By EV No " + ev.getId());
                        } else {
                            // handle cancellation - nothing more has to be done
                            statistics.addEV(ev.getId(), EVStateEnum.EV_STATE_CANCELLED, null, currentSlot, 0);
                            state.addStateEV(currentSlot, evAnswer.getId(), EVStateEnum.EV_STATE_CANCELLED, null);
                            System.out.println("We have a cancellation here! By EV No " + ev.getId());
                        }
                    }
                }
                for (EVObject remove : removeAccepted) {
                    System.out.println("Removing EV No " + remove.getId());
                    acceptedEVs.remove(remove);
                }
            } else { // else search for the ev to do the according actions
                for (EVObject evRequest : incomingRequests) {
                    if (evRequest.getId() == ev.getId()) {
                        if (answer == EVMessage.EV_EVALUATE_ACCEPT) {
                            //System.out.println("\t* " + ev + " says accepted my offer!");
                            ev.setCharged(true);
                            acceptedEVs.add(evRequest);
                            toBeRemoved.add(evRequest);
                            schedule.addEVtoScheduleMap(evRequest);

                            Preferences s = evRequest.getSuggestion().getPreferences();
                            if (evRequest.acceptedAlternative())
                                statistics.addEV(ev.getId(), EVStateEnum.EV_STATE_ACCEPTED_ALTERNATIVE,
                                        new Preferences(s.getStart(), s.getEnd(), s.getEnergy()), currentSlot, evRequest.getSuggestion().getSlotsAllocated().size());
                            else
                                statistics.addEV(ev.getId(), EVStateEnum.EV_STATE_ACCEPTED_INITIAL,
                                        new Preferences(s.getStart(), s.getEnd(), s.getEnergy()), currentSlot, evRequest.getSuggestion().getSlotsAllocated().size());
                            state.addStateEV(currentSlot, evRequest.getId(), EVStateEnum.EV_STATE_ACCEPTED, evRequest.getSuggestion().getPreferences());
                            break;
                        } else if (answer == EVMessage.EV_EVALUATE_WAIT) {
                            //System.out.println("\t* " + ev + " says waits for an offer!");
                            statistics.addEV(ev.getId(), EVStateEnum.EV_STATE_WAIT, null, currentSlot, 0);
                            break;
                        } else if (answer == EVMessage.EV_EVALUATE_REJECT) {
                            //System.out.println("\t* " + ev + " says rejected my offer!");
                            toBeRemoved.add(evRequest);

                            if (evRequest.acceptedAlternative())
                                statistics.addEV(ev.getId(), EVStateEnum.EV_STATE_REJECTED_ALTERNATIVE, null, currentSlot, 0);
                            else
                                statistics.addEV(ev.getId(), EVStateEnum.EV_STATE_REJECTED, null, currentSlot, 0);
                            state.addStateEV(currentSlot, evRequest.getId(), EVStateEnum.EV_STATE_REJECTED, null);
                            break;
                        }
                    }
                }
            }
        }
        for (EVObject ev : toBeRemoved) {
            incomingRequests.remove(ev);
        }
        toBeRemoved.clear();
        messenger.getIncomingAnswers().clear();

        // update chargers and ids
        schedule.updateStationIDs();
        for (int ev = 0; ev < incomingRequests.size(); ev++) {
            incomingRequests.get(ev).setStationId(ev);
        }

        state.addScheduleState(schedule.getScheduleMap(), schedule.getRemainingChargers(), schedule.getRemainingChargers(), acceptedEVs);
        statistics.getSlotStatistics(currentSlot).addSlots(schedule.getScheduleMap());
    }

    /**
     * after the computation of the schedule call this
     * method to add the suggestion messages to the agents.evs
     */
    protected void addSuggestionMessages(StationInfo info, ArrayList<EVObject> receivers) {
        for (EVObject ev : receivers) {
            // computeSuggestions cost and then set the message - deal with the cost later (simply put 0 now)

            //System.out.println("EV ID: " + ev.getStationId());
            Suggestion preferences = schedule.getChargingSlots(ev.getStationId());
            int cost = 0;
            StationMessage messageType;
            if (preferences.getEnergy() > 0) {
                messageType = StationMessage.STATION_HAS_SUGGESTION;
            } else {
                messageType = StationMessage.STATION_NEXT_ROUND_SUGGESTION;
            }
            ev.setSuggestion(preferences);
            ev.setSuggestionMessage(new SuggestionMessage(info, preferences.getPreferences(), cost, messageType));
        }
    }

    protected void compute(StationInfo info, Optimizer optimizer) {
        schedule.setTemporaryScheduleMap(optimizer.optimize(slotsNumber, currentSlot, incomingRequests, schedule.getRemainingChargers(), price));

        /*
        schedule.printRemainingChargers();
        printTemporaryScheduleMap();
        */
        addSuggestionMessages(info, incomingRequests);
        // computeSuggestions temporary chargers before going into the computation of the alternatives
        schedule.updateTemporaryChargers(incomingRequests);
        System.out.println("Station No. " + info.getId() + " successfully computed its schedule!");
    }

    public void computeSuggestions(StationInfo info) {
        System.out.println("\nStation No " + info.getId() + " computes suggestions...");
        notChargedEVs = new ArrayList<>();
        if (!incomingRequests.isEmpty()) {
            compute(info, optimizer);

            // find not charged
            for (EVObject ev : incomingRequests) {
                // computeSuggestions cost and then set the message - deal with the cost later (simply put 0 now)

                // computes a suggestion
                Suggestion preferences = schedule.getChargingSlots(ev.getStationId());
                if (preferences.getEnergy() <= 0) {
                    notChargedEVs.add(ev);
                }
            }
        } else
            System.out.println("Station No " + info.getId() + " has no incoming requests.");

        state.addScheduleState(schedule.getTemporaryScheduleMap(), schedule.getTemporaryChargers(), schedule.getRemainingChargers(), incomingRequests);
    }

    // computes alternative suggestions for agents.evs that did not charge with the initial schedule
    // those agents.evs are stored into a list, and they have a new reference id
    public void computeAlternatives(StationInfo info) {
        System.out.println("\nStation No " + info.getId() + " computes alternatives...");
        if (!notChargedEVs.isEmpty()) {
            //compute(alternativesOptimizer);
            schedule.updateTemporaryScheduleMap(alternativesOptimizer.optimize(slotsNumber, currentSlot + 1, notChargedEVs, schedule.getTemporaryChargers(), price), notChargedEVs);
            addSuggestionMessages(info, incomingRequests);
            schedule.updateTemporaryChargers(notChargedEVs);
        } else
            System.out.println("Station No " + info.getId() + " has no incoming requests.");
        notChargedEVs.clear();

        state.addScheduleState(schedule.getTemporaryScheduleMap(), schedule.getTemporaryChargers(), schedule.getRemainingChargers(), incomingRequests);
    }

    public void setCurrentSlot(int currentSlot) {
        this.currentSlot = currentSlot;
    }

    public boolean isUseAlternatives() {
        return useAlternatives;
    }

    public ArrayList<EVObject> getSuggestionReceivers() {
        return incomingRequests;
    }

    /**
     * Show if the agents.station has any more requests in its list
     * or it has serviced them all
     *
     * @return if incomingRequests is empty
     */
    public boolean hasFinished() {
        return incomingRequests.isEmpty();
    }

}
