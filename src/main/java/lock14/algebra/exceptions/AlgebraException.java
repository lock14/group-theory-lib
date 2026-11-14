package lock14.algebra.exceptions;

import java.io.Serial;

/**
 * Root unchecked exception for algebraic domain and structural contract violations.
 */
public class AlgebraException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code AlgebraException} with the specified detail message.
     *
     * @param message the detail message describing the algebraic violation
     */
    public AlgebraException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code AlgebraException} with the specified detail message and cause.
     *
     * @param message the detail message describing the algebraic violation
     * @param cause   the underlying cause of this exception
     */
    public AlgebraException(String message, Throwable cause) {
        super(message, cause);
    }
}
