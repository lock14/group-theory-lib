package lock14.algebra.numeric;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.NonInvertibleElementException;

/**
 * Shared off-heap Foreign Function and Memory storage and numerical kernels for {@link Float64Matrix},
 * {@link Float64SquareMatrix}, and {@link Float64RectangularMatrix}.
 */
final class Float64MatrixOps {

    static final long DOUBLE_BYTE_SIZE = ValueLayout.JAVA_DOUBLE.byteSize();

    private Float64MatrixOps() {}

    record OffHeapStorage(int rows, int cols, Arena arena, boolean ownsArena, MemorySegment segment) {

        boolean isOpen() {
            return segment.scope().isAlive();
        }

        void ensureOpen() {
            if (!segment.scope().isAlive()) {
                throw new IllegalStateException("Float64Matrix arena has already been closed");
            }
        }

        long index(int row, int col) {
            Objects.checkIndex(row, rows);
            Objects.checkIndex(col, cols);
            return (long) row * cols + col;
        }

        double get(int row, int col) {
            ensureOpen();
            return segment.getAtIndex(ValueLayout.JAVA_DOUBLE, index(row, col));
        }

        void set(int row, int col, double value) {
            ensureOpen();
            segment.setAtIndex(ValueLayout.JAVA_DOUBLE, index(row, col), value == 0.0 ? 0.0 : value);
        }

        double[][] toArray2D() {
            ensureOpen();
            double[][] out = new double[rows][cols];
            for (int i = 0; i < rows; i++) {
                long rowOffset = (long) i * cols * DOUBLE_BYTE_SIZE;
                MemorySegment.copy(segment, ValueLayout.JAVA_DOUBLE, rowOffset, out[i], 0, cols);
            }
            return out;
        }

        void close() {
            if (ownsArena && arena.scope().isAlive()) {
                arena.close();
            }
        }
    }

    static OffHeapStorage allocateOwned(int rows, int cols) {
        validateDimensions(rows, cols);
        long byteSize = computeByteSize(rows, cols);
        Arena owned = Arena.ofConfined();
        try {
            MemorySegment segment = owned.allocate(byteSize, ValueLayout.JAVA_DOUBLE.byteAlignment());
            return new OffHeapStorage(rows, cols, owned, true, segment);
        } catch (Throwable t) {
            owned.close();
            throw t;
        }
    }

    static OffHeapStorage allocateExternal(int rows, int cols, Arena externalArena) {
        Objects.requireNonNull(externalArena, "externalArena cannot be null");
        validateDimensions(rows, cols);
        long byteSize = computeByteSize(rows, cols);
        MemorySegment segment = externalArena.allocate(byteSize, ValueLayout.JAVA_DOUBLE.byteAlignment());
        return new OffHeapStorage(rows, cols, externalArena, false, segment);
    }

    static OffHeapStorage fromArray2D(double[][] values) {
        validateArray2D(values);
        int r = values.length;
        int c = values[0].length;
        OffHeapStorage storage = allocateOwned(r, c);
        try {
            for (int i = 0; i < r; i++) {
                long rowOffset = (long) i * c * DOUBLE_BYTE_SIZE;
                MemorySegment.copy(values[i], 0, storage.segment(), ValueLayout.JAVA_DOUBLE, rowOffset, c);
                for (int j = 0; j < c; j++) {
                    if (values[i][j] == 0.0) {
                        storage.segment().setAtIndex(ValueLayout.JAVA_DOUBLE, (long) i * c + j, 0.0);
                    }
                }
            }
            return storage;
        } catch (Throwable t) {
            storage.close();
            throw t;
        }
    }

    static OffHeapStorage fromRowMajor(int rows, int cols, double... rowMajorValues) {
        Objects.requireNonNull(rowMajorValues, "rowMajorValues cannot be null");
        validateDimensions(rows, cols);
        long expected = Math.multiplyExact((long) rows, (long) cols);
        if (rowMajorValues.length != expected) {
            throw new DimensionMismatchException(
                "Expected " + expected + " values for " + rows + "x" + cols + " matrix, got " + rowMajorValues.length
            );
        }
        OffHeapStorage storage = allocateOwned(rows, cols);
        try {
            MemorySegment.copy(
                rowMajorValues, 0, storage.segment(), ValueLayout.JAVA_DOUBLE, 0L, rowMajorValues.length
            );
            for (int idx = 0; idx < rowMajorValues.length; idx++) {
                if (rowMajorValues[idx] == 0.0) {
                    storage.segment().setAtIndex(ValueLayout.JAVA_DOUBLE, idx, 0.0);
                }
            }
            return storage;
        } catch (Throwable t) {
            storage.close();
            throw t;
        }
    }

    static void validateArray2D(double[][] values) {
        Objects.requireNonNull(values, "values cannot be null");
        if (values.length == 0 || values[0] == null || values[0].length == 0) {
            throw new IllegalArgumentException("Matrix values must be non-empty");
        }
        int c = values[0].length;
        for (double[] row : values) {
            if (row == null || row.length != c) {
                throw new DimensionMismatchException("Jagged array rows are not permitted");
            }
        }
    }

    static void validateDimensions(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive, got: " + rows + "x" + cols);
        }
    }

    static OffHeapStorage storageOf(Float64Matrix matrix) {
        Objects.requireNonNull(matrix, "other cannot be null");
        return switch (matrix) {
            case Float64SquareMatrix sq -> sq.storage();
            case Float64RectangularMatrix rect -> rect.storage();
        };
    }

    static Float64Matrix wrap(OffHeapStorage storage) {
        return storage.rows() == storage.cols()
            ? new Float64SquareMatrix(storage)
            : new Float64RectangularMatrix(storage);
    }

    static OffHeapStorage add(OffHeapStorage a, OffHeapStorage b) {
        a.ensureOpen();
        b.ensureOpen();
        if (a.rows() != b.rows() || a.cols() != b.cols()) {
            throw new DimensionMismatchException(
                "Cannot add " + a.rows() + "x" + a.cols() + " and " + b.rows() + "x" + b.cols() + " matrices"
            );
        }
        OffHeapStorage result = allocateOwned(a.rows(), a.cols());
        try {
            long total = (long) a.rows() * a.cols();
            for (long idx = 0; idx < total; idx++) {
                double sum = a.segment().getAtIndex(ValueLayout.JAVA_DOUBLE, idx)
                    + b.segment().getAtIndex(ValueLayout.JAVA_DOUBLE, idx);
                result.segment().setAtIndex(ValueLayout.JAVA_DOUBLE, idx, sum == 0.0 ? 0.0 : sum);
            }
            return result;
        } catch (Throwable t) {
            result.close();
            throw t;
        }
    }

    static OffHeapStorage subtract(OffHeapStorage a, OffHeapStorage b) {
        a.ensureOpen();
        b.ensureOpen();
        if (a.rows() != b.rows() || a.cols() != b.cols()) {
            throw new DimensionMismatchException(
                "Cannot subtract " + a.rows() + "x" + a.cols() + " and " + b.rows() + "x" + b.cols() + " matrices"
            );
        }
        OffHeapStorage result = allocateOwned(a.rows(), a.cols());
        try {
            long total = (long) a.rows() * a.cols();
            for (long idx = 0; idx < total; idx++) {
                double diff = a.segment().getAtIndex(ValueLayout.JAVA_DOUBLE, idx)
                    - b.segment().getAtIndex(ValueLayout.JAVA_DOUBLE, idx);
                result.segment().setAtIndex(ValueLayout.JAVA_DOUBLE, idx, diff == 0.0 ? 0.0 : diff);
            }
            return result;
        } catch (Throwable t) {
            result.close();
            throw t;
        }
    }

    static OffHeapStorage scale(OffHeapStorage a, double factor) {
        a.ensureOpen();
        OffHeapStorage result = allocateOwned(a.rows(), a.cols());
        try {
            long total = (long) a.rows() * a.cols();
            for (long idx = 0; idx < total; idx++) {
                double prod = a.segment().getAtIndex(ValueLayout.JAVA_DOUBLE, idx) * factor;
                result.segment().setAtIndex(ValueLayout.JAVA_DOUBLE, idx, prod == 0.0 ? 0.0 : prod);
            }
            return result;
        } catch (Throwable t) {
            result.close();
            throw t;
        }
    }

    static OffHeapStorage multiply(OffHeapStorage a, OffHeapStorage b) {
        a.ensureOpen();
        b.ensureOpen();
        if (a.cols() != b.rows()) {
            throw new DimensionMismatchException(
                "Cannot multiply " + a.rows() + "x" + a.cols() + " by " + b.rows() + "x" + b.cols()
            );
        }
        OffHeapStorage result = allocateOwned(a.rows(), b.cols());
        try {
            int m = a.rows();
            int p = a.cols();
            int n = b.cols();
            for (int i = 0; i < m; i++) {
                long aRow = (long) i * p;
                long cRow = (long) i * n;
                for (int k = 0; k < p; k++) {
                    double aik = a.segment().getAtIndex(ValueLayout.JAVA_DOUBLE, aRow + k);
                    if (aik == 0.0) {
                        continue;
                    }
                    long bRow = (long) k * n;
                    for (int j = 0; j < n; j++) {
                        double cij = result.segment().getAtIndex(ValueLayout.JAVA_DOUBLE, cRow + j)
                            + aik * b.segment().getAtIndex(ValueLayout.JAVA_DOUBLE, bRow + j);
                        result.segment().setAtIndex(ValueLayout.JAVA_DOUBLE, cRow + j, cij == 0.0 ? 0.0 : cij);
                    }
                }
            }
            return result;
        } catch (Throwable t) {
            result.close();
            throw t;
        }
    }

    static double[] multiplyVector(OffHeapStorage a, double[] vector) {
        Objects.requireNonNull(vector, "vector cannot be null");
        a.ensureOpen();
        if (vector.length != a.cols()) {
            throw new DimensionMismatchException(
                "Cannot multiply " + a.rows() + "x" + a.cols() + " matrix by vector of length " + vector.length
            );
        }
        double[] out = new double[a.rows()];
        for (int i = 0; i < a.rows(); i++) {
            double sum = 0.0;
            long rowBase = (long) i * a.cols();
            for (int j = 0; j < a.cols(); j++) {
                sum += a.segment().getAtIndex(ValueLayout.JAVA_DOUBLE, rowBase + j) * vector[j];
            }
            out[i] = sum == 0.0 ? 0.0 : sum;
        }
        return out;
    }

    static OffHeapStorage transpose(OffHeapStorage a) {
        a.ensureOpen();
        OffHeapStorage result = allocateOwned(a.cols(), a.rows());
        try {
            for (int i = 0; i < a.rows(); i++) {
                long rowBase = (long) i * a.cols();
                for (int j = 0; j < a.cols(); j++) {
                    double val = a.segment().getAtIndex(ValueLayout.JAVA_DOUBLE, rowBase + j);
                    result.segment().setAtIndex(ValueLayout.JAVA_DOUBLE, (long) j * a.rows() + i, val);
                }
            }
            return result;
        } catch (Throwable t) {
            result.close();
            throw t;
        }
    }

    static boolean approxEquals(OffHeapStorage a, OffHeapStorage b, double epsilon) {
        if (Double.isNaN(epsilon) || Double.isInfinite(epsilon) || epsilon < 0.0) {
            throw new IllegalArgumentException("epsilon must be finite and non-negative, got: " + epsilon);
        }
        a.ensureOpen();
        b.ensureOpen();
        if (a.rows() != b.rows() || a.cols() != b.cols()) {
            return false;
        }
        long total = (long) a.rows() * a.cols();
        for (long idx = 0; idx < total; idx++) {
            double diff = Math.abs(
                a.segment().getAtIndex(ValueLayout.JAVA_DOUBLE, idx)
                    - b.segment().getAtIndex(ValueLayout.JAVA_DOUBLE, idx)
            );
            if (!(diff <= epsilon)) {
                return false;
            }
        }
        return true;
    }

    static double trace(OffHeapStorage sq) {
        sq.ensureOpen();
        int n = sq.rows();
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += sq.segment().getAtIndex(ValueLayout.JAVA_DOUBLE, (long) i * n + i);
        }
        return sum == 0.0 ? 0.0 : sum;
    }

    static double determinant(OffHeapStorage sq) {
        double[][] a = sq.toArray2D();
        double threshold = singularityThreshold(a);
        int n = sq.rows();
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

    static double[] solve(OffHeapStorage sq, double[] b) {
        Objects.requireNonNull(b, "b cannot be null");
        sq.ensureOpen();
        int n = sq.rows();
        if (b.length != n) {
            throw new DimensionMismatchException(
                "Right-hand side length " + b.length + " does not match matrix dimension " + n
            );
        }
        double[][] a = sq.toArray2D();
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
                    "Float64SquareMatrix is numerically singular at column " + col + " (|pivot| = " + maxAbs + ")"
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

    static OffHeapStorage inverse(OffHeapStorage sq) {
        double[][] a = sq.toArray2D();
        int n = sq.rows();
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
                    "Float64SquareMatrix is numerically singular at column " + col + " (|pivot| = " + maxAbs + ")"
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
        return fromArray2D(inv);
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

    private static double singularityThreshold(double[][] work) {
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
}
