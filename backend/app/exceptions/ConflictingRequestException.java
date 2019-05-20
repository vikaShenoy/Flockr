package exceptions;

/**
 * Used to represent an exceptional case when the request is conflicting.
 */
public class ConflictingRequestException extends Exception {

    public ConflictingRequestException(String message) {
        super(message);
    }
}
