package lock14.algebra.linear;

import java.util.Arrays;
import java.util.Objects;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.structure.Field;

/**
 * An immutable Vector of dimension n over a {@link Field} F.
 *
 * @param <T> the scalar field entry type
 */
public record Vector<T>(int dimension, Object[] data, Field<T> field) {

    public Vector {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Vector dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(data, "data cannot be null");
        Objects.requireNonNull(field, "field cannot be null");
        if (data.length != dimension) {
            throw new DimensionMismatchException("Data length " + data.length + " does not match dimension " + dimension);
        }
        data = data.clone();
    }

    @SafeVarargs
    public static <T> Vector<T> of(Field<T> field, T... elements) {
        return new Vector<>(elements.length, elements, field);
    }

    public static <T> Vector<T> zeros(int dimension, Field<T> field) {
        Object[] arr = new Object[dimension];
        Arrays.fill(arr, field.zero());
        return new Vector<>(dimension, arr, field);
    }

    @SuppressWarnings("unchecked")
    public T get(int i) {
        if (i < 0 || i >= dimension) {
            throw new IndexOutOfBoundsException("Vector index " + i + " out of bounds for dimension " + dimension);
        }
        return (T) data[i];
    }

    public Vector<T> add(Vector<T> other) {
        checkSameDimension(other);
        Object[] res = new Object[dimension];
        for (int i = 0; i < dimension; i++) {
            res[i] = field.add(get(i), other.get(i));
        }
        return new Vector<>(dimension, res, field);
    }

    public Vector<T> negate() {
        Object[] res = new Object[dimension];
        for (int i = 0; i < dimension; i++) {
            res[i] = field.negate(get(i));
        }
        return new Vector<>(dimension, res, field);
    }

    public Vector<T> subtract(Vector<T> other) {
        checkSameDimension(other);
        return add(other.negate());
    }

    public Vector<T> scale(T scalar) {
        Object[] res = new Object[dimension];
        for (int i = 0; i < dimension; i++) {
            res[i] = field.multiply(scalar, get(i));
        }
        return new Vector<>(dimension, res, field);
    }

    public T dot(Vector<T> other) {
        checkSameDimension(other);
        T sum = field.zero();
        for (int i = 0; i < dimension; i++) {
            sum = field.add(sum, field.multiply(get(i), other.get(i)));
        }
        return sum;
    }

    private void checkSameDimension(Vector<T> other) {
        Objects.requireNonNull(other, "other vector cannot be null");
        if (this.dimension != other.dimension) {
            throw new DimensionMismatchException("Vector dimension mismatch: " + this.dimension + " vs " + other.dimension);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Vector<?> other
            && dimension == other.dimension
            && Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return 31 * dimension + Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}
