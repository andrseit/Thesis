package new_classes;

import evs.Preferences;
import new_classes.console.StationState;
import station.EVObject;
import station.StationInfo;
import station.SuggestionMessage;
import station.communication.StationPDA;
import station.communication.StationReceiver;
import station.negotiation.Suggestion;
import various.ArrayTransformations;
import various.IntegerConstants;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Thesis on 21/1/2019.
 */
public class Station {

    private StationStatistics statistics;
    private StationState state;

    // integrate station info with station pda (maybe add info into the pda)
    private StationPDA pda;
    private StationInfo info;
    private int[] price;

    private Schedule schedule;
    // there was an array named "price" and "renewables"
    // but I guess you can take that from "pricing"

    private int slotsNumber; // i am not sure if that is actually needed in this class
    private int currentSlot;

    // EVs
    // this may be redundant - this list is connected to PDA, when a request is received,
    // it is added in this list, then the station reads the messages from the list
    // and adds them to the proper list e.g. evBidders
    private ArrayList<EVObject> incomingRequests;
    private HashMap<EVObject, Integer> incomingAnswers;
    private ArrayList<EVObject> acceptedEVs; // the evs that accepted a station's offer - maybe this could go into schedule
    private ArrayList<EVObject> notChargedEVs; // the EVs that could not charge - to computeSuggestions alternatives - needed only for the strategy

    private Optimizer optimizer; // it computes the optimal schedule, i guess it will be better if you create it out of the class
    // and then add it, so that each station will have a unique optimizer
    private Optimizer alternativesOptimizer;

    public Station (int id, int x, int y, int chargersNumber, Optimizer optimizer, Optimizer alternativesOptimizer, int[] price, int slotsNumber) {
        this.optimizer = optimizer;
        this.alternativesOptimizer = alternativesOptimizer;
        this.slotsNumber = slotsNumber;
        this.price = price;
        schedule = new Schedule(slotsNumber, chargersNumber);
        incomingRequests = new ArrayList<>();
        acceptedEVs = new ArrayList<>();
        incomingAnswers = new HashMap<>();
        pda = new StationPDA(id,0, 0, incomingRequests, incomingAnswers);
        info = new StationInfo(id, x, y, chargersNumber, pda.getMessenger().getAddress());
        currentSlot = 0;

        statistics = new StationStatistics(chargersNumber, slotsNumber);
        state = new StationState(info.getId(), slotsNumber);
    }

    /**
     *  after the computation of the schedule call this
     *  method to add the suggestion messages to the evs
     */
    private void addSuggestionMessages (ArrayList<EVObject> receivers) {
        for (EVObject ev: receivers) {
            // computeSuggestions cost and then set the message - deal with the cost later (simply put 0 now)

            // computes a suggestion
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
        /*
        System.out.println("NOT CHARGED!");
        for (EVObject ev: notChargedEVs) {
            System.out.println(ev.toString());
        }
        */
    }

    public void computeSuggestions() {
        System.out.println("\nStation No " + info.getId() + " computes suggestions...");
        notChargedEVs = new ArrayList<>();
        if (!incomingRequests.isEmpty()) {
            compute(optimizer);

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
    }

    // computes alternative suggestions for evs that did not charge with the initial schedule
    // those evs are stored into a list, and they have a new reference id
    public void computeAlternatives () {
        System.out.println("\nStation No " + info.getId() + " computes alternatives...");
        if (!notChargedEVs.isEmpty()) {
            //compute(alternativesOptimizer);
            schedule.updateTemporaryScheduleMap(alternativesOptimizer.optimize(slotsNumber, currentSlot, notChargedEVs, schedule.getTemporaryChargers(), price), notChargedEVs);
            addSuggestionMessages(incomingRequests);
            schedule.updateTemporaryChargers(notChargedEVs);
        }
        else
            System.out.println("Station No " + info.getId() + " has no incoming requests.");
        notChargedEVs.clear();
    }

    private void compute (Optimizer optimizer) {
        schedule.setTemporaryScheduleMap(optimizer.optimize(slotsNumber, currentSlot, incomingRequests, schedule.getRemainingChargers(), price));

        /*
        schedule.printRemainingChargers();
        printTemporaryScheduleMap();
        */
        addSuggestionMessages(incomingRequests);
        // computeSuggestions temporary chargers before going into the computation of the alternatives
        schedule.updateTemporaryChargers(incomingRequests);
        System.out.println("Station No. " + info.getId() + " successfully computed its schedule!");
    }

    public void sendSuggestions () {
        for (EVObject ev: incomingRequests) {
            //System.out.println("Station No. " + info.getId() + " sends a suggestion to EV No. " + ev.getId() + "(station id: " + ev.getStationId() + ")");
            pda.sendMessage(ev.getSuggestionMessage(), ev.getEvReceiver());
        }
    }

    public void handleAnswers () {
        //System.out.println("\n****** Station's No. " + info.getId() + " answers *******");

        ArrayList<EVObject> toBeRemoved = new ArrayList<>();
        for (EVObject ev: incomingAnswers.keySet()) {
            Integer answer = incomingAnswers.get(ev);
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
                        ArrayTransformations t = new ArrayTransformations();
                        schedule.setScheduleMap(t.removeRowFromArray(schedule.getScheduleMap(), e));
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

                /*
                System.out.println("+++++++++++++ Schedule after removal of the cancelled EVs +++++++++++++++++++");
                printScheduleMap();
                schedule.printRemainingChargers();
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
                */

            } else { // else search for the ev to do the according actions
                for (int e = 0; e < incomingRequests.size(); e++) {
                    EVObject evRequest = incomingRequests.get(e);
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
        incomingAnswers.clear();

        // update chargers and ids
        schedule.updateStationIDs();
        for (int ev = 0; ev < incomingRequests.size(); ev++) {
            incomingRequests.get(ev).setStationId(ev);
        }
    }

    /**
     * Returns the receiver's reference value
     * @return
     */
    public StationReceiver getCommunicationPort () {
        return pda.getMessenger().getAddress();
    }


    /**
     * Show if the station has any more requests in its list
     * or it has serviced them all
     * @return if incomingRequests is empty
     */
    public boolean hasFinished () {
        return incomingRequests.isEmpty();
    }

    public void setCurrentSlot(int currentSlot) {
        this.currentSlot = currentSlot;
    }

    // **** Console Logs **** //
    public void printList () {
        if (incomingRequests.isEmpty()) {
            System.out.println("No EVs are waiting!");
        }
        for (EVObject ev: incomingRequests) {
            System.out.println("\t>" + ev.toString());
        }
    }

    public void printTemporaryScheduleMap() {
        /*
        int[][] map = schedule.getTemporaryScheduleMap();
        for (int s = 0; s < slotsNumber; s++)
            System.out.print(s + "  ");
        System.out.println(" : Slots number\n-------------------------------------");
        for (int ev = 0; ev < map.length; ev++) {
            for (int slot = 0; slot < map[ev].length; slot++) {
                System.out.print(map[ev][slot] + "  ");
            }
            System.out.print(" : EV No " + incomingRequests.get(ev).getId() + " (" + incomingRequests.get(ev).getStationId() + ")");
            System.out.println();
        }
        */
        System.out.println(printAnyMap("Temporary", schedule.getTemporaryScheduleMap(), schedule.getTemporaryChargers(), incomingRequests));
    }

    public void printTemporarySchedule () {
        for (EVObject ev: incomingRequests) {
            for (int s = 0; s < slotsNumber; s++) {
                if (ev.getSuggestion().getSlotsAllocated().contains(s)) {
                    System.out.print(1 + "  ");
                } else
                    System.out.print(0 + "  ");

            }
            System.out.println();
        }
    }

    public void printScheduleMap () {
        /*
        for (int s = 0; s < slotsNumber; s++)
            System.out.print(s + "  ");
        System.out.println(" : Slots number\n-------------------------------------");
        schedule.printRemainingChargers();
        System.out.println("-------------------------------------");
        int[][] scheduleMap = schedule.getScheduleMap();
        if (scheduleMap != null) {
            for (int ev = 0; ev < scheduleMap.length; ev++) {
                for (int slot = 0; slot < scheduleMap[ev].length; slot++) {
                    System.out.print(scheduleMap[ev][slot] + "  ");
                }
                System.out.print(" : EV No " + acceptedEVs.get(ev).getId() + " (" + acceptedEVs.get(ev).getStationId() + ")");
                System.out.println();
            }
        } else {
            System.out.println("No entries in map!");
        }
        */
        System.out.println(printAnyMap("Main", schedule.getScheduleMap(), schedule.getRemainingChargers(), acceptedEVs));
    }

    public void printAcceptedEVs () {
        if (acceptedEVs.isEmpty()) {
            System.out.println("There are not EVs in accepted!");
        }
        for (EVObject ev: acceptedEVs) {
            System.out.println(ev);
        }
    }

    public void printTemporaryChargers () {
        int[] chargers = schedule.getTemporaryChargers();
        for (int s = 0; s < chargers.length; s++) {
            System.out.print(chargers[s] + "  ");
        }
        System.out.println(" : Temporary Remaining Chargers\n--------------------------------");
    }

    public void printWhoCharges () {
        int[] whoCharges = schedule.getWhoCharges();
        for (int ev = 0; ev < whoCharges.length; ev++) {
            System.out.println(whoCharges[ev]);
        }
    }

    /**
     * A station can receive requests from EVs and save them into a list
     * An EV's request must be accompanied by: arrival-departure time and demanded energy
     */

    private String printAnyMap (String name, int[][] scheduleMap, int[] chargers, ArrayList<EVObject> evs) {
        //if (evs.isEmpty())
           // return "----------------\nCannot generate " + name + " map!\n-------------------";
        StringBuilder str = new StringBuilder();
        str.append("--------------- " + name + " Schedule Map -------------------\n");
        for (int s = 0; s < slotsNumber; s++)
            str.append(s + "  ");
        str.append(" : Slots number\n-------------------------------------\n");

        if (name.equals("Temporary")){
            for (int s = 0; s < chargers.length; s++) {
                str.append(schedule.getRemainingChargers()[s] + "  ");
            }
            str.append(" : Main Remaining Chargers\n");
        }

        for (int s = 0; s < chargers.length; s++) {
            str.append(chargers[s] + "  ");
        }
        str.append(" : " + name + " Remaining Chargers\n--------------------------------\n");
        if (!evs.isEmpty()) {
            for (int ev = 0; ev < scheduleMap.length; ev++) {
                for (int slot = 0; slot < scheduleMap[ev].length; slot++) {
                    str.append(scheduleMap[ev][slot] + "  ");
                }
                str.append(" : EV No " + evs.get(ev).getId() + " (" + evs.get(ev).getStationId() + ")\n");
            }
        } else {
            str.append("No entries in map!\n");
        }
        str.append("-----------------------------------\n");
        return str.toString();
    }


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