package exceptions;

/**
 * Thrown when a destination could not be found
 */
public class BadRequestException extends Exception {

    /**
     * Initialise the exception
     * @param message the message for whatever is catching this exception to see
     */
    public BadRequestException(String message) {
        super(message);
    }

   public BadRequestException() {
        super();
    }
}
