package lock14.algebra.exceptions;

import java.io.Serial;

/**
 * Thrown when vector or matrix dimensions do not satisfy operation constraints.
 */
public class DimensionMismatchException extends IllegalArgumentException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a {@code DimensionMismatchException} with the specified detail message.
     *
     * @param message the detail message describing the dimension mismatch
     */
    public DimensionMismatchException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code DimensionMismatchException} reporting expected versus actual matrix dimensions.
     *
     * @param expectedRows the expected number of rows
     * @param expectedCols the expected number of columns
     * @param actualRows   the actual number of rows encountered
     * @param actualCols   the actual number of columns encountered
     */
    public DimensionMismatchException(int expectedRows, int expectedCols, int actualRows, int actualCols) {
        super(String.format("Dimension mismatch: expected %dx%d but got %dx%d",
                expectedRows, expectedCols, actualRows, actualCols));
    }
}
