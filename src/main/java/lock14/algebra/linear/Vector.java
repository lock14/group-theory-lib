package lock14.algebra.linear;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lock14.algebra.element.ModuleElement;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.Ring;

/**
 * An immutable coordinate Vector of dimension {@code n >= 1} over an exact {@link Ring} {@code R},
 * backed by an encapsulated array with zero-copy internal construction.
 *
 * @param <T> scalar ring entry type
 */
public final class Vector<T> implements ModuleElement<Vector<T>, T> {

    private final int dimension;
    private final Ring<T> ring;
    private final Object[] data;

    Vector(int dimension, Ring<T> ring, Object[] trustedData) {
        this.dimension = dimension;
        this.ring = ring;
        this.data = trustedData;
    }

    @SafeVarargs
    public static <T> Vector<T> of(Ring<T> ring, T... elements) {
        Objects.requireNonNull(ring, "ring cannot be null");
        Objects.requireNonNull(elements, "elements cannot be null");
        if (elements.length == 0) {
            throw new IllegalArgumentException("Vector dimension must be positive");
        }
        Object[] copy = new Object[elements.length];
        for (int i = 0; i < elements.length; i++) {
            copy[i] = Objects.requireNonNull(elements[i], "Vector element cannot be null at index " + i);
        }
        return new Vector<>(elements.length, ring, copy);
    }

    public static <T> Vector<T> of(Ring<T> ring, List<T> elements) {
        Objects.requireNonNull(ring, "ring cannot be null");
        Objects.requireNonNull(elements, "elements cannot be null");
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Vector dimension must be positive");
        }
        Object[] copy = new Object[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            copy[i] = Objects.requireNonNull(elements.get(i), "Vector element cannot be null at index " + i);
        }
        return new Vector<>(copy.length, ring, copy);
    }

    public static <T> Vector<T> zeros(int dimension, Ring<T> ring) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Vector dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[dimension];
        Arrays.fill(arr, ring.zero());
        return new Vector<>(dimension, ring, arr);
    }

    public int dimension() {
        return dimension;
    }

    public Ring<T> ring() {
        return ring;
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
        get(i);
        Object[] copy = data.clone();
        copy[i] = val;
        return new Vector<>(dimension, ring, copy);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector<T> add(Vector<T> other) {
        checkCompatible(other);
        Object[] res = new Object[dimension];
        for (int i = 0; i < dimension; i++) {
            res[i] = ring.add((T) data[i], (T) other.data[i]);
        }
        return new Vector<>(dimension, ring, res);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector<T> negate() {
        Object[] res = new Object[dimension];
        for (int i = 0; i < dimension; i++) {
            res[i] = ring.negate((T) data[i]);
        }
        return new Vector<>(dimension, ring, res);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector<T> subtract(Vector<T> other) {
        checkCompatible(other);
        Object[] res = new Object[dimension];
        for (int i = 0; i < dimension; i++) {
            res[i] = ring.subtract((T) data[i], (T) other.data[i]);
        }
        return new Vector<>(dimension, ring, res);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector<T> scale(T scalar) {
        Objects.requireNonNull(scalar, "scalar cannot be null");
        Object[] res = new Object[dimension];
        for (int i = 0; i < dimension; i++) {
            res[i] = ring.multiply(scalar, (T) data[i]);
        }
        return new Vector<>(dimension, ring, res);
    }

    @SuppressWarnings("unchecked")
    public T dot(Vector<T> other) {
        checkCompatible(other);
        T sum = ring.zero();
        for (int i = 0; i < dimension; i++) {
            sum = ring.add(sum, ring.multiply((T) data[i], (T) other.data[i]));
        }
        return sum;
    }

    /**
     * Row-vector-by-matrix product {@code this^T * matrix}.
     */
    public Vector<T> multiply(Matrix<T> matrix) {
        Objects.requireNonNull(matrix, "matrix cannot be null");
        if (!Objects.equals(this.ring, matrix.ring())) {
            throw new IncompatibleAlgebraException("Cannot multiply vector and matrix over different rings");
        }
        if (this.dimension != matrix.rows()) {
            throw new DimensionMismatchException(
                "Cannot multiply vector of dimension " + dimension + " with matrix " + matrix.rows() + "x" + matrix.cols()
            );
        }
        Object[] matData = switch (matrix) {
            case RectangularMatrix<T> rm -> rm.rawData();
            case SquareMatrix<T> sm -> sm.rawData();
        };
        Object[] res = MatrixOps.multiplyRowVector(ring, this.data, matData, matrix.rows(), matrix.cols());
        return new Vector<>(matrix.cols(), ring, res);
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
            throw new IncompatibleAlgebraException("Cannot operate on vectors over different rings");
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
