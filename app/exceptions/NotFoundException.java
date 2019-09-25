package exceptions;

/**
 * Thrown when a model could not be found.
 */
public class NotFoundException extends Exception {

    /**
     * Initialise the exception
     */
    public NotFoundException() {
        super();
    }

    /**
     * Initialise the exception
     * @param errorMessage the message for whatever is catching this exception to see
     */
    public NotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
