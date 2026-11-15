package lock14.algebra.numeric;

import java.lang.foreign.Arena;
import java.util.Objects;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.numeric.Float64MatrixOps.OffHeapStorage;

/**
 * Off-heap strictly non-square ({@code rows != cols}) IEEE-754 {@code double} matrix backed by Java 25
 * Foreign Function and Memory ({@link Arena}).
 */
public final class Float64RectangularMatrix implements Float64Matrix {

    private final OffHeapStorage storage;

    /**
     * Allocates a zero-initialized {@code rows x cols} off-heap rectangular matrix backed by a newly
     * owned confined {@link Arena}.
     *
     * @param rows the positive row count
     * @param cols the positive column count ({@code cols != rows})
     * @throws IllegalArgumentException if {@code rows <= 0}, {@code cols <= 0}, or {@code rows == cols}
     */
    public Float64RectangularMatrix(int rows, int cols) {
        validateRectangular(rows, cols);
        this.storage = Float64MatrixOps.allocateOwned(rows, cols);
    }

    /**
     * Allocates a zero-initialized {@code rows x cols} off-heap rectangular matrix within an externally
     * managed {@link Arena}.
     *
     * @param rows          the positive row count
     * @param cols          the positive column count ({@code cols != rows})
     * @param externalArena the caller-managed arena from which off-heap memory is allocated
     * @throws IllegalArgumentException if {@code rows <= 0}, {@code cols <= 0}, or {@code rows == cols}
     */
    public Float64RectangularMatrix(int rows, int cols, Arena externalArena) {
        Objects.requireNonNull(externalArena, "externalArena cannot be null");
        validateRectangular(rows, cols);
        this.storage = Float64MatrixOps.allocateExternal(rows, cols, externalArena);
    }

    Float64RectangularMatrix(OffHeapStorage trustedStorage) {
        this.storage = trustedStorage;
    }

    /**
     * Allocates a zero-initialized {@code rows x cols} off-heap rectangular matrix.
     *
     * @param rows the positive row count
     * @param cols the positive column count ({@code cols != rows})
     * @return a newly allocated zero {@link Float64RectangularMatrix}
     */
    public static Float64RectangularMatrix zeros(int rows, int cols) {
        return new Float64RectangularMatrix(rows, cols);
    }

    /**
     * Allocates a zero-initialized {@code rows x cols} off-heap rectangular matrix within {@code externalArena}.
     *
     * @param rows          the positive row count
     * @param cols          the positive column count ({@code cols != rows})
     * @param externalArena the caller-managed arena from which off-heap memory is allocated
     * @return a newly allocated zero {@link Float64RectangularMatrix}
     */
    public static Float64RectangularMatrix zeros(int rows, int cols, Arena externalArena) {
        return new Float64RectangularMatrix(rows, cols, externalArena);
    }

    /**
     * Creates an off-heap rectangular matrix initialized from a non-empty non-square 2D {@code double} array.
     *
     * @param values non-empty non-square 2D array of entries
     * @return a newly allocated {@link Float64RectangularMatrix}
     * @throws IllegalArgumentException if {@code values} is empty or square
     */
    public static Float64RectangularMatrix of(double[][] values) {
        Float64MatrixOps.validateArray2D(values);
        validateRectangular(values.length, values[0].length);
        return new Float64RectangularMatrix(Float64MatrixOps.fromArray2D(values));
    }

    /**
     * Creates a {@code rows x cols} off-heap rectangular matrix initialized from flat row-major values.
     *
     * @param rows           the positive row count
     * @param cols           the positive column count ({@code cols != rows})
     * @param rowMajorValues the {@code rows * cols} entries in row-major order
     * @return a newly allocated {@link Float64RectangularMatrix}
     */
    public static Float64RectangularMatrix of(int rows, int cols, double... rowMajorValues) {
        validateRectangular(rows, cols);
        return new Float64RectangularMatrix(Float64MatrixOps.fromRowMajor(rows, cols, rowMajorValues));
    }

    @Override
    public int rows() {
        return storage.rows();
    }

    @Override
    public int cols() {
        return storage.cols();
    }

    @Override
    public boolean isOpen() {
        return storage.isOpen();
    }

    @Override
    public double get(int row, int col) {
        return storage.get(row, col);
    }

    @Override
    public void set(int row, int col, double value) {
        storage.set(row, col, value);
    }

    @Override
    public double[][] toArray2D() {
        return storage.toArray2D();
    }

    /**
     * Allocates and computes the entrywise sum {@code this + other} of two rectangular matrices.
     *
     * @param other an open rectangular matrix of identical dimensions
     * @return a newly allocated sum {@link Float64RectangularMatrix}
     */
    public Float64RectangularMatrix add(Float64RectangularMatrix other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Float64RectangularMatrix(Float64MatrixOps.add(this.storage, other.storage));
    }

    @Override
    public Float64Matrix add(Float64Matrix other) {
        return switch (Objects.requireNonNull(other, "other cannot be null")) {
            case Float64RectangularMatrix rm -> add(rm);
            case Float64SquareMatrix sq -> {
                storage.ensureOpen();
                sq.storage().ensureOpen();
                throw new DimensionMismatchException(rows(), cols(), sq.dimension(), sq.dimension());
            }
        };
    }

    /**
     * Allocates and computes the entrywise difference {@code this - other} of two rectangular matrices.
     *
     * @param other an open rectangular matrix of identical dimensions
     * @return a newly allocated difference {@link Float64RectangularMatrix}
     */
    public Float64RectangularMatrix subtract(Float64RectangularMatrix other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Float64RectangularMatrix(Float64MatrixOps.subtract(this.storage, other.storage));
    }

    @Override
    public Float64Matrix subtract(Float64Matrix other) {
        return switch (Objects.requireNonNull(other, "other cannot be null")) {
            case Float64RectangularMatrix rm -> subtract(rm);
            case Float64SquareMatrix sq -> {
                storage.ensureOpen();
                sq.storage().ensureOpen();
                throw new DimensionMismatchException(rows(), cols(), sq.dimension(), sq.dimension());
            }
        };
    }

    @Override
    public Float64RectangularMatrix scale(double factor) {
        return new Float64RectangularMatrix(Float64MatrixOps.scale(this.storage, factor));
    }

    @Override
    public Float64Matrix multiply(Float64Matrix other) {
        OffHeapStorage rhs = Float64MatrixOps.storageOf(other);
        return Float64MatrixOps.wrap(Float64MatrixOps.multiply(this.storage, rhs));
    }

    @Override
    public double[] multiplyVector(double[] vector) {
        return Float64MatrixOps.multiplyVector(this.storage, vector);
    }

    @Override
    public Float64RectangularMatrix transpose() {
        return new Float64RectangularMatrix(Float64MatrixOps.transpose(this.storage));
    }

    @Override
    public boolean approxEquals(Float64Matrix other, double epsilon) {
        return Float64MatrixOps.approxEquals(this.storage, Float64MatrixOps.storageOf(other), epsilon);
    }

    @Override
    public void close() {
        storage.close();
    }

    OffHeapStorage storage() {
        return storage;
    }

    private static void validateRectangular(int rows, int cols) {
        Float64MatrixOps.validateDimensions(rows, cols);
        if (rows == cols) {
            throw new IllegalArgumentException(
                "Float64RectangularMatrix requires non-square dimensions (rows != cols), got "
                    + rows + "x" + cols + "; use Float64SquareMatrix or Float64Matrix"
            );
        }
    }
}
