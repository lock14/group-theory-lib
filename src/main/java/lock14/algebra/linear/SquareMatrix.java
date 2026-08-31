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
 * An immutable n x n Square Matrix over a Ring R.
 *
 * @param <T> the entry type in Ring R
 */
public record SquareMatrix<T>(int dimension, Ring<T> ring, Object[] data)
    implements Matrix<T>, RingElement<SquareMatrix<T>> {

    public SquareMatrix {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Square matrix dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(ring, "Ring cannot be null");
        Objects.requireNonNull(data, "Data cannot be null");
        if (data.length != dimension * dimension) {
            throw new DimensionMismatchException("Element count " + data.length + " does not match square dimension " + dimension + "x" + dimension);
        }
    }

    @SafeVarargs
    public static <T> SquareMatrix<T> of(int n, Ring<T> ring, T... elements) {
        Objects.requireNonNull(elements, "Elements cannot be null");
        if (elements.length != n * n) {
            throw new DimensionMismatchException("Element count " + elements.length + " does not match expected " + (n * n));
        }
        Object[] copy = new Object[elements.length];
        for (int i = 0; i < elements.length; i++) {
            copy[i] = Objects.requireNonNull(elements[i], "Matrix element cannot be null at index " + i);
        }
        return new SquareMatrix<>(n, ring, copy);
    }

    public static <T> SquareMatrix<T> zeros(int n, Ring<T> ring) {
        if (n <= 0) throw new IllegalArgumentException("Dimension must be positive: " + n);
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[n * n];
        Arrays.fill(arr, ring.zero());
        return new SquareMatrix<>(n, ring, arr);
    }

    public static <T> SquareMatrix<T> identity(int n, Ring<T> ring) {
        if (n <= 0) throw new IllegalArgumentException("Dimension must be positive: " + n);
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[n * n];
        Arrays.fill(arr, ring.zero());
        for (int i = 0; i < n; i++) {
            arr[i * n + i] = ring.one();
        }
        return new SquareMatrix<>(n, ring, arr);
    }

    public static <T> SquareMatrix<T> of(Ring<T> ring, T[][] matrix2D) {
        Objects.requireNonNull(matrix2D, "matrix2D cannot be null");
        int n = matrix2D.length;
        if (n == 0 || matrix2D[0].length != n) {
            throw new IllegalArgumentException("Provided 2D array is not square (" + n + "x" + (n == 0 ? 0 : matrix2D[0].length) + ")");
        }
        DenseMatrix<T> dense = DenseMatrix.of(ring, matrix2D);
        return new SquareMatrix<>(n, ring, dense.data());
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
    public SquareMatrix<T> add(SquareMatrix<T> other) {
        checkSameDimension(other);
        return new SquareMatrix<>(dimension, ring, MatrixOps.add(ring, data, other.data));
    }

    @Override
    public SquareMatrix<T> negate() {
        return new SquareMatrix<>(dimension, ring, MatrixOps.negate(ring, data));
    }

    @Override
    public SquareMatrix<T> subtract(SquareMatrix<T> other) {
        checkSameDimension(other);
        return new SquareMatrix<>(dimension, ring, MatrixOps.subtract(ring, data, other.data));
    }

    @Override
    public SquareMatrix<T> multiply(SquareMatrix<T> other) {
        checkSameDimension(other);
        return new SquareMatrix<>(dimension, ring, MatrixOps.multiply(ring, data, other.data, dimension, dimension, dimension));
    }

    public SquareMatrix<T> scale(T scalar) {
        Objects.requireNonNull(scalar, "scalar cannot be null");
        return new SquareMatrix<>(dimension, ring, MatrixOps.scale(ring, data, scalar));
    }

    @Override
    public SquareMatrix<T> transpose() {
        return new SquareMatrix<>(dimension, ring, MatrixOps.transpose(data, dimension, dimension));
    }

    @Override
    public T trace() {
        return MatrixOps.trace(ring, data, dimension, dimension);
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
        return res;
    }

    @Override
    public List<T> col(int c) {
        if (c < 0 || c >= dimension) throw new IndexOutOfBoundsException("Col " + c + " out of bounds");
        List<T> res = new ArrayList<>(dimension);
        for (int r = 0; r < dimension; r++) {
            res.add(get(r, c));
        }
        return res;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<List<T>> streamRows() {
        if (dimension == 0) return Stream.empty();
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

    @Override
    public boolean equals(Object o) {
        return o instanceof SquareMatrix<?> other
            && dimension == other.dimension
            && Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return 31 * dimension + Arrays.hashCode(data);
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
