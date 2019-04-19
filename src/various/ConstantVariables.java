package various;

/**
 * Created by Darling on 15/9/2017.
 * make more clear messages
 */
public class ConstantVariables {
    public static int LESS_ENERGY_TYPE = 1;
    public static int ALTERED_WINDOW_TYPE = 2;
    public static int SUGGESTION_COMPUTER_INITIAL = 1;
    public static int SUGGESTION_COMPUTER_CONVERSATION = 2;

    public static int EV_EVALUATE_ACCEPT = 1;
    public static int EV_EVALUATE_REJECT = 2;
    public static int EV_EVALUATE_WAIT = 3;
    public static int EV_EVALUATE_PENDING = 4;
    public static int EV_UPDATE_DELAY = 5;
    public static int EV_UPDATE_CANCEL = 6;

    // new types of messages - maybe change that to 0 and add +1 to the others
    public static int EV_MESSAGE_REQUEST = 0;

    // types of agents.station messages
    // 0: has suggestion, 1: has no suggestion right now but it may have later, 2: has no suggestion (even after the alternatives computation), 3: will send suggestion in a future time slot
    public static int STATION_HAS_SUGGESTION = 0;
    public static int STATION_NEXT_ROUND_SUGGESTION = 1;
    public static int STATION_HAS_NO_SUGGESTION = 2;
    public static int STATION_FUTURE_SLOT_SUGGESTION = 3;

    // for the statistics
    public static String EV_STATE_REQUESTED = "request";
    public static String EV_STATE_ACCEPTED_INITIAL = "accepted_initial";
    public static String EV_STATE_ACCEPTED_ALTERNATIVE = "accepted_alternative";
    public static String EV_STATE_DELAYED = "delay";
    public static String EV_STATE_REJECTED = "reject";
    public static String EV_STATE_CANCELLED = "cancel";
}
