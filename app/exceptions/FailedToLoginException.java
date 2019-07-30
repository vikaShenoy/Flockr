package exceptions;

/**
 * Thrown when we fail to log in a user
 */
public class FailedToLoginException extends Exception {

    public FailedToLoginException(String message) {
        super(message);
    }
}
