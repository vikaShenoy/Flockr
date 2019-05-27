package exceptions;

/**
 * Thrown when a destination could not be found
 */
public class NotFoundException extends Exception {

    /**
     * Initialise the exception
     */
    public NotFoundException() {
        super();
    }

    public NotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
