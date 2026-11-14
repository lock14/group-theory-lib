package lock14.algebra.exceptions;

import java.io.Serial;

/**
 * Thrown when an operation is mathematically undefined over the underlying algebraic structure
 * (for example, commutative determinant over a non-commutative ring).
 */
public class UndefinedOperationException extends UnsupportedOperationException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code UndefinedOperationException} with the specified detail message.
     *
     * @param message the detail message describing why the operation is undefined
     */
    public UndefinedOperationException(String message) {
        super(message);
    }
}
