package lock14.algebra.linear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Gatherers;
import java.util.stream.Stream;
import lock14.algebra.element.ModuleElement;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.structure.Ring;

/**
 * An immutable strictly rectangular ({@code rows != cols}) Dense Matrix over a {@link Ring} R.
 * Backed by a contiguous 1D flat row-major array for optimal cache locality.
 * For square matrices ({@code rows == cols}), use {@link SquareMatrix} or {@link Matrix#of}.
 *
 * @param <T> the entry type in Ring R
 */
public record DenseMatrix<T>(int rows, int cols, Ring<T> ring, Object[] data)
    implements Matrix<T>, ModuleElement<DenseMatrix<T>, T> {

    public DenseMatrix {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive: " + rows + "x" + cols);
        }
        if (rows == cols) {
            throw new IllegalArgumentException(
                "DenseMatrix requires rectangular dimensions (rows != cols), got " + rows + "x" + cols
                + "; use SquareMatrix or Matrix.of(...) for square matrices"
            );
        }
        Objects.requireNonNull(ring, "Ring cannot be null");
        Objects.requireNonNull(data, "Data cannot be null");
        int expectedLength;
        try {
            expectedLength = Math.multiplyExact(rows, cols);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Matrix dimensions overflow array capacity: " + rows + "x" + cols, ex);
        }
        if (data.length != expectedLength) {
            throw new DimensionMismatchException(
                "Element array length (" + data.length + ") does not match matrix dimensions (" + rows + "x" + cols + ")"
            );
        }
        Object[] copy = new Object[data.length];
        for (int i = 0; i < data.length; i++) {
            copy[i] = Objects.requireNonNull(data[i], "Matrix element cannot be null at index " + i);
        }
        data = copy;
    }

    @SafeVarargs
    public static <T> DenseMatrix<T> of(int rows, int cols, Ring<T> ring, T... elements) {
        Objects.requireNonNull(elements, "Elements cannot be null");
        return new DenseMatrix<>(rows, cols, ring, elements);
    }

    public static <T> DenseMatrix<T> zeros(int rows, int cols, Ring<T> ring) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive: " + rows + "x" + cols);
        }
        if (rows == cols) {
            throw new IllegalArgumentException(
                "DenseMatrix requires rectangular dimensions (rows != cols); use SquareMatrix.zeros or Matrix.zeros"
            );
        }
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[Math.multiplyExact(rows, cols)];
        Arrays.fill(arr, ring.zero());
        return new DenseMatrix<>(rows, cols, ring, arr);
    }

    public static <T> DenseMatrix<T> of(Ring<T> ring, T[][] matrix2D) {
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] flat = MatrixOps.flatten2D(matrix2D);
        int r = matrix2D.length;
        int c = matrix2D[0].length;
        return new DenseMatrix<>(r, c, ring, flat);
    }

    @Override
    public Object[] data() {
        return data.clone();
    }

    Object[] rawData() {
        return data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) {
            throw new IndexOutOfBoundsException("Matrix index (" + r + ", " + c + ") out of bounds for " + rows + "x" + cols);
        }
        return (T) data[r * cols + c];
    }

    @Override
    public DenseMatrix<T> with(int r, int c, T val) {
        Objects.requireNonNull(val, "val cannot be null");
        get(r, c); // Bounds check
        Object[] copy = data.clone();
        copy[r * cols + c] = val;
        return new DenseMatrix<>(rows, cols, ring, copy);
    }

    @Override
    public DenseMatrix<T> add(DenseMatrix<T> other) {
        checkSameShape(other);
        checkSameRing(other.ring);
        return new DenseMatrix<>(rows, cols, ring, MatrixOps.add(ring, data, other.data));
    }

    @Override
    public DenseMatrix<T> add(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return switch (other) {
            case DenseMatrix<T> dm -> add(dm);
            case SquareMatrix<T> sm -> {
                checkSameRing(sm.ring());
                throw new DimensionMismatchException(this.rows, this.cols, sm.dimension(), sm.dimension());
            }
        };
    }

    @Override
    public DenseMatrix<T> negate() {
        return new DenseMatrix<>(rows, cols, ring, MatrixOps.negate(ring, data));
    }

    @Override
    public DenseMatrix<T> subtract(DenseMatrix<T> other) {
        checkSameShape(other);
        checkSameRing(other.ring);
        return new DenseMatrix<>(rows, cols, ring, MatrixOps.subtract(ring, data, other.data));
    }

    @Override
    public DenseMatrix<T> subtract(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return switch (other) {
            case DenseMatrix<T> dm -> subtract(dm);
            case SquareMatrix<T> sm -> {
                checkSameRing(sm.ring());
                throw new DimensionMismatchException(this.rows, this.cols, sm.dimension(), sm.dimension());
            }
        };
    }

    /**
     * Multiplies this rectangular {@code (rows x cols)} matrix with another rectangular matrix.
     * Returns a {@link SquareMatrix} when {@code this.rows == other.cols}, or a {@link DenseMatrix} otherwise.
     */
    public Matrix<T> multiply(DenseMatrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        checkSameRing(other.ring);
        if (this.cols != other.rows) {
            throw new DimensionMismatchException(
                "Cannot multiply matrix " + rows + "x" + cols + " with " + other.rows + "x" + other.cols
            );
        }
        Object[] res = MatrixOps.multiply(ring, data, other.data, this.rows, this.cols, other.cols);
        return this.rows == other.cols
            ? new SquareMatrix<>(this.rows, ring, res)
            : new DenseMatrix<>(this.rows, other.cols, ring, res);
    }

    /**
     * Multiplies this rectangular {@code (rows x n)} matrix with an {@code (n x n)} {@link SquareMatrix},
     * yielding a rectangular {@code (rows x n)} {@link DenseMatrix} ({@code rows != n}).
     */
    public DenseMatrix<T> multiply(SquareMatrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        checkSameRing(other.ring());
        if (this.cols != other.dimension()) {
            throw new DimensionMismatchException(
                "Cannot multiply matrix " + rows + "x" + cols + " with " + other.dimension() + "x" + other.dimension()
            );
        }
        Object[] res = MatrixOps.multiply(ring, data, other.rawData(), this.rows, this.cols, other.dimension());
        return new DenseMatrix<>(this.rows, other.dimension(), ring, res);
    }

    @Override
    public Matrix<T> multiply(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return switch (other) {
            case DenseMatrix<T> dm -> multiply(dm);
            case SquareMatrix<T> sm -> multiply(sm);
        };
    }

    @Override
    public Vector<T> multiply(Vector<T> vector) {
        Objects.requireNonNull(vector, "vector cannot be null");
        checkSameRing(vector.ring());
        if (this.cols != vector.dimension()) {
            throw new DimensionMismatchException(
                "Cannot multiply matrix " + rows + "x" + cols + " with vector of dimension " + vector.dimension()
            );
        }
        return new Vector<>(this.rows, MatrixOps.multiplyColumnVector(ring, data, vector.rawData(), this.rows, this.cols), ring);
    }

    @Override
    public DenseMatrix<T> scale(T scalar) {
        Objects.requireNonNull(scalar, "scalar cannot be null");
        return new DenseMatrix<>(rows, cols, ring, MatrixOps.scale(ring, data, scalar));
    }

    @Override
    public DenseMatrix<T> transpose() {
        return new DenseMatrix<>(cols, rows, ring, MatrixOps.transpose(data, rows, cols));
    }

    @Override
    public List<T> row(int r) {
        if (r < 0 || r >= rows) throw new IndexOutOfBoundsException("Row " + r + " out of bounds");
        List<T> res = new ArrayList<>(cols);
        for (int c = 0; c < cols; c++) {
            res.add(get(r, c));
        }
        return List.copyOf(res);
    }

    @Override
    public List<T> col(int c) {
        if (c < 0 || c >= cols) throw new IndexOutOfBoundsException("Col " + c + " out of bounds");
        List<T> res = new ArrayList<>(rows);
        for (int r = 0; r < rows; r++) {
            res.add(get(r, c));
        }
        return List.copyOf(res);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<List<T>> streamRows() {
        return Arrays.stream((T[]) data).gather(Gatherers.windowFixed(cols));
    }

    @Override
    public SquareMatrix<T> asSquareMatrix() {
        throw new IllegalArgumentException("Cannot convert rectangular matrix (" + rows + "x" + cols + ") to SquareMatrix");
    }

    private void checkSameShape(DenseMatrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        if (this.rows != other.rows || this.cols != other.cols) {
            throw new DimensionMismatchException(this.rows, this.cols, other.rows, other.cols);
        }
    }

    private void checkSameRing(Ring<T> otherRing) {
        if (!Objects.equals(this.ring, otherRing)) {
            throw new IllegalArgumentException("Cannot operate on matrices/vectors over different rings");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DenseMatrix<?> other
            && rows == other.rows
            && cols == other.cols
            && Objects.equals(ring, other.ring)
            && Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(rows, cols, ring) + Arrays.hashCode(data);
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
