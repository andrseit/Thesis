package station.negotiation;

import evs.Preferences;
import statistics.TimeStats;
import various.IntegerConstants;

/**
 * Created by Thesis on 6/12/2017.
 */
public class Suggestion extends Preferences {

    private int rating;
    private int type;
    private int profit;
    private int cost;

    // epeidi otan thes na epanafereis tous fortistes prepei na ksereis poia eixan allaksei
    // me to suggestion opote na pas kai na ta kaneis +1
    private int[] slots_afected;

    private double time;

    public Suggestion() {
        start = -1;
        end = -1;
        energy = -1;
    }

    public int getRating() {
        if (rating < 0 || energy <= 0)
            return Integer.MAX_VALUE;
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        if (type == IntegerConstants.LESS_ENERGY_TYPE)
            str.append("Less Energy -> ");
        else
            str.append("Alt Window  -> ");

        str.append("Start: ").append(start);
        str.append(", End: ").append(end);
        str.append(", Energy: ").append(energy);
        str.append(", Rating: ").append(rating);
        str.append(", Profit: ").append(profit);
        str.append(", Slots affected: ");
        for (int s = 0; s < slots_afected.length; s++) {
            if (slots_afected[s] == 1)
                str.append(s).append(",");
        }
        str.append(" time: " + time);
        return str.toString();
    }

    public void setSlotsAffected(int[] slots_affected) {
        this.slots_afected = slots_affected;
    }

    public void findSlotsAffected(int[] chargers) {

        if ((start == Integer.MAX_VALUE && end == Integer.MAX_VALUE) || (start == -1 && end == -1)) {
            slots_afected = new int[0];
        } else {
            slots_afected = new int[chargers.length];
            for (int s = start; s <= end; s++) {
                if (chargers[s] > 0) {
                    slots_afected[s] = 1;
                }
            }
        }
    }

    public void findSlotsAffected(int[][] schedule, int row) {
        slots_afected = new int[schedule[row].length];
        for (int s = start; s <= end; s++) {
            if (schedule[row][s] == 1) {
                slots_afected[s] = 1;
            }
        }
    }


    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int[] getSlotsAfected() {
        return slots_afected;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getProfit() {
        return profit;
    }

    public void setProfit(int profit) {
        this.profit = profit;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
