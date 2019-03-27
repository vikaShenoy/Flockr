package exceptions;

/**
 * Thrown when a destination could not be found
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
