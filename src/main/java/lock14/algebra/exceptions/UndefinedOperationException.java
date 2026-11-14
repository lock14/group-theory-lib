package lock14.algebra.exceptions;

/**
 * Thrown when an operation is mathematically undefined over the underlying algebraic structure
 * (for example, commutative determinant over a non-commutative ring).
 */
public class UndefinedOperationException extends UnsupportedOperationException {
    /**
     * Constructs an {@code UndefinedOperationException} with the specified detail message.
     *
     * @param message the detail message describing why the operation is undefined
     */
    public UndefinedOperationException(String message) {
        super(message);
    }
}
