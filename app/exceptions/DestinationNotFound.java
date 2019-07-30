package exceptions;

/**
 * Thrown when a destination could not be found
 */
public class DestinationNotFound extends Exception {

    /**
     * Initialise the exception
     * @param message the message for whatever is catching this exception to see
     */
    public DestinationNotFound(String message) {
        super(message);
    }
}
