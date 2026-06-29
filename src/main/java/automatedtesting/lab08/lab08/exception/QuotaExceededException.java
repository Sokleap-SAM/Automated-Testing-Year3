package automatedtesting.lab08.lab08.exception;

/**
 * Thrown when an upload would push a user's used bytes past their quota (R7).
 */
public class QuotaExceededException extends RuntimeException {

    public QuotaExceededException(String message) {
        super(message);
    }
}
