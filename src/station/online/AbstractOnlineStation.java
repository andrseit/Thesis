package station.online;

import station.EVObject;
import station.StationInfo;
import station.negotiation.Suggestion;
import station.offline.AbstractStation;
import various.ArrayTransformations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class AbstractOnlineStation extends AbstractStation {

    protected int minSlot; // the earlier slot that the station needs to make an offer to an ev
    protected ArrayList<EVObject> lockedBidders;

    protected int[][] fullScheduleMap;
    protected int[] chargersState; // saves the chargers as they were after the last computation of the schedule
    protected int rounds;
    protected int currentSlot;

    /**
     * Constructor
     *
     * @param info
     * @param slotsNumber
     */
    public AbstractOnlineStation(StationInfo info, int slotsNumber, int[] price, int[] renewables, HashMap<String, Integer> strategyFlags) {
        super(info, slotsNumber, price, renewables, strategyFlags);
        minSlot = Integer.MAX_VALUE;
        lockedBidders = new ArrayList<>();

        fullScheduleMap = new int[0][slotsNumber];
        chargersState = Arrays.copyOf(schedule.getRemainingChargers(), schedule.getRemainingChargers().length);
        finished = true;
    }

    /**
     * Checks when is the last slot, the one that the station will
     * make its offers
     *
     * @param ev
     * @return
     */
    public abstract int setLastSlot(EVObject ev);

    public void addEVBidder(EVObject ev) {
        ev.setStationId(id_counter);
        this.setSlotsNeeded(ev);
        int lastSlot = this.setLastSlot(ev);
        if (lastSlot < minSlot)
            minSlot = lastSlot;
        id_counter++;
        addHasNoOffersMessage(ev);
        evBidders.add(ev);
        messageReceivers.add(ev);
        waiting.add(ev);
    }

    /**
     * Set the number of slots that the ev needs to reach the station
     * We assume that an ev needs a slot to decrease by 1 the distance to the station
     *
     * @param ev
     * @return
     */
    private void setSlotsNeeded(EVObject ev) {
        int distance = Math.abs(ev.getX() - info.getLocationX()) + Math.abs(ev.getY() - info.getLocationY());
        ev.setSlotsNeeded(distance);
    }

    /**
     * When the procedure of the scheduling and negotiating in one slot is over
     * move bidders that charged to a list and remove them from the bidders list
     */
    private void lockEVBidders() {
        int[] whoCharged = cp.getWhoCharges();
        ArrayList<EVObject> removed = new ArrayList<>();
        if (whoCharged.length != evBidders.size())
            System.out.println("evbidders size: " + evBidders.size() + " who charges length: " + whoCharged.length
            + " map length: " + cp.getScheduleMap().length);
        for (int s = 0; s < evBidders.size(); s++) {
            if (whoCharged[s] == 1) {
                lockedBidders.add(evBidders.get(s));
                removed.add(evBidders.get(s));
            }
        }
        for (EVObject ev : removed) {
            evBidders.remove(ev);
        }
        id_counter = 0;
    }

    public void updateStationData() {
        chargersState = Arrays.copyOf(schedule.getRemainingChargers(), schedule.getRemainingChargers().length);
        this.updateBiddersLists();
        this.updateEVBiddersIDs();
        this.lockEVBidders();
        this.updateFullScheduleMap();
        finished = false;
        demandComputed = false;
        minSlot = Integer.MAX_VALUE;
        rounds = 0;
        //System.out.println(schedule.printScheduleMap(fullScheduleMap, price));
    }

    /**
     * Sometimes a station may send a not available slots message,
     * but without computing the schedule, because based on its strategy it
     * may have not computed the offers yet, so you have to update only the
     * rejections.
     */
    public void updateStationDataNoSchedule() {
        this.updateBiddersLists();
        finished = false;
        demandComputed = false;
        if (evBidders.isEmpty())
            minSlot = Integer.MAX_VALUE;
        rounds = 0;
    }

    private void updateFullScheduleMap() {
        ArrayTransformations t = new ArrayTransformations();
        fullScheduleMap = t.concatMaps(fullScheduleMap, schedule.getScheduleMap(), slotsNumber);
        //t.printIntArray(fullScheduleMap);
    }

    private void updateEVBiddersIDs() {
        for (EVObject ev : evBidders) {
            int oldID = ev.getStationId();
            ev.setStationId(oldID + fullScheduleMap.length);
        }
    }

    public void addHasNoOffersMessage(EVObject ev) {
        Suggestion suggestion = new Suggestion();
        suggestion.setStartEndSlots(-3, -3);  // NEO 18/2/2018 - itan -1
        suggestion.setEnergy(0);
        suggestion.setCost(0);
        ev.setSuggestion(suggestion);
        ev.setFinalSuggestion();
    }

    /**
     * Transfers bidders from evBidders to message receivers
     * because previous bidders are gone when the message was sent
     */
    public void transferBidders() {
        for (EVObject ev : evBidders) {
            if (!messageReceivers.contains(ev)) {
                messageReceivers.add(ev);
                waiting.add(ev);
            }
        }
    }

    /**
     * Checks if the stations is ready to make offers
     * Some stations may wait till the last available slot,
     * some other make offers immediately...
     *
     * @param slot
     * @return
     */
    public boolean hasOffers(int slot) {
        return minSlot == slot;
    }

    public void resetChargers() {
        schedule.resetChargers(chargersState);
    }

    public boolean isUpdate() {
        return update;
    }

    public void printScheduleMap() {
        //schedule.printScheduleMap(fullScheduleMap, price);
    }

    public void setCurrentSlot(int currentSlot) {
        renewablesUpdated = false;
        this.currentSlot = currentSlot;
    }

    @Override
    public ArrayList<EVObject> getChargedEVs() {
        return lockedBidders;
    }

    @Override
    public int[][] getScheduleMap() {
        return fullScheduleMap;
    }
}
