package lock14.algebra.linear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.structure.AbelianGroup;
import lock14.algebra.structure.Field;
import lock14.algebra.structure.VectorSpace;

/**
 * The Coordinate Vector Space F^n of dimension {@code n} over a scalar {@link Field} F.
 *
 * @param <T> the scalar field element type
 */
public record CoordinateVectorSpace<T>(int dimension, Field<T> scalarField) implements VectorSpace<Vector<T>, T> {

    public CoordinateVectorSpace {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Vector space dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(scalarField, "scalarField cannot be null");
    }

    @SafeVarargs
    public final Vector<T> of(T... elements) {
        Objects.requireNonNull(elements, "elements cannot be null");
        if (elements.length != dimension) {
            throw new DimensionMismatchException("Expected " + dimension + " coordinates, but got " + elements.length);
        }
        return Vector.of(scalarField, elements);
    }

    public Vector<T> unitBasis(int index) {
        if (index < 0 || index >= dimension) {
            throw new IndexOutOfBoundsException("Basis index " + index + " out of bounds for dimension " + dimension);
        }
        Object[] arr = new Object[dimension];
        Arrays.fill(arr, scalarField.zero());
        arr[index] = scalarField.one();
        return new Vector<>(dimension, arr, scalarField);
    }

    public List<Vector<T>> standardBasis() {
        List<Vector<T>> basis = new ArrayList<>(dimension);
        for (int i = 0; i < dimension; i++) {
            basis.add(unitBasis(i));
        }
        return List.copyOf(basis);
    }

    @Override
    public AbelianGroup<Vector<T>> moduleGroup() {
        return new AbelianGroup<>() {
            @Override
            public Vector<T> identity() {
                return Vector.zeros(dimension, scalarField);
            }

            @Override
            public Vector<T> combine(Vector<T> left, Vector<T> right) {
                checkInSpace(left);
                checkInSpace(right);
                return left.add(right);
            }

            @Override
            public Vector<T> inverse(Vector<T> element) {
                checkInSpace(element);
                return element.negate();
            }
        };
    }

    @Override
    public Vector<T> scale(T scalar, Vector<T> element) {
        Objects.requireNonNull(scalar, "scalar cannot be null");
        checkInSpace(element);
        return element.scale(scalar);
    }

    private void checkInSpace(Vector<T> v) {
        Objects.requireNonNull(v, "vector cannot be null");
        if (v.dimension() != this.dimension) {
            throw new DimensionMismatchException("Vector dimension " + v.dimension() + " does not match vector space dimension " + this.dimension);
        }
        if (!Objects.equals(v.ring(), this.scalarField)) {
            throw new IllegalArgumentException("Vector scalar field does not match vector space scalar field");
        }
    }
}
