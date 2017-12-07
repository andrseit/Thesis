package station.negotiation;

import evs.Preferences;

/**
 * Created by Thesis on 6/12/2017.
 */
public class Suggestion extends Preferences {

    private int rating;

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
