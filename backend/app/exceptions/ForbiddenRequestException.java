package exceptions;

/**
 * An error for a forbidden request
 */
public class ForbiddenRequestException extends Exception {

    /**
     * Create a new exception
     * @param message a helpful error message
     */
    public ForbiddenRequestException(String message) {
        super(message);
    }
}
