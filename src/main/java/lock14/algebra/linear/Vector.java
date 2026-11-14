package lock14.algebra.linear;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lock14.algebra.element.ModuleElement;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.structure.Ring;

/**
 * An immutable coordinate Vector of dimension n over a {@link Ring} R (or {@link lock14.algebra.structure.Field} F).
 *
 * @param <T> the scalar ring entry type
 */
public record Vector<T>(int dimension, Object[] data, Ring<T> ring) implements ModuleElement<Vector<T>, T> {

    public Vector {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Vector dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(data, "data cannot be null");
        Objects.requireNonNull(ring, "ring cannot be null");
        if (data.length != dimension) {
            throw new DimensionMismatchException("Data length " + data.length + " does not match dimension " + dimension);
        }
        Object[] copy = new Object[data.length];
        for (int i = 0; i < data.length; i++) {
            copy[i] = Objects.requireNonNull(data[i], "Vector element cannot be null at index " + i);
        }
        data = copy;
    }

    @SafeVarargs
    public static <T> Vector<T> of(Ring<T> ring, T... elements) {
        Objects.requireNonNull(elements, "elements cannot be null");
        return new Vector<>(elements.length, elements, ring);
    }

    public static <T> Vector<T> of(Ring<T> ring, List<T> elements) {
        Objects.requireNonNull(elements, "elements cannot be null");
        return new Vector<>(elements.size(), elements.toArray(), ring);
    }

    public static <T> Vector<T> zeros(int dimension, Ring<T> ring) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Vector dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[dimension];
        Arrays.fill(arr, ring.zero());
        return new Vector<>(dimension, arr, ring);
    }

    @Override
    public Object[] data() {
        return data.clone();
    }

    Object[] rawData() {
        return data;
    }

    @SuppressWarnings("unchecked")
    public T get(int i) {
        if (i < 0 || i >= dimension) {
            throw new IndexOutOfBoundsException("Vector index " + i + " out of bounds for dimension " + dimension);
        }
        return (T) data[i];
    }

    public Vector<T> with(int i, T val) {
        Objects.requireNonNull(val, "val cannot be null");
        get(i); // Bounds check
        Object[] copy = data.clone();
        copy[i] = val;
        return new Vector<>(dimension, copy, ring);
    }

    @Override
    public Vector<T> add(Vector<T> other) {
        checkCompatible(other);
        Object[] res = new Object[dimension];
        for (int i = 0; i < dimension; i++) {
            res[i] = ring.add(get(i), other.get(i));
        }
        return new Vector<>(dimension, res, ring);
    }

    @Override
    public Vector<T> negate() {
        Object[] res = new Object[dimension];
        for (int i = 0; i < dimension; i++) {
            res[i] = ring.negate(get(i));
        }
        return new Vector<>(dimension, res, ring);
    }

    @Override
    public Vector<T> subtract(Vector<T> other) {
        checkCompatible(other);
        Object[] res = new Object[dimension];
        for (int i = 0; i < dimension; i++) {
            res[i] = ring.subtract(get(i), other.get(i));
        }
        return new Vector<>(dimension, res, ring);
    }

    @Override
    public Vector<T> scale(T scalar) {
        Objects.requireNonNull(scalar, "scalar cannot be null");
        Object[] res = new Object[dimension];
        for (int i = 0; i < dimension; i++) {
            res[i] = ring.multiply(scalar, get(i));
        }
        return new Vector<>(dimension, res, ring);
    }

    public T dot(Vector<T> other) {
        checkCompatible(other);
        T sum = ring.zero();
        for (int i = 0; i < dimension; i++) {
            sum = ring.add(sum, ring.multiply(get(i), other.get(i)));
        }
        return sum;
    }

    /**
     * Computes the row-vector-by-matrix product {@code this^T * matrix}.
     */
    public Vector<T> multiply(Matrix<T> matrix) {
        Objects.requireNonNull(matrix, "matrix cannot be null");
        if (!Objects.equals(this.ring, matrix.ring())) {
            throw new IllegalArgumentException("Cannot multiply vector and matrix over different rings");
        }
        if (this.dimension != matrix.rows()) {
            throw new DimensionMismatchException(
                "Cannot multiply vector of dimension " + dimension + " with matrix " + matrix.rows() + "x" + matrix.cols()
            );
        }
        if (matrix.cols() <= 0) {
            throw new IllegalArgumentException("Resulting vector dimension must be positive, but matrix has 0 columns");
        }
        Object[] matData = switch (matrix) {
            case DenseMatrix<T> dm -> dm.rawData();
            case SquareMatrix<T> sm -> sm.rawData();
        };
        Object[] res = MatrixOps.multiplyRowVector(ring, this.data, matData, matrix.rows(), matrix.cols());
        return new Vector<>(matrix.cols(), res, ring);
    }

    @SuppressWarnings("unchecked")
    public List<T> toList() {
        return (List<T>) List.of(data);
    }

    private void checkCompatible(Vector<T> other) {
        Objects.requireNonNull(other, "other vector cannot be null");
        if (this.dimension != other.dimension) {
            throw new DimensionMismatchException("Vector dimension mismatch: " + this.dimension + " vs " + other.dimension);
        }
        if (!Objects.equals(this.ring, other.ring)) {
            throw new IllegalArgumentException("Cannot operate on vectors over different rings");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Vector<?> other
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
        return Arrays.toString(data);
    }
}
