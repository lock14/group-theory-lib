package lock14.algebra.exceptions;

/**
 * Thrown when an operation is mathematically undefined over the underlying algebraic structure
 * (for example, commutative determinant over a non-commutative ring).
 */
public class UndefinedOperationException extends UnsupportedOperationException {
    public UndefinedOperationException(String message) {
        super(message);
    }
}
