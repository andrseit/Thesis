package exceptions;

/**
 * Created by Thesis on 29/1/2018.
 */
public class NoPricingException extends Exception {
    public NoPricingException () {
        super("New instance of pricing was not created.");
    }
}
