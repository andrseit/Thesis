package agents.evs;

public class EVParameters {

    private final int id;
    private final int x;
    private final int y;
    private final int finalX;
    private final int finalY;
    private final int start;
    private final int end;
    private final int energy;
    private final int bid;
    private final int max_distance;

    public EVParameters(int id, int x, int y, int finalX,
                        int finalY, int start,
                        int end, int energy, int bid,
                        int max_distance) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.finalX = finalX;
        this.finalY = finalY;
        this.start = start;
        this.end = end;
        this.energy = energy;
        this.bid = bid;
        this.max_distance = max_distance;
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


    public int getBid() {
        return bid;
    }


    public int getMax_distance() {
        return max_distance;
    }

}
