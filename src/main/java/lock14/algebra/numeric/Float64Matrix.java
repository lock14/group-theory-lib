package lock14.algebra.numeric;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.exceptions.UndefinedOperationException;

/**
 * High-performance off-heap IEEE-754 {@code double} matrix backed by Java 25 Foreign Function &amp; Memory
 * ({@link Arena} and {@link MemorySegment}).
 *
 * <p>Instances created without an explicit external {@link Arena} own a confined {@code Arena} that is
 * deterministically freed on {@link #close()}. Linear system solvers ({@link #solve(double[])},
 * {@link #inverse()}) use partial pivoting and respect {@link NumericContext#EPSILON} when bound.</p>
 */
public final class Float64Matrix implements AutoCloseable {

    private static final long DOUBLE_BYTE_SIZE = ValueLayout.JAVA_DOUBLE.byteSize();

    private final int rows;
    private final int cols;
    private final Arena arena;
    private final boolean ownsArena;
    private final MemorySegment segment;

    /**
     * Allocates a zero-initialized {@code rows x cols} off-heap matrix backed by a newly owned confined {@link Arena}.
     *
     * @param rows the positive row count
     * @param cols the positive column count
     */
    public Float64Matrix(int rows, int cols) {
        validateDimensions(rows, cols);
        long byteSize = computeByteSize(rows, cols);
        Arena owned = Arena.ofConfined();
        try {
            this.rows = rows;
            this.cols = cols;
            this.arena = owned;
            this.ownsArena = true;
            this.segment = owned.allocate(byteSize, ValueLayout.JAVA_DOUBLE.byteAlignment());
        } catch (Throwable t) {
            owned.close();
            throw t;
        }
    }

    /**
     * Allocates a zero-initialized {@code rows x cols} off-heap matrix within an externally managed {@link Arena}.
     *
     * @param rows          the positive row count
     * @param cols          the positive column count
     * @param externalArena the caller-managed arena from which off-heap memory is allocated
     */
    public Float64Matrix(int rows, int cols, Arena externalArena) {
        Objects.requireNonNull(externalArena, "externalArena cannot be null");
        validateDimensions(rows, cols);
        long byteSize = computeByteSize(rows, cols);
        this.rows = rows;
        this.cols = cols;
        this.arena = externalArena;
        this.ownsArena = false;
        this.segment = externalArena.allocate(byteSize, ValueLayout.JAVA_DOUBLE.byteAlignment());
    }

    /**
     * Creates an off-heap matrix initialized from a non-empty rectangular 2D {@code double} array.
     *
     * @param values non-empty rectangular 2D array of entries
     * @return a newly allocated {@link Float64Matrix}
     */
    public static Float64Matrix of(double[][] values) {
        Objects.requireNonNull(values, "values cannot be null");
        if (values.length == 0 || values[0] == null || values[0].length == 0) {
            throw new IllegalArgumentException("Matrix values must be non-empty");
        }
        int r = values.length;
        int c = values[0].length;
        for (int i = 0; i < r; i++) {
            if (values[i] == null || values[i].length != c) {
                throw new DimensionMismatchException("Jagged array rows are not permitted");
            }
        }
        Float64Matrix matrix = new Float64Matrix(r, c);
        try {
            for (int i = 0; i < r; i++) {
                long rowOffset = (long) i * c * DOUBLE_BYTE_SIZE;
                MemorySegment.copy(values[i], 0, matrix.segment, ValueLayout.JAVA_DOUBLE, rowOffset, c);
            }
            return matrix;
        } catch (Throwable t) {
            matrix.close();
            throw t;
        }
    }

    /**
     * Creates a {@code rows x cols} off-heap matrix initialized from flat row-major values.
     *
     * @param rows           the positive row count
     * @param cols           the positive column count
     * @param rowMajorValues the {@code rows * cols} entries in row-major order
     * @return a newly allocated {@link Float64Matrix}
     */
    public static Float64Matrix of(int rows, int cols, double... rowMajorValues) {
        Objects.requireNonNull(rowMajorValues, "rowMajorValues cannot be null");
        validateDimensions(rows, cols);
        long expected = Math.multiplyExact((long) rows, (long) cols);
        if (rowMajorValues.length != expected) {
            throw new DimensionMismatchException(
                "Expected " + expected + " values for " + rows + "x" + cols + " matrix, got " + rowMajorValues.length
            );
        }
        Float64Matrix matrix = new Float64Matrix(rows, cols);
        try {
            MemorySegment.copy(
                rowMajorValues, 0, matrix.segment, ValueLayout.JAVA_DOUBLE, 0L, rowMajorValues.length
            );
            return matrix;
        } catch (Throwable t) {
            matrix.close();
            throw t;
        }
    }

    /**
     * Creates the {@code size x size} off-heap multiplicative identity matrix.
     *
     * @param size the positive square dimension
     * @return a newly allocated {@code size x size} identity {@link Float64Matrix}
     */
    public static Float64Matrix identity(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Identity size must be positive, got: " + size);
        }
        Float64Matrix id = new Float64Matrix(size, size);
        for (int i = 0; i < size; i++) {
            id.set(i, i, 1.0);
        }
        return id;
    }

    private static void validateDimensions(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive, got: " + rows + "x" + cols);
        }
    }

    private static long computeByteSize(int rows, int cols) {
        try {
            long count = Math.multiplyExact((long) rows, (long) cols);
            return Math.multiplyExact(count, DOUBLE_BYTE_SIZE);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                "Matrix dimensions overflow off-heap byte allocation: " + rows + "x" + cols, ex
            );
        }
    }

    /**
     * Returns the number of rows in this matrix.
     *
     * @return the positive row count
     */
    public int rows() {
        return rows;
    }

    /**
     * Returns the number of columns in this matrix.
     *
     * @return the positive column count
     */
    public int cols() {
        return cols;
    }

    /**
     * Returns {@code true} if {@code rows() == cols()}.
     *
     * @return {@code true} if this matrix is square
     */
    public boolean isSquare() {
        return rows == cols;
    }

    /**
     * Returns {@code true} if the underlying {@link MemorySegment} scope is still alive.
     *
     * @return {@code true} if this matrix has not been closed
     */
    public boolean isOpen() {
        return segment.scope().isAlive();
    }

    private void ensureOpen() {
        if (!segment.scope().isAlive()) {
            throw new IllegalStateException("Float64Matrix arena has already been closed");
        }
    }

    private long index(int row, int col) {
        Objects.checkIndex(row, rows);
        Objects.checkIndex(col, cols);
        return (long) row * cols + col;
    }

    /**
     * Reads the entry at zero-based {@code (row, col)}.
     *
     * @param row zero-based row index in {@code [0, rows())}
     * @param col zero-based column index in {@code [0, cols())}
     * @return the {@code double} value at {@code (row, col)}
     */
    public double get(int row, int col) {
        ensureOpen();
        return segment.getAtIndex(ValueLayout.JAVA_DOUBLE, index(row, col));
    }

    /**
     * Writes {@code value} into zero-based {@code (row, col)}, canonicalizing {@code -0.0} to {@code +0.0}.
     *
     * @param row   zero-based row index in {@code [0, rows())}
     * @param col   zero-based column index in {@code [0, cols())}
     * @param value the {@code double} value to store
     */
    public void set(int row, int col, double value) {
        ensureOpen();
        segment.setAtIndex(ValueLayout.JAVA_DOUBLE, index(row, col), value == 0.0 ? 0.0 : value);
    }

    /**
     * Copies this off-heap matrix into a newly allocated on-heap 2D {@code double} array.
     *
     * @return a {@code rows() x cols()} 2D array copy
     */
    public double[][] toArray2D() {
        ensureOpen();
        double[][] out = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            long rowOffset = (long) i * cols * DOUBLE_BYTE_SIZE;
            MemorySegment.copy(segment, ValueLayout.JAVA_DOUBLE, rowOffset, out[i], 0, cols);
        }
        return out;
    }

    /**
     * Allocates and computes the entrywise sum {@code this + other}.
     *
     * @param other an open matrix of identical dimensions
     * @return a newly allocated sum {@link Float64Matrix}
     */
    public Float64Matrix add(Float64Matrix other) {
        Objects.requireNonNull(other, "other cannot be null");
        ensureOpen();
        other.ensureOpen();
        if (this.rows != other.rows || this.cols != other.cols) {
            throw new DimensionMismatchException(
                "Cannot add " + rows + "x" + cols + " and " + other.rows + "x" + other.cols + " matrices"
            );
        }
        Float64Matrix result = new Float64Matrix(rows, cols);
        try {
            long total = (long) rows * cols;
            for (long idx = 0; idx < total; idx++) {
                double sum = this.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, idx)
                    + other.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, idx);
                result.segment.setAtIndex(ValueLayout.JAVA_DOUBLE, idx, sum == 0.0 ? 0.0 : sum);
            }
            return result;
        } catch (Throwable t) {
            result.close();
            throw t;
        }
    }

    /**
     * Allocates and computes the entrywise difference {@code this - other}.
     *
     * @param other an open matrix of identical dimensions
     * @return a newly allocated difference {@link Float64Matrix}
     */
    public Float64Matrix subtract(Float64Matrix other) {
        Objects.requireNonNull(other, "other cannot be null");
        ensureOpen();
        other.ensureOpen();
        if (this.rows != other.rows || this.cols != other.cols) {
            throw new DimensionMismatchException(
                "Cannot subtract " + rows + "x" + cols + " and " + other.rows + "x" + other.cols + " matrices"
            );
        }
        Float64Matrix result = new Float64Matrix(rows, cols);
        try {
            long total = (long) rows * cols;
            for (long idx = 0; idx < total; idx++) {
                double diff = this.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, idx)
                    - other.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, idx);
                result.segment.setAtIndex(ValueLayout.JAVA_DOUBLE, idx, diff == 0.0 ? 0.0 : diff);
            }
            return result;
        } catch (Throwable t) {
            result.close();
            throw t;
        }
    }

    /**
     * Allocates and computes the scalar multiple {@code factor * this}.
     *
     * @param factor the scalar multiplier
     * @return a newly allocated scaled {@link Float64Matrix}
     */
    public Float64Matrix scale(double factor) {
        ensureOpen();
        Float64Matrix result = new Float64Matrix(rows, cols);
        try {
            long total = (long) rows * cols;
            for (long idx = 0; idx < total; idx++) {
                double prod = this.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, idx) * factor;
                result.segment.setAtIndex(ValueLayout.JAVA_DOUBLE, idx, prod == 0.0 ? 0.0 : prod);
            }
            return result;
        } catch (Throwable t) {
            result.close();
            throw t;
        }
    }

    /**
     * Multiplies {@code this * other} using a cache-friendly {@code i-k-j} row-major loop.
     *
     * @param other an open matrix with {@code other.rows() == this.cols()}
     * @return a newly allocated {@code this.rows() x other.cols()} product {@link Float64Matrix}
     */
    public Float64Matrix multiply(Float64Matrix other) {
        Objects.requireNonNull(other, "other cannot be null");
        ensureOpen();
        other.ensureOpen();
        if (this.cols != other.rows) {
            throw new DimensionMismatchException(
                "Cannot multiply " + rows + "x" + cols + " by " + other.rows + "x" + other.cols
            );
        }
        Float64Matrix result = new Float64Matrix(this.rows, other.cols);
        try {
            int m = this.rows;
            int p = this.cols;
            int n = other.cols;
            for (int i = 0; i < m; i++) {
                long aRow = (long) i * p;
                long cRow = (long) i * n;
                for (int k = 0; k < p; k++) {
                    double aik = this.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, aRow + k);
                    if (aik == 0.0) {
                        continue;
                    }
                    long bRow = (long) k * n;
                    for (int j = 0; j < n; j++) {
                        double cij = result.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, cRow + j)
                            + aik * other.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, bRow + j);
                        result.segment.setAtIndex(ValueLayout.JAVA_DOUBLE, cRow + j, cij);
                    }
                }
            }
            return result;
        } catch (Throwable t) {
            result.close();
            throw t;
        }
    }

    /**
     * Computes the matrix-vector product {@code this * vector}.
     *
     * @param vector an array of length {@link #cols()}
     * @return a newly allocated array of length {@link #rows()}
     */
    public double[] multiplyVector(double[] vector) {
        Objects.requireNonNull(vector, "vector cannot be null");
        ensureOpen();
        if (vector.length != cols) {
            throw new DimensionMismatchException(
                "Cannot multiply " + rows + "x" + cols + " matrix by vector of length " + vector.length
            );
        }
        double[] out = new double[rows];
        for (int i = 0; i < rows; i++) {
            double sum = 0.0;
            long rowBase = (long) i * cols;
            for (int j = 0; j < cols; j++) {
                sum += segment.getAtIndex(ValueLayout.JAVA_DOUBLE, rowBase + j) * vector[j];
            }
            out[i] = sum == 0.0 ? 0.0 : sum;
        }
        return out;
    }

    /**
     * Allocates and computes the transpose {@code this^T} of shape {@code cols() x rows()}.
     *
     * @return a newly allocated transposed {@link Float64Matrix}
     */
    public Float64Matrix transpose() {
        ensureOpen();
        Float64Matrix result = new Float64Matrix(cols, rows);
        try {
            for (int i = 0; i < rows; i++) {
                long rowBase = (long) i * cols;
                for (int j = 0; j < cols; j++) {
                    double val = segment.getAtIndex(ValueLayout.JAVA_DOUBLE, rowBase + j);
                    result.segment.setAtIndex(ValueLayout.JAVA_DOUBLE, (long) j * rows + i, val);
                }
            }
            return result;
        } catch (Throwable t) {
            result.close();
            throw t;
        }
    }

    private double singularityThreshold(double[][] work) {
        if (NumericContext.EPSILON.isBound()) {
            return NumericContext.currentEpsilon();
        }
        double maxAbs = 0.0;
        for (double[] row : work) {
            for (double v : row) {
                double a = Math.abs(v);
                if (a > maxAbs) {
                    maxAbs = a;
                }
            }
        }
        return Math.scalb(1.0, -50) * work.length * maxAbs;
    }

    /**
     * Computes the determinant of a square matrix using Gaussian elimination with partial pivoting.
     *
     * @return the approximate determinant
     * @throws UndefinedOperationException if this matrix is not square
     */
    public double determinant() {
        ensureOpen();
        if (!isSquare()) {
            throw new UndefinedOperationException(
                "Determinant is only defined for square matrices, got: " + rows + "x" + cols
            );
        }
        double[][] a = toArray2D();
        double threshold = singularityThreshold(a);
        int n = rows;
        double sign = 1.0;

        for (int col = 0; col < n; col++) {
            int pivotRow = col;
            double maxAbs = Math.abs(a[col][col]);
            for (int r = col + 1; r < n; r++) {
                double candidate = Math.abs(a[r][col]);
                if (candidate > maxAbs) {
                    maxAbs = candidate;
                    pivotRow = r;
                }
            }
            if (maxAbs <= threshold) {
                return 0.0;
            }
            if (pivotRow != col) {
                double[] tmp = a[col];
                a[col] = a[pivotRow];
                a[pivotRow] = tmp;
                sign = -sign;
            }
            double pivot = a[col][col];
            for (int r = col + 1; r < n; r++) {
                double factor = a[r][col] / pivot;
                if (factor != 0.0) {
                    for (int c = col + 1; c < n; c++) {
                        a[r][c] -= factor * a[col][c];
                    }
                }
            }
        }

        double det = sign;
        for (int i = 0; i < n; i++) {
            det *= a[i][i];
        }
        return det == 0.0 ? 0.0 : det;
    }

    /**
     * Solves {@code A * x = b} using Gaussian elimination with partial pivoting.
     *
     * @param b the right-hand side vector of length {@link #rows()}
     * @return the solution vector {@code x}
     * @throws NonInvertibleElementException if {@code A} is singular within the active tolerance
     */
    public double[] solve(double[] b) {
        Objects.requireNonNull(b, "b cannot be null");
        ensureOpen();
        if (!isSquare()) {
            throw new UndefinedOperationException(
                "Direct square solve requires a square matrix, got: " + rows + "x" + cols
            );
        }
        if (b.length != rows) {
            throw new DimensionMismatchException(
                "Right-hand side length " + b.length + " does not match matrix dimension " + rows
            );
        }
        int n = rows;
        double[][] a = toArray2D();
        double[] rhs = b.clone();
        double threshold = singularityThreshold(a);

        for (int col = 0; col < n; col++) {
            int pivotRow = col;
            double maxAbs = Math.abs(a[col][col]);
            for (int r = col + 1; r < n; r++) {
                double candidate = Math.abs(a[r][col]);
                if (candidate > maxAbs) {
                    maxAbs = candidate;
                    pivotRow = r;
                }
            }
            if (maxAbs <= threshold) {
                throw new NonInvertibleElementException(
                    "Float64Matrix is numerically singular at column " + col + " (|pivot| = " + maxAbs + ")"
                );
            }
            if (pivotRow != col) {
                double[] tmpRow = a[col];
                a[col] = a[pivotRow];
                a[pivotRow] = tmpRow;

                double tmpRhs = rhs[col];
                rhs[col] = rhs[pivotRow];
                rhs[pivotRow] = tmpRhs;
            }

            double pivot = a[col][col];
            for (int r = col + 1; r < n; r++) {
                double factor = a[r][col] / pivot;
                if (factor != 0.0) {
                    for (int c = col + 1; c < n; c++) {
                        a[r][c] -= factor * a[col][c];
                    }
                    rhs[r] -= factor * rhs[col];
                }
            }
        }

        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = rhs[i];
            for (int j = i + 1; j < n; j++) {
                sum -= a[i][j] * x[j];
            }
            double xi = sum / a[i][i];
            x[i] = xi == 0.0 ? 0.0 : xi;
        }
        return x;
    }

    /**
     * Computes {@code A^-1} via partial-pivoted Gauss-Jordan elimination.
     *
     * @return a newly allocated inverse {@link Float64Matrix}
     * @throws NonInvertibleElementException if {@code A} is numerically singular within the active tolerance
     */
    public Float64Matrix inverse() {
        ensureOpen();
        if (!isSquare()) {
            throw new UndefinedOperationException(
                "Inverse is only defined for square matrices, got: " + rows + "x" + cols
            );
        }
        int n = rows;
        double[][] a = toArray2D();
        double[][] inv = new double[n][n];
        for (int i = 0; i < n; i++) {
            inv[i][i] = 1.0;
        }
        double threshold = singularityThreshold(a);

        for (int col = 0; col < n; col++) {
            int pivotRow = col;
            double maxAbs = Math.abs(a[col][col]);
            for (int r = col + 1; r < n; r++) {
                double candidate = Math.abs(a[r][col]);
                if (candidate > maxAbs) {
                    maxAbs = candidate;
                    pivotRow = r;
                }
            }
            if (maxAbs <= threshold) {
                throw new NonInvertibleElementException(
                    "Float64Matrix is numerically singular at column " + col + " (|pivot| = " + maxAbs + ")"
                );
            }
            if (pivotRow != col) {
                double[] tmpA = a[col];
                a[col] = a[pivotRow];
                a[pivotRow] = tmpA;

                double[] tmpInv = inv[col];
                inv[col] = inv[pivotRow];
                inv[pivotRow] = tmpInv;
            }

            double pivot = a[col][col];
            for (int c = 0; c < n; c++) {
                a[col][c] /= pivot;
                inv[col][c] /= pivot;
            }
            for (int r = 0; r < n; r++) {
                if (r == col) {
                    continue;
                }
                double factor = a[r][col];
                if (factor != 0.0) {
                    for (int c = 0; c < n; c++) {
                        a[r][c] -= factor * a[col][c];
                        inv[r][c] -= factor * inv[col][c];
                    }
                }
            }
        }
        return Float64Matrix.of(inv);
    }

    /**
     * Returns {@code true} if {@code other} has the same dimensions and every corresponding entry differs by at most {@code epsilon}.
     *
     * @param other   an open matrix to compare against
     * @param epsilon the non-negative finite entrywise tolerance
     * @return {@code true} if all entries match within {@code epsilon}
     */
    public boolean approxEquals(Float64Matrix other, double epsilon) {
        Objects.requireNonNull(other, "other cannot be null");
        if (Double.isNaN(epsilon) || Double.isInfinite(epsilon) || epsilon < 0.0) {
            throw new IllegalArgumentException("epsilon must be finite and non-negative, got: " + epsilon);
        }
        ensureOpen();
        other.ensureOpen();
        if (this.rows != other.rows || this.cols != other.cols) {
            return false;
        }
        long total = (long) rows * cols;
        for (long idx = 0; idx < total; idx++) {
            double diff = Math.abs(
                this.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, idx)
                    - other.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, idx)
            );
            if (!(diff <= epsilon)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if {@code other} has the same dimensions and every corresponding entry differs by at most {@link NumericContext#currentEpsilon()}.
     *
     * @param other an open matrix to compare against
     * @return {@code true} if approximately equal within the active tolerance
     */
    public boolean approxEquals(Float64Matrix other) {
        return approxEquals(other, NumericContext.currentEpsilon());
    }

    @Override
    public void close() {
        if (ownsArena && arena.scope().isAlive()) {
            arena.close();
        }
    }
}
