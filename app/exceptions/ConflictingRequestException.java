package exceptions;

/**
 * Used to represent an exceptional case when the request is conflicting.
 */
public class ConflictingRequestException extends Exception {

    /**
     * Initialise the exception
     * @param message the message for whatever is catching this exception to see
     */
    public ConflictingRequestException(String message) {
        super(message);
    }
}
