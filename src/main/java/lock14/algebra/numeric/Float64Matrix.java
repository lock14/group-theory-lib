package lock14.algebra.numeric;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Sealed Algebraic Data Type representing an off-heap IEEE-754 {@code double} matrix backed by Java 25
 * Foreign Function and Memory ({@link Arena} and {@link MemorySegment}):
 * <ul>
 *   <li>{@link Float64SquareMatrix} exclusively represents {@code n x n} square matrices ({@code n >= 1})
 *       and exposes square-only operations ({@link Float64SquareMatrix#trace()},
 *       {@link Float64SquareMatrix#determinant()}, {@link Float64SquareMatrix#solve(double[])},
 *       {@link Float64SquareMatrix#tryInverse()}, and {@link Float64SquareMatrix#inverse()}).</li>
 *   <li>{@link Float64RectangularMatrix} exclusively represents {@code m x n} non-square matrices
 *       ({@code m, n >= 1, m != n}).</li>
 * </ul>
 *
 * <p>Instances created without an explicit external {@link Arena} own a confined {@code Arena} that is
 * deterministically freed on {@link #close()}.</p>
 */
public sealed interface Float64Matrix extends AutoCloseable
    permits Float64RectangularMatrix, Float64SquareMatrix {

    /**
     * Allocates a zero-initialized {@code rows x cols} off-heap matrix backed by a newly owned confined {@link Arena},
     * returning a {@link Float64SquareMatrix} when {@code rows == cols} or a {@link Float64RectangularMatrix} otherwise.
     *
     * @param rows the positive row count
     * @param cols the positive column count
     * @return a newly allocated {@link Float64Matrix}
     */
    static Float64Matrix zeros(int rows, int cols) {
        Float64MatrixOps.validateDimensions(rows, cols);
        return rows == cols
            ? Float64SquareMatrix.zeros(rows)
            : Float64RectangularMatrix.zeros(rows, cols);
    }

    /**
     * Allocates a zero-initialized {@code rows x cols} off-heap matrix within an externally managed {@link Arena},
     * returning a {@link Float64SquareMatrix} when {@code rows == cols} or a {@link Float64RectangularMatrix} otherwise.
     *
     * @param rows          the positive row count
     * @param cols          the positive column count
     * @param externalArena the caller-managed arena from which off-heap memory is allocated
     * @return a newly allocated {@link Float64Matrix}
     */
    static Float64Matrix zeros(int rows, int cols, Arena externalArena) {
        Objects.requireNonNull(externalArena, "externalArena cannot be null");
        Float64MatrixOps.validateDimensions(rows, cols);
        return rows == cols
            ? Float64SquareMatrix.zeros(rows, externalArena)
            : Float64RectangularMatrix.zeros(rows, cols, externalArena);
    }

    /**
     * Creates an off-heap matrix initialized from a non-empty rectangular 2D {@code double} array,
     * returning a {@link Float64SquareMatrix} if square or a {@link Float64RectangularMatrix} otherwise.
     *
     * @param values non-empty rectangular 2D array of entries
     * @return a newly allocated {@link Float64Matrix}
     */
    static Float64Matrix of(double[][] values) {
        Float64MatrixOps.validateArray2D(values);
        return values.length == values[0].length
            ? Float64SquareMatrix.of(values)
            : Float64RectangularMatrix.of(values);
    }

    /**
     * Creates a {@code rows x cols} off-heap matrix initialized from flat row-major values,
     * returning a {@link Float64SquareMatrix} when {@code rows == cols} or a {@link Float64RectangularMatrix} otherwise.
     *
     * @param rows           the positive row count
     * @param cols           the positive column count
     * @param rowMajorValues the {@code rows * cols} entries in row-major order
     * @return a newly allocated {@link Float64Matrix}
     */
    static Float64Matrix of(int rows, int cols, double... rowMajorValues) {
        Float64MatrixOps.validateDimensions(rows, cols);
        return rows == cols
            ? Float64SquareMatrix.of(rows, rowMajorValues)
            : Float64RectangularMatrix.of(rows, cols, rowMajorValues);
    }

    /**
     * Creates the {@code size x size} off-heap multiplicative identity {@link Float64SquareMatrix}.
     *
     * @param size the positive square dimension
     * @return a newly allocated {@code size x size} identity {@link Float64SquareMatrix}
     */
    static Float64SquareMatrix identity(int size) {
        return Float64SquareMatrix.identity(size);
    }

    /**
     * Creates the {@code size x size} off-heap multiplicative identity {@link Float64SquareMatrix}
     * within an externally managed {@link Arena}.
     *
     * @param size          the positive square dimension
     * @param externalArena the caller-managed arena from which off-heap memory is allocated
     * @return a newly allocated {@code size x size} identity {@link Float64SquareMatrix}
     */
    static Float64SquareMatrix identity(int size, Arena externalArena) {
        return Float64SquareMatrix.identity(size, externalArena);
    }

    /**
     * Returns the number of rows in this matrix.
     *
     * @return the positive row count
     */
    int rows();

    /**
     * Returns the number of columns in this matrix.
     *
     * @return the positive column count
     */
    int cols();

    /**
     * Returns {@code true} if {@code rows() == cols()}.
     *
     * @return {@code true} if this matrix is a {@link Float64SquareMatrix}
     */
    default boolean isSquare() {
        return rows() == cols();
    }

    /**
     * Returns {@code true} if the underlying {@link MemorySegment} scope is still alive.
     *
     * @return {@code true} if this matrix has not been closed
     */
    boolean isOpen();

    /**
     * Reads the entry at zero-based {@code (row, col)}.
     *
     * @param row zero-based row index in {@code [0, rows())}
     * @param col zero-based column index in {@code [0, cols())}
     * @return the {@code double} value at {@code (row, col)}
     */
    double get(int row, int col);

    /**
     * Writes {@code value} into zero-based {@code (row, col)}, canonicalizing {@code -0.0} to {@code +0.0}.
     *
     * @param row   zero-based row index in {@code [0, rows())}
     * @param col   zero-based column index in {@code [0, cols())}
     * @param value the {@code double} value to store
     */
    void set(int row, int col, double value);

    /**
     * Copies this off-heap matrix into a newly allocated on-heap 2D {@code double} array.
     *
     * @return a {@code rows() x cols()} 2D array copy
     */
    double[][] toArray2D();

    /**
     * Allocates and computes the entrywise sum {@code this + other}.
     *
     * @param other an open matrix of identical dimensions
     * @return a newly allocated sum {@link Float64Matrix}
     */
    Float64Matrix add(Float64Matrix other);

    /**
     * Allocates and computes the entrywise difference {@code this - other}.
     *
     * @param other an open matrix of identical dimensions
     * @return a newly allocated difference {@link Float64Matrix}
     */
    Float64Matrix subtract(Float64Matrix other);

    /**
     * Allocates and computes the scalar multiple {@code factor * this}.
     *
     * @param factor the scalar multiplier
     * @return a newly allocated scaled {@link Float64Matrix}
     */
    Float64Matrix scale(double factor);

    /**
     * Multiplies {@code this * other} using a cache-friendly {@code i-k-j} row-major loop.
     *
     * @param other an open matrix with {@code other.rows() == this.cols()}
     * @return a newly allocated {@code this.rows() x other.cols()} product {@link Float64Matrix}
     */
    Float64Matrix multiply(Float64Matrix other);

    /**
     * Computes the matrix-vector product {@code this * vector}.
     *
     * @param vector an array of length {@link #cols()}
     * @return a newly allocated array of length {@link #rows()}
     */
    double[] multiplyVector(double[] vector);

    /**
     * Allocates and computes the transpose {@code this^T} of shape {@code cols() x rows()}.
     *
     * @return a newly allocated transposed {@link Float64Matrix}
     */
    Float64Matrix transpose();

    /**
     * Returns {@code true} if {@code other} has the same dimensions and every corresponding entry differs by at most {@code epsilon}.
     *
     * @param other   an open matrix to compare against
     * @param epsilon the non-negative finite entrywise tolerance
     * @return {@code true} if all entries match within {@code epsilon}
     */
    boolean approxEquals(Float64Matrix other, double epsilon);

    /**
     * Returns {@code true} if {@code other} has the same dimensions and every corresponding entry differs by at most {@link NumericContext#currentEpsilon()}.
     *
     * @param other an open matrix to compare against
     * @return {@code true} if approximately equal within the active tolerance
     */
    default boolean approxEquals(Float64Matrix other) {
        return approxEquals(other, NumericContext.currentEpsilon());
    }

    @Override
    void close();
}
