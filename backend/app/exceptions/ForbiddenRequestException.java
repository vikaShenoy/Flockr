package exceptions;

/**
 * Used to represent an exceptional case when the request is forbidden.
 */
public class ForbiddenRequestException extends Exception {

    public ForbiddenRequestException(String message) {
        super(message);
    }
}
