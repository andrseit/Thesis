package evs;

import java.util.Random;

/**
 * Created by Thesis on 8/12/2017.
 */
public class EV {

    private Preferences preferences;
    private int bid;

    public EV (int start, int end, int energy, int bid) {
        preferences = new Preferences();
        preferences.setStart(start);
        preferences.setEnd(end);
        preferences.setEnergy(energy);
        this.bid = bid;
    }


    /**
     * Dexetai mia protasei kai elegxei an tou aresei
     * an nai tote epistrefei 1
     * an oxi kai thelei na lave alli protasi tote epistrefei 2
     * an oxi kai de thelei na lavei alli protasei tote epistrefei 3
     * @param suggestion
     * @return
     */
    public int evaluateSuggestion (Preferences suggestion) {
        int accept;
        Random generator = new Random();
        accept = generator.nextInt(3) + 1;
        return 1;
    }
}
