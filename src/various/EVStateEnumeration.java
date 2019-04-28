package various;

public enum  EVStateEnumeration {

    EV_STATE_REQUESTED ("request"),
    EV_STATE_ACCEPTED ("accepted"),
    EV_STATE_ACCEPTED_INITIAL ("accepted_initial"),
    EV_STATE_ACCEPTED_ALTERNATIVE ("accepted_alternative"),
    EV_STATE_DELAYED ("delay"),
    EV_STATE_REJECTED ("reject"),
    EV_STATE_CANCELLED ("cancel");

    private String description;
    EVStateEnumeration (String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
