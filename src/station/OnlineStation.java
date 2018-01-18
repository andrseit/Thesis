package station;

import various.ArrayTransformations;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Thesis on 17/1/2018.
 */
public class OnlineStation extends NewStation {

    private int minSlot; // the earlier slot that the station needs to make an offer to an ev
    private ArrayList<EVObject> lockedBidders;

    private int[][] fullScheduleMap;
    private int[] chargersState; // saves the chargers as they were after the last computation of the schedule

    public OnlineStation(StationInfo info, int slots_number) {
        super(info, slots_number);
        minSlot = Integer.MAX_VALUE;
        lockedBidders = new ArrayList<>();

        fullScheduleMap = new int[0][slots_number];
        chargersState = Arrays.copyOf(schedule.getRemainingChargers(), schedule.getRemainingChargers().length);
    }

    public boolean hasOffers (int slot) {
        System.out.println("Min slot: " + minSlot);
        return minSlot == slot;
    }

    public void addEVBidder (EVObject ev) {
        System.out.println("Online Station's Add");
        ev.setStationId(id_counter);
        this.setSlotsNeeded(ev);
        int lastSlot = this.setLastSlot(ev);
        if (lastSlot < minSlot)
            minSlot = lastSlot;
        id_counter++;
        ev_bidders.add(ev);
    }

    /**
     * When the procedure of the scheduling and negotiating in one slot is over
     * move bidders that charged to a list and remove them from the bidders list
     */
    private void lockEVBidders() {
        int[] whoCharged = cp.getWhoCharges();
        ArrayList<EVObject> removed = new ArrayList<>();
        for (int s = 0; s < whoCharged.length; s++) {
            if (whoCharged[s] == 1) {
                lockedBidders.add(ev_bidders.get(s));
                removed.add(ev_bidders.get(s));
            }
        }
        for (EVObject ev: removed) {
            ev_bidders.remove(ev);
            System.out.println("Removing ev_" + ev.getId() + ", " + ev.getStationId());
        }
        id_counter = 0;
    }

    /**
     * Set the number of slots that the ev needs to reach the station
     * We assume that an ev needs a slot to decrease by 1 the distance to the station
     * @param ev
     * @return
     */
    private void setSlotsNeeded (EVObject ev) {
        int distance = Math.abs(ev.getX() - info.getLocationX()) + Math.abs(ev.getY() - info.getLocationY());
        System.out.println("<" + ev.getX() + ", " + ev.getY() + ">");
        System.out.println("ev_" + ev.getId() + ": " + distance);
        ev.setSlotsNeeded(distance);
    }

    private int setLastSlot(EVObject ev) {
        int distance = ev.getSlotsNeeded();
        int start = ev.getStartSlot();
        int lastSlot = start - distance;
        ev.setLastSlot(lastSlot);
        System.out.println("Last slot: " + lastSlot);
        return lastSlot;
    }

    private void updateFullScheduleMap() {
        ArrayTransformations t = new ArrayTransformations();
        fullScheduleMap = t.concatMaps(fullScheduleMap, schedule.getScheduleMap(), slots_number);
        t.printIntArray(fullScheduleMap);
    }

    private void updateEVBiddersIDs() {
        for (EVObject ev: ev_bidders) {
            int oldID = ev.getStationId();
            ev.setStationId(oldID + fullScheduleMap.length);
        }
    }

    public void resetChargers () {
        System.out.println("Tha treksw tou paidiou!");
        schedule.resetChargers(chargersState);
    }

    public void updateStationData () {
        this.updateBiddersLists();
        this.updateEVBiddersIDs();
        this.lockEVBidders();
        this.updateFullScheduleMap();
        finished = false;
        minSlot = Integer.MAX_VALUE;
        chargersState = Arrays.copyOf(schedule.getRemainingChargers(), schedule.getRemainingChargers().length);
        System.out.println(schedule.printScheduleMap(fullScheduleMap, price));
    }

    public int getMinSlot() {
        return minSlot;
    }

    public void resetMinSlot () {
        minSlot = Integer.MAX_VALUE;
    }
}
