package exceptions;

/**
 * Used to represent an exceptional case when the request is forbidden.
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
