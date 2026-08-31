package lock14.algebra.linear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Gatherers;
import java.util.stream.Stream;
import lock14.algebra.element.RingElement;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.structure.Ring;

/**
 * An immutable rectangular Dense Matrix over a {@link Ring} R.
 * Backed by a contiguous 1D flat row-major array for optimal cache locality.
 *
 * @param <T> the entry type in Ring R
 */
public class DenseMatrix<T> {

    protected final int rows;
    protected final int cols;
    protected final Ring<T> ring;
    protected final Object[] data;

    @SuppressWarnings("unchecked")
    public DenseMatrix(int rows, int cols, Ring<T> ring, T[] elements) {
        if (rows < 0 || cols < 0) {
            throw new IllegalArgumentException("Matrix dimensions must be non-negative: " + rows + "x" + cols);
        }
        Objects.requireNonNull(ring, "Ring cannot be null");
        Objects.requireNonNull(elements, "Elements cannot be null");
        if (elements.length != rows * cols) {
            throw new DimensionMismatchException(
                "Element array length (" + elements.length + ") does not match matrix dimensions (" + rows + "x" + cols + ")"
            );
        }
        this.rows = rows;
        this.cols = cols;
        this.ring = ring;
        this.data = new Object[elements.length];
        for (int i = 0; i < elements.length; i++) {
            this.data[i] = Objects.requireNonNull(elements[i], "Matrix element cannot be null at index " + i);
        }
    }

    protected DenseMatrix(int rows, int cols, Ring<T> ring, Object[] data, boolean trusted) {
        this.rows = rows;
        this.cols = cols;
        this.ring = ring;
        this.data = data;
    }

    public static <T> DenseMatrix<T> zeros(int rows, int cols, Ring<T> ring) {
        if (rows < 0 || cols < 0) throw new IllegalArgumentException("Dimensions must be non-negative");
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[rows * cols];
        Arrays.fill(arr, ring.zero());
        return new DenseMatrix<>(rows, cols, ring, arr, true);
    }

    public static <T> DenseMatrix<T> identity(int n, Ring<T> ring) {
        if (n <= 0) throw new IllegalArgumentException("Identity dimension must be positive: " + n);
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[n * n];
        Arrays.fill(arr, ring.zero());
        for (int i = 0; i < n; i++) {
            arr[i * n + i] = ring.one();
        }
        return new DenseMatrix<>(n, n, ring, arr, true);
    }

    public static <T> DenseMatrix<T> of(Ring<T> ring, T[][] matrix2D) {
        Objects.requireNonNull(matrix2D, "matrix2D cannot be null");
        int r = matrix2D.length;
        int c = r == 0 ? 0 : matrix2D[0].length;
        Object[] flat = new Object[r * c];
        for (int i = 0; i < r; i++) {
            if (matrix2D[i].length != c) {
                throw new IllegalArgumentException("Ragged 2D array: row " + i + " has length " + matrix2D[i].length + " expected " + c);
            }
            for (int j = 0; j < c; j++) {
                flat[i * c + j] = Objects.requireNonNull(matrix2D[i][j], "Element cannot be null at (" + i + ", " + j + ")");
            }
        }
        return new DenseMatrix<>(r, c, ring, flat, true);
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public Ring<T> ring() {
        return ring;
    }

    public boolean isSquare() {
        return rows == cols;
    }

    @SuppressWarnings("unchecked")
    public T get(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) {
            throw new IndexOutOfBoundsException("Matrix index (" + r + ", " + c + ") out of bounds for " + rows + "x" + cols);
        }
        return (T) data[r * cols + c];
    }

    public DenseMatrix<T> with(int r, int c, T val) {
        Objects.requireNonNull(val, "val cannot be null");
        get(r, c); // Bounds check
        Object[] copy = data.clone();
        copy[r * cols + c] = val;
        return new DenseMatrix<>(rows, cols, ring, copy, true);
    }

    public DenseMatrix<T> add(DenseMatrix<T> other) {
        checkSameShape(other);
        Object[] res = new Object[data.length];
        for (int i = 0; i < data.length; i++) {
            res[i] = ring.add(getDirect(i), other.getDirect(i));
        }
        return new DenseMatrix<>(rows, cols, ring, res, true);
    }

    public DenseMatrix<T> negate() {
        Object[] res = new Object[data.length];
        for (int i = 0; i < data.length; i++) {
            res[i] = ring.negate(getDirect(i));
        }
        return new DenseMatrix<>(rows, cols, ring, res, true);
    }

    public DenseMatrix<T> subtract(DenseMatrix<T> other) {
        checkSameShape(other);
        Object[] res = new Object[data.length];
        for (int i = 0; i < data.length; i++) {
            res[i] = ring.subtract(getDirect(i), other.getDirect(i));
        }
        return new DenseMatrix<>(rows, cols, ring, res, true);
    }

    public DenseMatrix<T> multiply(DenseMatrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        if (this.cols != other.rows) {
            throw new DimensionMismatchException(
                "Cannot multiply matrix " + rows + "x" + cols + " with " + other.rows + "x" + other.cols
            );
        }
        int resRows = this.rows;
        int resCols = other.cols;
        int inner = this.cols;
        Object[] result = new Object[resRows * resCols];

        // Cache-friendly i-k-j loop order
        for (int i = 0; i < resRows; i++) {
            int outRowOffset = i * resCols;
            int thisRowOffset = i * inner;
            for (int k = 0; k < inner; k++) {
                T aVal = getDirect(thisRowOffset + k);
                if (ring.isZero(aVal)) continue;
                int otherRowOffset = k * resCols;
                for (int j = 0; j < resCols; j++) {
                    T bVal = other.getDirect(otherRowOffset + j);
                    if (ring.isZero(bVal)) continue;
                    T prod = ring.multiply(aVal, bVal);
                    int idx = outRowOffset + j;
                    result[idx] = result[idx] == null ? prod : ring.add((T) result[idx], prod);
                }
            }
        }
        T zero = ring.zero();
        for (int idx = 0; idx < result.length; idx++) {
            if (result[idx] == null) result[idx] = zero;
        }
        return new DenseMatrix<>(resRows, resCols, ring, result, true);
    }

    public DenseMatrix<T> scale(T scalar) {
        Objects.requireNonNull(scalar, "scalar cannot be null");
        Object[] res = new Object[data.length];
        for (int i = 0; i < data.length; i++) {
            res[i] = ring.multiply(scalar, getDirect(i));
        }
        return new DenseMatrix<>(rows, cols, ring, res, true);
    }

    public DenseMatrix<T> transpose() {
        Object[] res = new Object[rows * cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                res[c * rows + r] = data[r * cols + c];
            }
        }
        return new DenseMatrix<>(cols, rows, ring, res, true);
    }

    public T trace() {
        if (!isSquare()) {
            throw new IllegalArgumentException("Trace is only defined for square matrices, but got " + rows + "x" + cols);
        }
        T sum = ring.zero();
        for (int i = 0; i < rows; i++) {
            sum = ring.add(sum, get(i, i));
        }
        return sum;
    }

    public List<T> row(int r) {
        if (r < 0 || r >= rows) throw new IndexOutOfBoundsException("Row " + r + " out of bounds");
        List<T> res = new ArrayList<>(cols);
        for (int c = 0; c < cols; c++) {
            res.add(get(r, c));
        }
        return res;
    }

    public List<T> col(int c) {
        if (c < 0 || c >= cols) throw new IndexOutOfBoundsException("Col " + c + " out of bounds");
        List<T> res = new ArrayList<>(rows);
        for (int r = 0; r < rows; r++) {
            res.add(get(r, c));
        }
        return res;
    }

    /**
     * Java 25 Stream Gatherer to stream rows of the matrix without pre-allocating full row objects.
     */
    @SuppressWarnings("unchecked")
    public Stream<List<T>> streamRows() {
        if (cols == 0) return Stream.empty();
        return Arrays.stream((T[]) data).gather(Gatherers.windowFixed(cols));
    }

    @SuppressWarnings("unchecked")
    protected T getDirect(int idx) {
        return (T) data[idx];
    }

    private void checkSameShape(DenseMatrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        if (this.rows != other.rows || this.cols != other.cols) {
            throw new DimensionMismatchException(this.rows, this.cols, other.rows, other.cols);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DenseMatrix<?> other
            && rows == other.rows
            && cols == other.cols
            && Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(rows, cols) + Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(rows).append("x").append(cols).append(" Matrix]\n");
        for (int i = 0; i < rows; i++) {
            sb.append("  [");
            for (int j = 0; j < cols; j++) {
                if (j > 0) sb.append(", ");
                sb.append(get(i, j));
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}
