package agents.evs;

public class EVParameters {

    private final int slotsNumber;
    private final int id;
    private final int x;
    private final int y;
    private final int finalX;
    private final int finalY;
    private final int start;
    private final int end;
    private final int energy;

    public EVParameters(int id, int x, int y, int finalX,
                        int finalY, int start,
                        int end, int energy,
                        int slotsNumber) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.finalX = finalX;
        this.finalY = finalY;
        this.start = start;
        this.end = end;
        this.energy = energy;
        this.slotsNumber = slotsNumber;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    public int getFinalX() {
        return finalX;
    }


    public int getFinalY() {
        return finalY;
    }


    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getEnergy() {
        return energy;
    }

    public int getSlotsNumber() { return slotsNumber; }
}
