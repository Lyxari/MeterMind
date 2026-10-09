package metermind.model;

/**
 * Thrown when a CSV data file operation fails (read, write, parse error).
 */
public class DataFileException extends Exception {
    public DataFileException(String message) {
        super(message);
    }

    public DataFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
