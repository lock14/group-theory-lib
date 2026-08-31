package lock14.algebra.exceptions;

public class NonInvertibleElementException extends ArithmeticException {
    public NonInvertibleElementException(String message) {
        super(message);
    }

    public NonInvertibleElementException(Object element) {
        super("Element is not invertible: " + element);
    }
}
