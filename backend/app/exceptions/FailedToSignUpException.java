package exceptions;

/**
 * Thrown when trying to sign up an invalid user.
 */
public class FailedToSignUpException extends Exception {

    public FailedToSignUpException(String message) {
        super(message);
    }
}
