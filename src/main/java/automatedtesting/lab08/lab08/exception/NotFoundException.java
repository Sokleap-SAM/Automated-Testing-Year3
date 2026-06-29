package automatedtesting.lab08.lab08.exception;

/**
 * Thrown when a resource does not exist <em>or</em> is not owned by the current
 * user. Returning the same 404 for both cases is what enforces isolation (R8):
 * a user can never tell another user's data apart from data that simply isn't there.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
