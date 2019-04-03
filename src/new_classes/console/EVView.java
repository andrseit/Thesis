package new_classes.console;

/**
 * Created by Thesis on 2/4/2019.
 */
public class EVView {

    private String evID;
    private String state; // request, waiting, delay, cancel, charge...
    private String preferences; // what it asks, or when it will charge etc

    public EVView(String evID, String state, String preferences) {
        this.evID = evID;
        this.state = state;
        this.preferences = preferences;
    }

    @Override
    public String toString() {
        return "EVView{" +
                "evID='" + evID + '\'' +
                ", state='" + state + '\'' +
                ", preferences='" + preferences + '\'' +
                '}';
    }

    public String getEvID() {
        return evID;
    }

    public void setEvID(String evID) {
        this.evID = evID;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }
}
