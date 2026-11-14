package lock14.algebra.linear;

import java.util.List;
import java.util.Objects;
import lock14.algebra.structure.AbelianGroup;
import lock14.algebra.structure.Field;
import lock14.algebra.structure.VectorSpace;

/**
 * The Coordinate Vector Space {@code F^n} of dimension {@code n} over a scalar {@link Field} {@code F}.
 *
 * @param <T> scalar field element type
 */
public record CoordinateVectorSpace<T>(int dimension, Field<T> scalarField) implements VectorSpace<Vector<T>, T> {

    public CoordinateVectorSpace {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Vector space dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(scalarField, "scalarField cannot be null");
    }

    public CoordinateModule<T> asModule() {
        return new CoordinateModule<>(dimension, scalarField);
    }

    @SafeVarargs
    public final Vector<T> of(T... elements) {
        return asModule().of(elements);
    }

    public Vector<T> unitBasis(int index) {
        return asModule().unitBasis(index);
    }

    public List<Vector<T>> standardBasis() {
        return asModule().standardBasis();
    }

    @Override
    public AbelianGroup<Vector<T>> moduleGroup() {
        return asModule().moduleGroup();
    }

    @Override
    public Vector<T> scale(T scalar, Vector<T> element) {
        return asModule().scale(scalar, element);
    }
}
