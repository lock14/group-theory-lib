package lock14.algebra.exceptions;

import java.io.Serial;

/**
 * Thrown when elements belonging to incompatible parent algebraic structures
 * (e.g., distinct moduli, scalar rings, or group degrees) are combined.
 */
public class IncompatibleAlgebraException extends IllegalArgumentException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code IncompatibleAlgebraException} with the specified detail message.
     *
     * @param message the detail message describing the incompatible algebraic structures
     */
    public IncompatibleAlgebraException(String message) {
        super(message);
    }
}
