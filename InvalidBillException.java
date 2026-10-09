package metermind.model;

/**
 * Thrown when a bill fails validation (e.g., zero/negative consumption or amount,
 * invalid month, missing required fields).
 */
public class InvalidBillException extends Exception {
    public InvalidBillException(String message) {
        super(message);
    }
}
