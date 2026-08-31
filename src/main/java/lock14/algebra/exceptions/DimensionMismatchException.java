package lock14.algebra.exceptions;

public class DimensionMismatchException extends IllegalArgumentException {
    public DimensionMismatchException(String message) {
        super(message);
    }

    public DimensionMismatchException(int expectedRows, int expectedCols, int actualRows, int actualCols) {
        super(String.format("Dimension mismatch: expected %dx%d but got %dx%d",
                expectedRows, expectedCols, actualRows, actualCols));
    }
}
