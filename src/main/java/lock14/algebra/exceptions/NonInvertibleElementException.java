package lock14.algebra.exceptions;

/**
 * Thrown when attempting to invert a zero or non-unit element (or singular matrix).
 */
public class NonInvertibleElementException extends ArithmeticException {
    public NonInvertibleElementException(String message) {
        super(message);
    }

    public NonInvertibleElementException(Object element) {
        super("Element is not invertible: " + element);
    }
}
