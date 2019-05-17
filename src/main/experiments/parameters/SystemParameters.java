package main.experiments.parameters;

public class SystemParameters {

    private int slotsNumber;
    private int gridSize;


    public SystemParameters(int slotsNumber, int gridSize) {
        this.slotsNumber = slotsNumber;
        this.gridSize = gridSize;
    }

    public int getSlotsNumber() {
        return slotsNumber;
    }

    public int getGridSize() {
        return gridSize;
    }
}
