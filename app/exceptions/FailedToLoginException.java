package exceptions;

/**
 * Thrown when we fail to log in a user
 */
public class FailedToLoginException extends Exception {

    /**
     * Initialise the exception
     * @param message the message for whatever is catching this exception to see
     */
    public FailedToLoginException(String message) {
        super(message);
    }
}
