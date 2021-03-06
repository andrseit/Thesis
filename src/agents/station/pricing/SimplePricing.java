package agents.station.pricing;


import various.ArrayTransformations;

import java.util.Arrays;

public class SimplePricing extends Pricing {

    public SimplePricing(int[] price, int[] demand, int[] renewables) {
        super(price, demand, renewables);
    }

    @Override
    public int computeCost(int[] slotsAffected, boolean isInitial) {
        int cost = 0;
        int slots = 0;
        double[] normalizedDemand = ArrayTransformations.normalizeArrayValues(demand);
        int[] renewablesCopy = Arrays.copyOf(renewables, renewables.length);
        for (int s = 0; s < price.length; s++) {
            if (slotsAffected[s] == 1) {
                slots++;
                if (normalizedDemand[s] >= 0.5 && renewables[s] > 0) {
                    if (isInitial) {
                        cost += (price[s] + 1);
                        renewablesCopy[s]--;
                    } else {
                        cost += (price[s] - 1);
                    }
                }
                else if (normalizedDemand[s] >= 0.5) {
                    if (isInitial) {
                        cost += (price[s] + 2);
                    } else {
                        cost += price[s];
                    }
                }
                else if (renewables[s] > 0) {
                    cost += (price[s] - 1);
                    renewablesCopy[s]--;
                }
                else {
                    cost += price[s];
                }
            }
        }
        return cost;
        //return 2*slots;
    }

}
