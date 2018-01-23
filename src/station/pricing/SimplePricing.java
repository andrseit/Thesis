package station.pricing;

import java.util.PriorityQueue;

/**
 * Created by Thesis on 23/1/2018.
 */
public class SimplePricing extends Pricing {

    public SimplePricing(int[] price) {
        super(price);
    }

    @Override
    public int computeCost(int[] slotsAffected) {
        int cost = 0;
        for (int s = 0; s < price.length; s++) {
            if (slotsAffected[s] == 1) {
                cost += price[s];
            }
        }
        return cost;
    }
}
