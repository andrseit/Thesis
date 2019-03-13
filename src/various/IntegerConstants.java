package various;

/**
 * Created by Darling on 15/9/2017.
 * make more clear messages
 */
public class IntegerConstants {
    public static int LESS_ENERGY_TYPE = 1;
    public static int ALTERED_WINDOW_TYPE = 2;
    public static int SUGGESTION_COMPUTER_INITIAL = 1;
    public static int SUGGESTION_COMPUTER_CONVERSATION = 2;

    public static int EV_EVALUATE_ACCEPT = 0;
    public static int EV_EVALUATE_REJECT = 1;
    public static int EV_EVALUATE_WAIT = 2;
    public static int EV_EVALUATE_PENDING = 3;

    public static int CPLEX_PROFIT = 0;
    public static int CPLEX_SERVICE = 1;
    public static int WINDOW_STANDARD = 0;
    public static int WINDOW_MIN = 1;
    public static int SUGGESTION_FIRST_ROUND = 1;
    public static int SUGGESTION_SECOND_ROUND = 0;
    public static int INSTANT_OFFER_YES = 1;
    public static int INSTANT_OFFER_NO = 0;

    // new types of messages - maybe change that to 0 and add +1 to the others
    public static int EV_MESSAGE_REQUEST = 4;

    // types of station messages
    // 0: has suggestion, 1: has no suggestion right now but it may have later, 2: has no suggestion (even after the alternatives computation), 3: will send suggestion in a future time slot
    public static int STATION_HAS_SUGGESTION = 0;
    public static int STATION_NEXT_ROUND_SUGGESTION = 1;
    public static int STATION_HAS_NO_SUGGESTION = 2;
    public static int STATION_FUTURE_SLOT_SUGGESTION = 3;
}
