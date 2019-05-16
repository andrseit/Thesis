package agents.station.statistics;

public class StationSystemValues {

    private int stationID;
    private int chargersNumber;
    private int slotsNumber;

    public StationSystemValues(int stationID, int chargersNumber, int slotsNumber) {
        this.stationID = stationID;
        this.chargersNumber = chargersNumber;
        this.slotsNumber = slotsNumber;
    }

    public int getStationID() {
        return stationID;
    }

    public int getChargersNumber() {
        return chargersNumber;
    }

    public int getSlotsNumber() {
        return slotsNumber;
    }
}
