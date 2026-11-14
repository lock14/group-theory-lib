package lock14.algebra.linear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Gatherers;
import java.util.stream.Stream;
import lock14.algebra.element.ModuleElement;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.Ring;

/**
 * An immutable strictly non-square ({@code rows != cols}) Dense Matrix over an exact {@link Ring} {@code R},
 * backed by an encapsulated flat row-major array with zero-copy internal arithmetic.
 *
 * @param <T> scalar ring entry type
 */
public final class RectangularMatrix<T> implements Matrix<T>, ModuleElement<RectangularMatrix<T>, T> {

    private final int rows;
    private final int cols;
    private final Ring<T> ring;
    private final Object[] data;

    RectangularMatrix(int rows, int cols, Ring<T> ring, Object[] trustedData) {
        this.rows = rows;
        this.cols = cols;
        this.ring = ring;
        this.data = trustedData;
    }

    private static <T> RectangularMatrix<T> validated(int rows, int cols, Ring<T> ring, Object[] rawData, boolean cloneArray) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive: " + rows + "x" + cols);
        }
        if (rows == cols) {
            throw new IllegalArgumentException(
                "RectangularMatrix requires rows != cols, got " + rows + "x" + cols + "; use SquareMatrix or Matrix.of"
            );
        }
        Objects.requireNonNull(ring, "ring cannot be null");
        Objects.requireNonNull(rawData, "elements cannot be null");
        int expectedLength;
        try {
            expectedLength = Math.multiplyExact(rows, cols);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Matrix dimensions overflow array capacity: " + rows + "x" + cols, ex);
        }
        if (rawData.length != expectedLength) {
            throw new DimensionMismatchException(
                "Element count (" + rawData.length + ") does not match dimensions (" + rows + "x" + cols + ")"
            );
        }
        Object[] target = cloneArray ? new Object[expectedLength] : rawData;
        for (int i = 0; i < expectedLength; i++) {
            Object elem = Objects.requireNonNull(rawData[i], "Matrix element cannot be null at index " + i);
            if (cloneArray) {
                target[i] = elem;
            }
        }
        return new RectangularMatrix<>(rows, cols, ring, target);
    }

    /**
     * Constructs a strictly non-square ({@code rows != cols}) matrix over {@code ring} from row-major elements.
     *
     * @param <T>      scalar ring entry type
     * @param rows     the positive row count
     * @param cols     the positive column count ({@code cols != rows})
     * @param ring     the scalar entry ring
     * @param elements the {@code rows * cols} entries in row-major order
     * @return an immutable {@code rows x cols} {@link RectangularMatrix}
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> RectangularMatrix<T> of(int rows, int cols, Ring<T> ring, T... elements) {
        return validated(rows, cols, ring, elements, true);
    }

    /**
     * Constructs a strictly non-square matrix over {@code ring} from a 2D array.
     *
     * @param <T>      scalar ring entry type
     * @param ring     the scalar entry ring
     * @param matrix2D non-empty non-square rectangular 2D array of entries
     * @return an immutable {@link RectangularMatrix}
     */
    public static <T> RectangularMatrix<T> of(Ring<T> ring, T[][] matrix2D) {
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] flat = MatrixOps.flatten2D(matrix2D);
        return validated(matrix2D.length, matrix2D[0].length, ring, flat, false);
    }

    /**
     * Constructs the strictly non-square ({@code rows != cols}) zero matrix over {@code ring}.
     *
     * @param <T>  scalar ring entry type
     * @param rows the positive row count
     * @param cols the positive column count ({@code cols != rows})
     * @param ring the scalar entry ring
     * @return the {@code rows x cols} zero {@link RectangularMatrix}
     */
    public static <T> RectangularMatrix<T> zeros(int rows, int cols, Ring<T> ring) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive: " + rows + "x" + cols);
        }
        if (rows == cols) {
            throw new IllegalArgumentException(
                "RectangularMatrix requires rows != cols; use SquareMatrix.zeros or Matrix.zeros"
            );
        }
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[Math.multiplyExact(rows, cols)];
        Arrays.fill(arr, ring.zero());
        return new RectangularMatrix<>(rows, cols, ring, arr);
    }

    @Override
    public int rows() {
        return rows;
    }

    @Override
    public int cols() {
        return cols;
    }

    @Override
    public Ring<T> ring() {
        return ring;
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
    public RectangularMatrix<T> with(int r, int c, T val) {
        Objects.requireNonNull(val, "val cannot be null");
        get(r, c);
        Object[] copy = data.clone();
        copy[r * cols + c] = val;
        return new RectangularMatrix<>(rows, cols, ring, copy);
    }

    @Override
    public Matrix<T> submatrix(int r0, int r1, int c0, int c1) {
        return MatrixOps.submatrix(ring, data, rows, cols, r0, r1, c0, c1);
    }

    @Override
    public RectangularMatrix<T> add(RectangularMatrix<T> other) {
        checkSameShape(other);
        checkSameRing(other.ring);
        return new RectangularMatrix<>(rows, cols, ring, MatrixOps.add(ring, data, other.data));
    }

    @Override
    public RectangularMatrix<T> add(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return switch (other) {
            case RectangularMatrix<T> rm -> add(rm);
            case SquareMatrix<T> sm -> {
                checkSameRing(sm.ring());
                throw new DimensionMismatchException(this.rows, this.cols, sm.dimension(), sm.dimension());
            }
        };
    }

    @Override
    public RectangularMatrix<T> negate() {
        return new RectangularMatrix<>(rows, cols, ring, MatrixOps.negate(ring, data));
    }

    @Override
    public RectangularMatrix<T> subtract(RectangularMatrix<T> other) {
        checkSameShape(other);
        checkSameRing(other.ring);
        return new RectangularMatrix<>(rows, cols, ring, MatrixOps.subtract(ring, data, other.data));
    }

    @Override
    public RectangularMatrix<T> subtract(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return switch (other) {
            case RectangularMatrix<T> rm -> subtract(rm);
            case SquareMatrix<T> sm -> {
                checkSameRing(sm.ring());
                throw new DimensionMismatchException(this.rows, this.cols, sm.dimension(), sm.dimension());
            }
        };
    }

    /**
     * Multiplies this rectangular matrix by {@code other}, returning a {@link SquareMatrix} when
     * {@code this.rows() == other.cols()} or a {@link RectangularMatrix} otherwise.
     *
     * @param other a rectangular matrix over the same ring with {@code other.rows() == this.cols()}
     * @return the {@code this.rows() x other.cols()} product matrix
     */
    public Matrix<T> multiply(RectangularMatrix<T> other) {
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
            : new RectangularMatrix<>(this.rows, other.cols, ring, res);
    }

    /**
     * Multiplies this {@code m x n} rectangular matrix on the right by an {@code n x n} {@link SquareMatrix},
     * preserving the {@code m x n} rectangular shape.
     *
     * @param other an {@code n x n} square matrix over the same ring with {@code other.dimension() == this.cols()}
     * @return the {@code m x n} product {@link RectangularMatrix}
     */
    public RectangularMatrix<T> multiply(SquareMatrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        checkSameRing(other.ring());
        if (this.cols != other.dimension()) {
            throw new DimensionMismatchException(
                "Cannot multiply matrix " + rows + "x" + cols + " with " + other.dimension() + "x" + other.dimension()
            );
        }
        Object[] res = MatrixOps.multiply(ring, data, other.rawData(), this.rows, this.cols, other.dimension());
        return new RectangularMatrix<>(this.rows, other.dimension(), ring, res);
    }

    @Override
    public Matrix<T> multiply(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return switch (other) {
            case RectangularMatrix<T> rm -> multiply(rm);
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
        return new Vector<>(this.rows, ring, MatrixOps.multiplyColumnVector(ring, data, vector.rawData(), this.rows, this.cols));
    }

    @Override
    public RectangularMatrix<T> scale(T scalar) {
        Objects.requireNonNull(scalar, "scalar cannot be null");
        return new RectangularMatrix<>(rows, cols, ring, MatrixOps.scale(ring, data, scalar));
    }

    @Override
    public RectangularMatrix<T> transpose() {
        return new RectangularMatrix<>(cols, rows, ring, MatrixOps.transpose(data, rows, cols));
    }

    @Override
    public RrefResult<T> rref() {
        return MatrixOps.rref(ring, data, rows, cols);
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

    private void checkSameShape(RectangularMatrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        if (this.rows != other.rows || this.cols != other.cols) {
            throw new DimensionMismatchException(this.rows, this.cols, other.rows, other.cols);
        }
    }

    private void checkSameRing(Ring<T> otherRing) {
        if (!Objects.equals(this.ring, otherRing)) {
            throw new IncompatibleAlgebraException("Cannot operate on matrices/vectors over different rings");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RectangularMatrix<?> other
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
        sb.append('[').append(rows).append('x').append(cols).append(" RectangularMatrix]\n");
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
