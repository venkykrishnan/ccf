package ccf.domain.standard;

// This class is for standard exceptions for try catch. It wraps all exceptions related to standards.
public class StandardException extends Exception {
    public StandardException(String area, String message) {
        super(area + ": "+ message);
    }

    public StandardException(String area, String message, Throwable cause) {
        super(area + ": " + message, cause);
    }
}