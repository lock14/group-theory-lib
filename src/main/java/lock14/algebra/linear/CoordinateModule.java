package lock14.algebra.linear;

import java.util.Arrays;
import java.util.Objects;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.structure.AbelianGroup;
import lock14.algebra.structure.Module;
import lock14.algebra.structure.Ring;

/**
 * The Free Coordinate Module R^n of dimension {@code n} over a scalar {@link Ring} R.
 *
 * @param <T> the scalar ring element type
 */
public record CoordinateModule<T>(int dimension, Ring<T> scalarRing) implements Module<Vector<T>, T> {

    public CoordinateModule {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Module dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(scalarRing, "scalarRing cannot be null");
    }

    @SafeVarargs
    public final Vector<T> of(T... elements) {
        Objects.requireNonNull(elements, "elements cannot be null");
        if (elements.length != dimension) {
            throw new DimensionMismatchException("Expected " + dimension + " coordinates, but got " + elements.length);
        }
        return Vector.of(scalarRing, elements);
    }

    public Vector<T> unitBasis(int index) {
        if (index < 0 || index >= dimension) {
            throw new IndexOutOfBoundsException("Basis index " + index + " out of bounds for dimension " + dimension);
        }
        Object[] arr = new Object[dimension];
        Arrays.fill(arr, scalarRing.zero());
        arr[index] = scalarRing.one();
        return new Vector<>(dimension, arr, scalarRing);
    }

    @Override
    public AbelianGroup<Vector<T>> moduleGroup() {
        return new AbelianGroup<>() {
            @Override
            public Vector<T> identity() {
                return Vector.zeros(dimension, scalarRing);
            }

            @Override
            public Vector<T> combine(Vector<T> left, Vector<T> right) {
                checkInModule(left);
                checkInModule(right);
                return left.add(right);
            }

            @Override
            public Vector<T> inverse(Vector<T> element) {
                checkInModule(element);
                return element.negate();
            }
        };
    }

    @Override
    public Vector<T> scale(T scalar, Vector<T> element) {
        Objects.requireNonNull(scalar, "scalar cannot be null");
        checkInModule(element);
        return element.scale(scalar);
    }

    private void checkInModule(Vector<T> v) {
        Objects.requireNonNull(v, "vector cannot be null");
        if (v.dimension() != this.dimension) {
            throw new DimensionMismatchException("Vector dimension " + v.dimension() + " does not match module dimension " + this.dimension);
        }
        if (!Objects.equals(v.ring(), this.scalarRing)) {
            throw new IllegalArgumentException("Vector scalar ring does not match module scalar ring");
        }
    }
}
