package station;

import various.ArrayTransformations;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Thesis on 22/1/2018.
 */
public abstract class AbstractOnlineStation extends AbstractStation {

    protected int minSlot; // the earlier slot that the station needs to make an offer to an ev
    protected ArrayList<EVObject> lockedBidders;

    protected int[][] fullScheduleMap;
    protected int[] chargersState; // saves the chargers as they were after the last computation of the schedule
    protected int rounds;

    /**
     * Constructor
     *
     * @param info
     * @param slotsNumber
     */
    public AbstractOnlineStation(StationInfo info, int slotsNumber) {
        super(info, slotsNumber);
        minSlot = Integer.MAX_VALUE;
        lockedBidders = new ArrayList<>();

        fullScheduleMap = new int[0][slotsNumber];
        chargersState = Arrays.copyOf(schedule.getRemainingChargers(), schedule.getRemainingChargers().length);
    }

    /**
     * Checks if the stations is ready to make offers
     * Some stations may wait till the last available slot,
     * some other make offers immediately...
     * @param slot
     * @return
     */
    public abstract boolean hasOffers (int slot);

    /**
     * Checks when is the last slot, the one that the station will
     * make its offers
     * @param ev
     * @return
     */
    public abstract int setLastSlot(EVObject ev);

    public void addEVBidder (EVObject ev) {
        System.out.println("Online Station's Add");
        ev.setStationId(id_counter);
        this.setSlotsNeeded(ev);
        int lastSlot = this.setLastSlot(ev);
        if (lastSlot < minSlot)
            minSlot = lastSlot;
        id_counter++;
        evBidders.add(ev);
        waiting.add(ev);
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

    /**
     * When the procedure of the scheduling and negotiating in one slot is over
     * move bidders that charged to a list and remove them from the bidders list
     */
    private void lockEVBidders() {
        int[] whoCharged = cp.getWhoCharges();
        ArrayList<EVObject> removed = new ArrayList<>();
        for (int s = 0; s < whoCharged.length; s++) {
            if (whoCharged[s] == 1) {
                lockedBidders.add(evBidders.get(s));
                removed.add(evBidders.get(s));
            }
        }
        for (EVObject ev: removed) {
            evBidders.remove(ev);
            System.out.println("Removing ev_" + ev.getId() + ", " + ev.getStationId());
        }
        id_counter = 0;
    }

    public void updateStationData () {
        chargersState = Arrays.copyOf(schedule.getRemainingChargers(), schedule.getRemainingChargers().length);
        this.updateBiddersLists();
        this.updateEVBiddersIDs();
        this.lockEVBidders();
        this.updateFullScheduleMap();
        finished = false;
        minSlot = Integer.MAX_VALUE;
        rounds = 0;
        System.out.println(schedule.printScheduleMap(fullScheduleMap, price));
    }

    private void updateFullScheduleMap() {
        ArrayTransformations t = new ArrayTransformations();
        fullScheduleMap = t.concatMaps(fullScheduleMap, schedule.getScheduleMap(), slotsNumber);
        t.printIntArray(fullScheduleMap);
    }

    private void updateEVBiddersIDs() {
        for (EVObject ev: evBidders) {
            int oldID = ev.getStationId();
            ev.setStationId(oldID + fullScheduleMap.length);
        }
    }

    public void resetChargers () {
        schedule.resetChargers(chargersState);
    }


    public boolean isUpdate () {
        return update;
    }
}
