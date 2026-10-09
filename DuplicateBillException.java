package metermind.model;

/**
 * Thrown when attempting to add a bill that duplicates an existing entry
 * (same year + month + utility type).
 */
public class DuplicateBillException extends Exception {
    public DuplicateBillException(String message) {
        super(message);
    }
}
