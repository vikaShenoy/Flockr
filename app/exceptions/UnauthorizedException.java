package exceptions;

/**
 * Thrown when a user is not authorized.
 */
public class UnauthorizedException extends Exception {

    /**
     * Initialise the exception
     * @param message the message for whatever is catching this exception to see
     */
    public UnauthorizedException(String message) {
        super(message);
    }


    /**
     * Initialise the exception without message
     */
    public UnauthorizedException() {
        super();
    }
}
