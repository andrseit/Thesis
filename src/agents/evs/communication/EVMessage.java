package agents.evs.communication;

public enum EVMessage {
    EV_MESSAGE_REQUEST ("request"),
    EV_EVALUATE_ACCEPT ("accept"),
    EV_EVALUATE_REJECT ("reject"),
    EV_EVALUATE_WAIT ("wait"),
    EV_EVALUATE_PENDING ("pending"),
    EV_UPDATE_DELAY ("delay"),
    EV_UPDATE_CANCEL ("cancel"),
    EV_UPDATE_EMPTY ("empty");

    private String description;
    EVMessage (String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
