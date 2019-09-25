package exceptions;

/**
 * Thrown when trying to sign up an invalid user.
 */
public class FailedToSignUpException extends Exception {

    /**
     * Initialise the exception
     * @param message the message for whatever is catching this exception to see
     */
    public FailedToSignUpException(String message) {
        super(message);
    }
}
