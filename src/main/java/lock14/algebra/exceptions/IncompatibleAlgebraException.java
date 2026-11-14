package lock14.algebra.exceptions;

/**
 * Thrown when elements belonging to incompatible parent algebraic structures
 * (e.g., distinct moduli, scalar rings, or group degrees) are combined.
 */
public class IncompatibleAlgebraException extends IllegalArgumentException {
    public IncompatibleAlgebraException(String message) {
        super(message);
    }
}
