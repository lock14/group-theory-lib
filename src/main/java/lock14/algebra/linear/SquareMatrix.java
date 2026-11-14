package lock14.algebra.linear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Gatherers;
import java.util.stream.Stream;
import lock14.algebra.element.ModuleElement;
import lock14.algebra.element.RingElement;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.structure.Ring;

/**
 * An immutable {@code n x n} Square Matrix over a {@link Ring} R.
 *
 * @param <T> the entry type in Ring R
 */
public record SquareMatrix<T>(int dimension, Ring<T> ring, Object[] data)
    implements Matrix<T>, RingElement<SquareMatrix<T>>, ModuleElement<SquareMatrix<T>, T> {

    public SquareMatrix {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Square matrix dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(ring, "Ring cannot be null");
        Objects.requireNonNull(data, "Data cannot be null");
        int expectedLength;
        try {
            expectedLength = Math.multiplyExact(dimension, dimension);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Matrix dimension overflows array capacity: " + dimension, ex);
        }
        if (data.length != expectedLength) {
            throw new DimensionMismatchException("Element count " + data.length + " does not match square dimension " + dimension + "x" + dimension);
        }
        Object[] copy = new Object[data.length];
        for (int i = 0; i < data.length; i++) {
            copy[i] = Objects.requireNonNull(data[i], "Matrix element cannot be null at index " + i);
        }
        data = copy;
    }

    @SafeVarargs
    public static <T> SquareMatrix<T> of(int n, Ring<T> ring, T... elements) {
        Objects.requireNonNull(elements, "Elements cannot be null");
        return new SquareMatrix<>(n, ring, elements);
    }

    public static <T> SquareMatrix<T> zeros(int n, Ring<T> ring) {
        if (n <= 0) throw new IllegalArgumentException("Dimension must be positive: " + n);
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[Math.multiplyExact(n, n)];
        Arrays.fill(arr, ring.zero());
        return new SquareMatrix<>(n, ring, arr);
    }

    public static <T> SquareMatrix<T> identity(int n, Ring<T> ring) {
        if (n <= 0) throw new IllegalArgumentException("Dimension must be positive: " + n);
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[Math.multiplyExact(n, n)];
        Arrays.fill(arr, ring.zero());
        for (int i = 0; i < n; i++) {
            arr[i * n + i] = ring.one();
        }
        return new SquareMatrix<>(n, ring, arr);
    }

    public static <T> SquareMatrix<T> of(Ring<T> ring, T[][] matrix2D) {
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] flat = MatrixOps.flatten2D(matrix2D);
        int n = matrix2D.length;
        int cols = matrix2D[0].length;
        if (cols != n) {
            throw new IllegalArgumentException("Provided 2D array is not square (" + n + "x" + cols + ")");
        }
        return new SquareMatrix<>(n, ring, flat);
    }

    @Override
    public Object[] data() {
        return data.clone();
    }

    Object[] rawData() {
        return data;
    }

    @Override
    public int rows() {
        return dimension;
    }

    @Override
    public int cols() {
        return dimension;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int r, int c) {
        if (r < 0 || r >= dimension || c < 0 || c >= dimension) {
            throw new IndexOutOfBoundsException("Matrix index (" + r + ", " + c + ") out of bounds for " + dimension + "x" + dimension);
        }
        return (T) data[r * dimension + c];
    }

    @Override
    public SquareMatrix<T> with(int r, int c, T val) {
        Objects.requireNonNull(val, "val cannot be null");
        get(r, c); // Bounds check
        Object[] copy = data.clone();
        copy[r * dimension + c] = val;
        return new SquareMatrix<>(dimension, ring, copy);
    }

    @Override
    public SquareMatrix<T> add(SquareMatrix<T> other) {
        checkSameDimension(other);
        checkSameRing(other.ring);
        return new SquareMatrix<>(dimension, ring, MatrixOps.add(ring, data, other.data));
    }

    @Override
    public SquareMatrix<T> add(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return switch (other) {
            case SquareMatrix<T> sm -> add(sm);
            case DenseMatrix<T> dm -> {
                checkSameRing(dm.ring());
                throw new DimensionMismatchException(this.dimension, this.dimension, dm.rows(), dm.cols());
            }
        };
    }

    @Override
    public SquareMatrix<T> negate() {
        return new SquareMatrix<>(dimension, ring, MatrixOps.negate(ring, data));
    }

    @Override
    public SquareMatrix<T> subtract(SquareMatrix<T> other) {
        checkSameDimension(other);
        checkSameRing(other.ring);
        return new SquareMatrix<>(dimension, ring, MatrixOps.subtract(ring, data, other.data));
    }

    @Override
    public SquareMatrix<T> subtract(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return switch (other) {
            case SquareMatrix<T> sm -> subtract(sm);
            case DenseMatrix<T> dm -> {
                checkSameRing(dm.ring());
                throw new DimensionMismatchException(this.dimension, this.dimension, dm.rows(), dm.cols());
            }
        };
    }

    @Override
    public SquareMatrix<T> multiply(SquareMatrix<T> other) {
        checkSameDimension(other);
        checkSameRing(other.ring);
        return new SquareMatrix<>(dimension, ring, MatrixOps.multiply(ring, data, other.data, dimension, dimension, dimension));
    }

    public DenseMatrix<T> multiply(DenseMatrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        checkSameRing(other.ring());
        if (this.dimension != other.rows()) {
            throw new DimensionMismatchException(
                "Cannot multiply matrix " + dimension + "x" + dimension + " with " + other.rows() + "x" + other.cols()
            );
        }
        return new DenseMatrix<>(this.dimension, other.cols(), ring, MatrixOps.multiply(ring, data, other.rawData(), this.dimension, this.dimension, other.cols()));
    }

    @Override
    public Matrix<T> multiply(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return switch (other) {
            case SquareMatrix<T> sm -> multiply(sm);
            case DenseMatrix<T> dm -> multiply(dm);
        };
    }

    @Override
    public Vector<T> multiply(Vector<T> vector) {
        Objects.requireNonNull(vector, "vector cannot be null");
        checkSameRing(vector.ring());
        if (this.dimension != vector.dimension()) {
            throw new DimensionMismatchException(
                "Cannot multiply matrix " + dimension + "x" + dimension + " with vector of dimension " + vector.dimension()
            );
        }
        return new Vector<>(this.dimension, MatrixOps.multiplyColumnVector(ring, data, vector.rawData(), this.dimension, this.dimension), ring);
    }

    @Override
    public SquareMatrix<T> scale(T scalar) {
        Objects.requireNonNull(scalar, "scalar cannot be null");
        return new SquareMatrix<>(dimension, ring, MatrixOps.scale(ring, data, scalar));
    }

    @Override
    public SquareMatrix<T> transpose() {
        return new SquareMatrix<>(dimension, ring, MatrixOps.transpose(data, dimension, dimension));
    }

    /**
     * Computes the trace (sum of main diagonal elements) of this square matrix.
     */
    public T trace() {
        return MatrixOps.trace(ring, data, dimension);
    }

    public T determinant() {
        return MatrixOps.determinant(ring, data, dimension);
    }

    public SquareMatrix<T> inverse() {
        return new SquareMatrix<>(dimension, ring, MatrixOps.inverse(ring, data, dimension));
    }

    @Override
    public List<T> row(int r) {
        if (r < 0 || r >= dimension) throw new IndexOutOfBoundsException("Row " + r + " out of bounds");
        List<T> res = new ArrayList<>(dimension);
        for (int c = 0; c < dimension; c++) {
            res.add(get(r, c));
        }
        return List.copyOf(res);
    }

    @Override
    public List<T> col(int c) {
        if (c < 0 || c >= dimension) throw new IndexOutOfBoundsException("Col " + c + " out of bounds");
        List<T> res = new ArrayList<>(dimension);
        for (int r = 0; r < dimension; r++) {
            res.add(get(r, c));
        }
        return List.copyOf(res);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<List<T>> streamRows() {
        return Arrays.stream((T[]) data).gather(Gatherers.windowFixed(dimension));
    }

    @Override
    public SquareMatrix<T> asSquareMatrix() {
        return this;
    }

    private void checkSameDimension(SquareMatrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        if (this.dimension != other.dimension) {
            throw new DimensionMismatchException(this.dimension, this.dimension, other.dimension, other.dimension);
        }
    }

    private void checkSameRing(Ring<T> otherRing) {
        if (!Objects.equals(this.ring, otherRing)) {
            throw new IllegalArgumentException("Cannot operate on matrices/vectors over different rings");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SquareMatrix<?> other
            && dimension == other.dimension
            && Objects.equals(ring, other.ring)
            && Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(dimension, ring) + Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(dimension).append("x").append(dimension).append(" SquareMatrix]\n");
        for (int i = 0; i < dimension; i++) {
            sb.append("  [");
            for (int j = 0; j < dimension; j++) {
                if (j > 0) sb.append(", ");
                sb.append(get(i, j));
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}
