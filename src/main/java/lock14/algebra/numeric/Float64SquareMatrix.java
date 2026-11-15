package lock14.algebra.numeric;

import java.lang.foreign.Arena;
import java.util.Objects;
import java.util.Optional;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.numeric.Float64MatrixOps.OffHeapStorage;

/**
 * Off-heap {@code n x n} square IEEE-754 {@code double} matrix backed by Java 25 Foreign Function and Memory
 * ({@link Arena}).
 *
 * <p>Linear system solvers ({@link #solve(double[])}, {@link #tryInverse()}, {@link #inverse()}) use partial
 * pivoting and respect {@link NumericContext#EPSILON} when bound.</p>
 */
public final class Float64SquareMatrix implements Float64Matrix {

    private final OffHeapStorage storage;

    /**
     * Allocates a zero-initialized {@code dimension x dimension} off-heap square matrix backed by a newly
     * owned confined {@link Arena}.
     *
     * @param dimension the positive square dimension {@code n}
     */
    public Float64SquareMatrix(int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Square matrix dimension must be positive, got: " + dimension);
        }
        this.storage = Float64MatrixOps.allocateOwned(dimension, dimension);
    }

    /**
     * Allocates a zero-initialized {@code dimension x dimension} off-heap square matrix within an externally
     * managed {@link Arena}.
     *
     * @param dimension     the positive square dimension {@code n}
     * @param externalArena the caller-managed arena from which off-heap memory is allocated
     */
    public Float64SquareMatrix(int dimension, Arena externalArena) {
        Objects.requireNonNull(externalArena, "externalArena cannot be null");
        if (dimension <= 0) {
            throw new IllegalArgumentException("Square matrix dimension must be positive, got: " + dimension);
        }
        this.storage = Float64MatrixOps.allocateExternal(dimension, dimension, externalArena);
    }

    Float64SquareMatrix(OffHeapStorage trustedStorage) {
        this.storage = trustedStorage;
    }

    /**
     * Allocates a zero-initialized {@code dimension x dimension} off-heap square matrix.
     *
     * @param dimension the positive square dimension {@code n}
     * @return a newly allocated zero {@link Float64SquareMatrix}
     */
    public static Float64SquareMatrix zeros(int dimension) {
        return new Float64SquareMatrix(dimension);
    }

    /**
     * Allocates a zero-initialized {@code dimension x dimension} off-heap square matrix within {@code externalArena}.
     *
     * @param dimension     the positive square dimension {@code n}
     * @param externalArena the caller-managed arena from which off-heap memory is allocated
     * @return a newly allocated zero {@link Float64SquareMatrix}
     */
    public static Float64SquareMatrix zeros(int dimension, Arena externalArena) {
        return new Float64SquareMatrix(dimension, externalArena);
    }

    /**
     * Creates the {@code dimension x dimension} off-heap multiplicative identity matrix.
     *
     * @param dimension the positive square dimension {@code n}
     * @return a newly allocated {@code dimension x dimension} identity {@link Float64SquareMatrix}
     */
    public static Float64SquareMatrix identity(int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Identity size must be positive, got: " + dimension);
        }
        Float64SquareMatrix id = new Float64SquareMatrix(dimension);
        for (int i = 0; i < dimension; i++) {
            id.set(i, i, 1.0);
        }
        return id;
    }

    /**
     * Creates the {@code dimension x dimension} off-heap multiplicative identity matrix within {@code externalArena}.
     *
     * @param dimension     the positive square dimension {@code n}
     * @param externalArena the caller-managed arena from which off-heap memory is allocated
     * @return a newly allocated {@code dimension x dimension} identity {@link Float64SquareMatrix}
     */
    public static Float64SquareMatrix identity(int dimension, Arena externalArena) {
        Objects.requireNonNull(externalArena, "externalArena cannot be null");
        if (dimension <= 0) {
            throw new IllegalArgumentException("Identity size must be positive, got: " + dimension);
        }
        Float64SquareMatrix id = new Float64SquareMatrix(dimension, externalArena);
        for (int i = 0; i < dimension; i++) {
            id.set(i, i, 1.0);
        }
        return id;
    }

    /**
     * Creates an off-heap square matrix initialized from a non-empty square 2D {@code double} array.
     *
     * @param values non-empty square 2D array of entries
     * @return a newly allocated {@link Float64SquareMatrix}
     * @throws IllegalArgumentException if {@code values} is empty or not square
     */
    public static Float64SquareMatrix of(double[][] values) {
        Float64MatrixOps.validateArray2D(values);
        if (values.length != values[0].length) {
            throw new IllegalArgumentException(
                "Provided 2D array is not square: " + values.length + "x" + values[0].length
            );
        }
        return new Float64SquareMatrix(Float64MatrixOps.fromArray2D(values));
    }

    /**
     * Creates a {@code dimension x dimension} off-heap square matrix initialized from flat row-major values.
     *
     * @param dimension      the positive square dimension {@code n}
     * @param rowMajorValues the {@code dimension * dimension} entries in row-major order
     * @return a newly allocated {@link Float64SquareMatrix}
     */
    public static Float64SquareMatrix of(int dimension, double... rowMajorValues) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Square matrix dimension must be positive, got: " + dimension);
        }
        return new Float64SquareMatrix(Float64MatrixOps.fromRowMajor(dimension, dimension, rowMajorValues));
    }

    /**
     * Returns the square dimension {@code n} of this {@code n x n} matrix.
     *
     * @return the positive matrix dimension
     */
    public int dimension() {
        return storage.rows();
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
     * Allocates and computes the entrywise sum {@code this + other} of two square matrices.
     *
     * @param other an open square matrix of identical dimension
     * @return a newly allocated sum {@link Float64SquareMatrix}
     */
    public Float64SquareMatrix add(Float64SquareMatrix other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Float64SquareMatrix(Float64MatrixOps.add(this.storage, other.storage));
    }

    @Override
    public Float64Matrix add(Float64Matrix other) {
        return switch (Objects.requireNonNull(other, "other cannot be null")) {
            case Float64SquareMatrix sq -> add(sq);
            case Float64RectangularMatrix rm -> {
                storage.ensureOpen();
                rm.storage().ensureOpen();
                throw new DimensionMismatchException(dimension(), dimension(), rm.rows(), rm.cols());
            }
        };
    }

    /**
     * Allocates and computes the entrywise difference {@code this - other} of two square matrices.
     *
     * @param other an open square matrix of identical dimension
     * @return a newly allocated difference {@link Float64SquareMatrix}
     */
    public Float64SquareMatrix subtract(Float64SquareMatrix other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Float64SquareMatrix(Float64MatrixOps.subtract(this.storage, other.storage));
    }

    @Override
    public Float64Matrix subtract(Float64Matrix other) {
        return switch (Objects.requireNonNull(other, "other cannot be null")) {
            case Float64SquareMatrix sq -> subtract(sq);
            case Float64RectangularMatrix rm -> {
                storage.ensureOpen();
                rm.storage().ensureOpen();
                throw new DimensionMismatchException(dimension(), dimension(), rm.rows(), rm.cols());
            }
        };
    }

    @Override
    public Float64SquareMatrix scale(double factor) {
        return new Float64SquareMatrix(Float64MatrixOps.scale(this.storage, factor));
    }

    /**
     * Multiplies two square matrices {@code this * other}.
     *
     * @param other an open square matrix of identical dimension
     * @return a newly allocated product {@link Float64SquareMatrix}
     */
    public Float64SquareMatrix multiply(Float64SquareMatrix other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Float64SquareMatrix(Float64MatrixOps.multiply(this.storage, other.storage));
    }

    /**
     * Multiplies this square matrix by a rectangular matrix {@code this * other}.
     *
     * @param other an open rectangular matrix with {@code other.rows() == this.dimension()}
     * @return a newly allocated product {@link Float64RectangularMatrix}
     */
    public Float64RectangularMatrix multiply(Float64RectangularMatrix other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Float64RectangularMatrix(Float64MatrixOps.multiply(this.storage, other.storage()));
    }

    @Override
    public Float64Matrix multiply(Float64Matrix other) {
        return switch (Objects.requireNonNull(other, "other cannot be null")) {
            case Float64SquareMatrix sq -> multiply(sq);
            case Float64RectangularMatrix rm -> multiply(rm);
        };
    }

    @Override
    public double[] multiplyVector(double[] vector) {
        return Float64MatrixOps.multiplyVector(this.storage, vector);
    }

    @Override
    public Float64SquareMatrix transpose() {
        return new Float64SquareMatrix(Float64MatrixOps.transpose(this.storage));
    }

    /**
     * Computes the trace {@code sum_i this[i, i]}.
     *
     * @return the sum of the main diagonal entries
     */
    public double trace() {
        return Float64MatrixOps.trace(this.storage);
    }

    /**
     * Computes the determinant using Gaussian elimination with partial pivoting.
     *
     * @return the approximate determinant
     */
    public double determinant() {
        return Float64MatrixOps.determinant(this.storage);
    }

    /**
     * Solves {@code this * x = b} using Gaussian elimination with partial pivoting.
     *
     * @param b the right-hand side vector of length {@link #dimension()}
     * @return the solution vector {@code x}
     * @throws DimensionMismatchException    if {@code b.length != dimension()}
     * @throws NonInvertibleElementException if {@code this} is numerically singular within the active tolerance
     */
    public double[] solve(double[] b) {
        return Float64MatrixOps.solve(this.storage, b);
    }

    /**
     * Attempts to compute {@code this^-1} via partial-pivoted Gauss-Jordan elimination.
     *
     * @return an {@link Optional} containing a newly allocated inverse {@link Float64SquareMatrix} if nonsingular,
     *     or {@link Optional#empty()} if numerically singular within the active tolerance
     */
    public Optional<Float64SquareMatrix> tryInverse() {
        try {
            return Optional.of(inverse());
        } catch (NonInvertibleElementException ex) {
            return Optional.empty();
        }
    }

    /**
     * Computes {@code this^-1} via partial-pivoted Gauss-Jordan elimination.
     *
     * @return a newly allocated inverse {@link Float64SquareMatrix}
     * @throws NonInvertibleElementException if {@code this} is numerically singular within the active tolerance
     */
    public Float64SquareMatrix inverse() {
        return new Float64SquareMatrix(Float64MatrixOps.inverse(this.storage));
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
}
