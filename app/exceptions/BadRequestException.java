package exceptions;

/**
 * Thrown when a request contains invalid parameters.
 */
public class BadRequestException extends Exception {

    /**
     * Initialise the exception
     * @param message the message for whatever is catching this exception to see
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Initialises a bad request exception without an error
      */
    public BadRequestException() {
        super();
    }
}
