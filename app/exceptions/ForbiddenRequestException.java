package exceptions;

/**
 * Used to represent an exceptional case when the request is forbidden.
 */
public class ForbiddenRequestException extends Exception {

    /**
     * Initialise the exception
     * @param message the message for whatever is catching this exception to see
     */
    public ForbiddenRequestException(String message) {
        super(message);
    }
}
