package lock14.algebra.exceptions;

/**
 * Root unchecked exception for algebraic domain and structural contract violations.
 */
public class AlgebraException extends RuntimeException {
    public AlgebraException(String message) {
        super(message);
    }

    public AlgebraException(String message, Throwable cause) {
        super(message, cause);
    }
}
