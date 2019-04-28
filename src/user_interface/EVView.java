package user_interface;

import java.util.ArrayList;

/**
 * Created by Thesis on 2/4/2019.
 */
public class EVView {

    private String evID;
    private ArrayList<EVStateEnum> states; // to be combined with String state (remove state, keep states)
    private ArrayList<String> preferencesStates; // parallel with states
    private int slotsUsed;

    public EVView(String evID, EVStateEnum state, String preferences) {
        states = new ArrayList<>();
        states.add(state);
        preferencesStates = new ArrayList<>();
        preferencesStates.add(preferences);
        this.evID = evID;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(evID).append(": ");
        if (states.size() != preferencesStates.size()) {
            System.err.println("Something is wrong with the states");
            System.exit(0);
        }
        for (int i = 0; i < states.size(); i++)
            str.append(states.get(i).getDescription() + " [" + preferencesStates.get(i) + "] -> ");
        if (!str.toString().contains(" -> "))
            return str.toString();
        return str.substring(0, str.length() - 3) + ", slots: " + slotsUsed;
    }

    public String getEvID() { return evID; }

    public int getSlotsUsed() { return slotsUsed; }

    public void setSlotsUsed(int slotsUsed) { this.slotsUsed = slotsUsed; }

    public ArrayList<EVStateEnum> getStates() { return states; }

    public ArrayList<String> getPreferencesStates() { return preferencesStates; }

}
