package agents.station.pricing;

public abstract class Pricing {

    protected int[] price;
    protected int[] demand;
    protected int[] renewables;

    public Pricing(int[] price, int[] demand, int[] renewables) {
        this.price = price;
        this.demand = demand;
        this.renewables = renewables;
    }

    public abstract int computeCost(int[] slotsAffected, boolean isSuggestion);

    public int[] getPrice() {
        return price;
    }
}
