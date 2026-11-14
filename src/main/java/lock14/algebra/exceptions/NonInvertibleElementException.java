package lock14.algebra.exceptions;

/**
 * Thrown when attempting to invert a zero or non-unit element (or singular matrix).
 */
public class NonInvertibleElementException extends ArithmeticException {
    /**
     * Constructs a {@code NonInvertibleElementException} with the specified detail message.
     *
     * @param message the detail message describing why inversion failed
     */
    public NonInvertibleElementException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code NonInvertibleElementException} identifying the non-invertible element.
     *
     * @param element the element that could not be inverted
     */
    public NonInvertibleElementException(Object element) {
        super("Element is not invertible: " + element);
    }
}
