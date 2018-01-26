package station.pricing;

/**
 * Created by Thesis on 23/1/2018.
 */
public abstract class Pricing {

    protected int[] price;

    public Pricing(int[] price) {
        this.price = price;
    }

    public abstract int computeCost(int[] slotsAffected);

    public int[] getPrice() {
        return price;
    }
}
