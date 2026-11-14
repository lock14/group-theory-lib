package lock14.algebra.linear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.AbelianGroup;
import lock14.algebra.structure.LeftModule;
import lock14.algebra.structure.Ring;

/**
 * The Free Left Coordinate Module {@code R^n} of dimension {@code n} over a scalar {@link Ring} {@code R}.
 *
 * @param <T>        scalar ring element type
 * @param dimension  the positive coordinate dimension {@code n}
 * @param scalarRing the underlying scalar ring {@code R}
 */
public record CoordinateModule<T>(int dimension, Ring<T> scalarRing) implements LeftModule<Vector<T>, T> {

    /**
     * Validates that {@code dimension} is positive and {@code scalarRing} is non-null.
     *
     * @param dimension  the positive coordinate dimension
     * @param scalarRing the underlying scalar ring
     */
    public CoordinateModule {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Module dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(scalarRing, "scalarRing cannot be null");
    }

    /**
     * Constructs a vector in this coordinate module from exactly {@link #dimension()} coordinates.
     *
     * @param elements the coordinate entries
     * @return the coordinate vector in {@code R^n}
     */
    @SafeVarargs
    public final Vector<T> of(T... elements) {
        Objects.requireNonNull(elements, "elements cannot be null");
        if (elements.length != dimension) {
            throw new DimensionMismatchException("Expected " + dimension + " coordinates, but got " + elements.length);
        }
        return Vector.of(scalarRing, elements);
    }

    /**
     * Returns the {@code index}-th standard unit basis vector {@code e_index} (with {@code 1} at {@code index} and {@code 0} elsewhere).
     *
     * @param index zero-based basis index in {@code [0, dimension())}
     * @return the standard basis vector {@code e_index}
     */
    public Vector<T> unitBasis(int index) {
        if (index < 0 || index >= dimension) {
            throw new IndexOutOfBoundsException("Basis index " + index + " out of bounds for dimension " + dimension);
        }
        Object[] arr = new Object[dimension];
        Arrays.fill(arr, scalarRing.zero());
        arr[index] = scalarRing.one();
        return new Vector<>(dimension, scalarRing, arr);
    }

    /**
     * Returns the ordered standard basis {@code (e_0, e_1, ..., e_{n-1})} of this free module.
     *
     * @return an unmodifiable list of the {@link #dimension()} standard basis vectors
     */
    public List<Vector<T>> standardBasis() {
        List<Vector<T>> basis = new ArrayList<>(dimension);
        for (int i = 0; i < dimension; i++) {
            basis.add(unitBasis(i));
        }
        return List.copyOf(basis);
    }

    @Override
    public AbelianGroup<Vector<T>> moduleGroup() {
        record ModuleGroupView<E>(CoordinateModule<E> mod) implements AbelianGroup<Vector<E>> {
            @Override
            public Vector<E> identity() {
                return Vector.zeros(mod.dimension, mod.scalarRing);
            }

            @Override
            public Vector<E> combine(Vector<E> left, Vector<E> right) {
                mod.checkInModule(left);
                mod.checkInModule(right);
                return left.add(right);
            }

            @Override
            public Vector<E> inverse(Vector<E> element) {
                mod.checkInModule(element);
                return element.negate();
            }
        }
        return new ModuleGroupView<>(this);
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
            throw new DimensionMismatchException(
                "Vector dimension " + v.dimension() + " does not match module dimension " + this.dimension
            );
        }
        if (!Objects.equals(v.ring(), this.scalarRing)) {
            throw new IncompatibleAlgebraException("Vector scalar ring does not match module scalar ring");
        }
    }
}
