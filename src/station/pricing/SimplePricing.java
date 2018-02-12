package station.pricing;


import various.ArrayTransformations;

import java.util.Arrays;

/**
 * Created by Thesis on 23/1/2018.
 */
public class SimplePricing extends Pricing {

    public SimplePricing(int[] price, int[] demand, int[] renewables) {
        super(price, demand, renewables);
    }

    @Override
    public int computeCost(int[] slotsAffected, boolean isSuggestion) {
        int cost = 0;
        ArrayTransformations t = new ArrayTransformations();
        double[] normalizedDemand = t.normalizeArrayValues(demand);
        int[] renewablesCopy = Arrays.copyOf(renewables, renewables.length);
        for (int s = 0; s < price.length; s++) {
            if (slotsAffected[s] == 1) {
                if (normalizedDemand[s] >= 0.5 && renewables[s] > 0) {
                    if (!isSuggestion) {
                        cost += (price[s] + 1);
                        renewablesCopy[s]--;
                    }
                }
                else if (normalizedDemand[s] >= 0.5) {
                    if (!isSuggestion)
                        cost += (price[s] + 2);
                }
                else if (renewables[s] > 0) {
                    cost += (price[s] - 1);
                    renewablesCopy[s]--;
                }
                else
                    cost += price[s];
            }
        }
        return cost;
    }

}
