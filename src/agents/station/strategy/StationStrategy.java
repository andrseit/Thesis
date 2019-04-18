package agents.station.strategy;

import agents.evs.Preferences;
import agents.station.*;
import agents.station.communication.StationMessenger;
import agents.station.optimize.Optimizer;
import user_interface.StationState;
import various.ArrayTransformations;
import various.IntegerConstants;

import java.util.ArrayList;

public class StationStrategy {

    private StationStatistics statistics;
    private StationState state;

    int slotsNumber;
    int currentSlot;
    private int[] price;

    private Schedule schedule;

    // EVs
    // this may be redundant - this list is connected to PDA, when a request is received,
    // it is added in this list, then the agents.station reads the messages from the list
    // and adds them to the proper list e.g. evBidders
    private ArrayList<EVObject> incomingRequests;
    private ArrayList<EVObject> acceptedEVs; // the agents.evs that accepted a agents.station's offer - maybe this could go into schedule
    private ArrayList<EVObject> notChargedEVs; // the EVs that could not charge - to computeSuggestions alternatives - needed only for the strategy

    private Optimizer optimizer; // it computes the optimal schedule, i guess it will be better if you create it out of the class
    // and then add it, so that each agents.station will have a unique optimizer
    private Optimizer alternativesOptimizer;

    public StationStrategy (Optimizer optimizer, Optimizer alternativesOptimizer,
                            int[] price, int slotsNumber, int chargersNumber,
                            StationStatistics statistics, StationState state) {
        this.slotsNumber = slotsNumber;
        this.price = price;
        currentSlot = 0;

        schedule = new Schedule(slotsNumber, chargersNumber);

        incomingRequests = new ArrayList<>();
        acceptedEVs = new ArrayList<>();

        this.optimizer = optimizer;
        this.alternativesOptimizer = alternativesOptimizer;

        this.statistics = statistics;
        this.state = state;
    }

    public void handleAnswers (StationMessenger messenger) {
        ArrayList<EVObject> toBeRemoved = new ArrayList<>();
        for (EVObject ev: messenger.getIncomingAnswers().keySet()) {
            Integer answer = messenger.getIncomingAnswers().get(ev);
            // if it is a request then add it to the incoming requests
            if (answer == IntegerConstants.EV_MESSAGE_REQUEST) {
                incomingRequests.add(ev);

                statistics.updateRequests(1);
                state.addStateEV(currentSlot, ev.getId(), "request", ev.getPreferences().toString());

            } else if (answer == IntegerConstants.EV_UPDATE_DELAY || answer == IntegerConstants.EV_UPDATE_CANCEL) {
                ArrayList<EVObject> removeAccepted = new ArrayList<>();
                for (int e = 0; e < acceptedEVs.size(); e++) {
                    EVObject evAnswer = acceptedEVs.get(e);
                    if (evAnswer.getId() == ev.getId()) {

                        // remove the ev's row from schedule and update chargers
                        schedule.setScheduleMap(ArrayTransformations.removeRowFromArray(schedule.getScheduleMap(), e));
                        removeAccepted.add(evAnswer);
                        schedule.increaseRemainingChargers(evAnswer);

                        if (answer == IntegerConstants.EV_UPDATE_DELAY) {
                            // handle deferral
                            Preferences updatedPreferences = ev.getPreferences();
                            evAnswer.getPreferences().setPreferences(updatedPreferences.getStart(), updatedPreferences.getEnd(), updatedPreferences.getEnergy());
                            evAnswer.setDelayed(true);
                            incomingRequests.add(evAnswer);
                            statistics.updateDelays(1);
                            statistics.updateSlotsUsed(-evAnswer.getSuggestion().getSlotsAllocated().size());
                            state.addStateEV(currentSlot, evAnswer.getId(), "delay", evAnswer.getPreferences().toString());
                            System.out.println("We have a deferral here! By EV No " + ev.getId());
                        }
                        else {
                            // handle cancellation - nothing more has to be done
                            statistics.updateCancellations(1);
                            statistics.updateSlotsUsed(-evAnswer.getSuggestion().getSlotsAllocated().size());
                            state.addStateEV(currentSlot, evAnswer.getId(), "cancel", "X");
                            System.out.println("We have a cancellation here! By EV No " + ev.getId());
                        }
                    }
                }
                for (EVObject remove: removeAccepted) {
                    System.out.println("Removing EV No " + remove.getId());
                    acceptedEVs.remove(remove);
                }
            } else { // else search for the ev to do the according actions
                for (EVObject evRequest : incomingRequests) {
                    if (evRequest.getId() == ev.getId()) {
                        if (answer == IntegerConstants.EV_EVALUATE_ACCEPT) {
                            //System.out.println("\t* " + ev + " says accepted my offer!");
                            ev.setCharged(true);
                            acceptedEVs.add(evRequest);
                            toBeRemoved.add(evRequest);
                            schedule.addEVtoScheduleMap(evRequest);
                            if (!evRequest.isDelayed())
                                statistics.updateAccepted(1);
                            statistics.updateSlotsUsed(evRequest.getSuggestion().getSlotsAllocated().size());
                            state.addStateEV(currentSlot, evRequest.getId(), "accept", evRequest.getSuggestion().getPreferences().toString());
                            break;
                        } else if (answer == IntegerConstants.EV_EVALUATE_WAIT) {
                            //System.out.println("\t* " + ev + " says waits for an offer!");
                            break;
                        } else if (answer == IntegerConstants.EV_EVALUATE_REJECT) {
                            //System.out.println("\t* " + ev + " says rejected my offer!");
                            toBeRemoved.add(evRequest);
                            if (evRequest.isDelayed())
                                statistics.updateDelayRejected(1);
                            else
                                statistics.updateRejected(1);
                            state.addStateEV(currentSlot, evRequest.getId(), "reject", "X");
                            break;
                        }
                    }
                }
            }
        }
        for (EVObject ev: toBeRemoved) {
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
    }

    /**
     *  after the computation of the schedule call this
     *  method to add the suggestion messages to the agents.evs
     */
    private void addSuggestionMessages (StationInfo info, ArrayList<EVObject> receivers) {
        for (EVObject ev: receivers) {
            // computeSuggestions cost and then set the message - deal with the cost later (simply put 0 now)

            //System.out.println("EV ID: " + ev.getStationId());
            Suggestion preferences = schedule.getChargingSlots(ev.getStationId());
            int cost = 0;
            Integer messageType;
            if (preferences.getEnergy() > 0) {
                messageType = IntegerConstants.STATION_HAS_SUGGESTION;
            } else {
                messageType = IntegerConstants.STATION_NEXT_ROUND_SUGGESTION;
            }
            ev.setSuggestion(preferences);
            ev.setSuggestionMessage(new SuggestionMessage(info, preferences.getPreferences(), cost, messageType));
        }
    }

    private void compute (StationInfo info, Optimizer optimizer) {
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
            for (EVObject ev: incomingRequests) {
                // computeSuggestions cost and then set the message - deal with the cost later (simply put 0 now)

                // computes a suggestion
                Suggestion preferences = schedule.getChargingSlots(ev.getStationId());
                if (preferences.getEnergy() <= 0) {
                    notChargedEVs.add(ev);
                }
            }
        }
        else
            System.out.println("Station No " + info.getId() + " has no incoming requests.");

        state.addScheduleState(schedule.getTemporaryScheduleMap(), schedule.getTemporaryChargers(), schedule.getRemainingChargers(), incomingRequests);
    }

    // computes alternative suggestions for agents.evs that did not charge with the initial schedule
    // those agents.evs are stored into a list, and they have a new reference id
    public void computeAlternatives (StationInfo info) {
        System.out.println("\nStation No " + info.getId() + " computes alternatives...");
        if (!notChargedEVs.isEmpty()) {
            //compute(alternativesOptimizer);
            schedule.updateTemporaryScheduleMap(alternativesOptimizer.optimize(slotsNumber, currentSlot, notChargedEVs, schedule.getTemporaryChargers(), price), notChargedEVs);
            addSuggestionMessages(info, incomingRequests);
            schedule.updateTemporaryChargers(notChargedEVs);
        }
        else
            System.out.println("Station No " + info.getId() + " has no incoming requests.");
        notChargedEVs.clear();

        state.addScheduleState(schedule.getTemporaryScheduleMap(), schedule.getTemporaryChargers(), schedule.getRemainingChargers(), incomingRequests);
    }

    public void setCurrentSlot(int currentSlot) {
        this.currentSlot = currentSlot;
    }

    public ArrayList<EVObject> getSuggestionReceivers () { return incomingRequests; }

    /**
     * Show if the agents.station has any more requests in its list
     * or it has serviced them all
     * @return if incomingRequests is empty
     */
    public boolean hasFinished () { return incomingRequests.isEmpty(); }

}
