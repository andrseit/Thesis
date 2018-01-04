package station.negotiation;

import evs.Preferences;
import various.IntegerConstants;

/**
 * Created by Thesis on 6/12/2017.
 */
public class Suggestion extends Preferences {

    private int rating;
    private int type;
    private int profit;

    // epeidi otan thes na epanafereis tous fortistes prepei na ksereis poia eixan allaksei
    // me to suggestion opote na pas kai na ta kaneis +1
    private int[] slots_afected;

    public Suggestion () {
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

    public String toString () {
        StringBuilder str = new StringBuilder();
        if (type == IntegerConstants.LESS_ENERGY_TYPE)
            str.append("Less Energy -> ");
        else
            str.append("Alt Window  -> ");

        str.append("Start: " + start);
        str.append(", End: " + end);
        str.append(", Energy: " + energy);
        str.append(", Rating: " + rating);
        str.append(", Profit: " + profit);
        str.append(", Slots affected: ");
        for (int s = 0; s < slots_afected.length; s++) {
            if (slots_afected[s] == 1)
                str.append(s + ",");
        }
        return str.toString();
    }

    public void setSlotsAffected(int[] slots_affected) {
        this.slots_afected = slots_affected;
    }

    public void findSlotsAffected (int[] chargers) {
        slots_afected = new int[chargers.length];
        for (int s = start; s <= end; s++) {
            if (chargers[s] > 0) {
                slots_afected[s] = 1;
            }
        }
    }

    public int[] getSlotsAfected() {
        return slots_afected;
    }

    public void setType (int type) {
        this.type = type;
    }

    public int getType () {
        return type;
    }

    public int getProfit() {
        return profit;
    }

    public void setProfit(int profit) {
        this.profit = profit;
    }
}
