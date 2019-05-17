package main.experiments.parameters;

public class EVsParameters {

    private int evsNumber;
    private int minEnergy;
    private int maxEnergy;
    private double sEnergy;
    private double windowLength;

    public EVsParameters(int evsNumber, int minEnergy, int maxEnergy, double sEnergy, double windowLength) {
        this.evsNumber = evsNumber;
        this.minEnergy = minEnergy;
        this.maxEnergy = maxEnergy;
        this.sEnergy = sEnergy;
        this.windowLength = windowLength;
    }

    public int getEvsNumber() {
        return evsNumber;
    }

    public int getMinEnergy() {
        return minEnergy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public double getsEnergy() {
        return sEnergy;
    }

    public double getWindowLength() {
        return windowLength;
    }
}
