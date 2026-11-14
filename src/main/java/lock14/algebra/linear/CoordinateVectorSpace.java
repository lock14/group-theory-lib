package lock14.algebra.linear;

import java.util.List;
import java.util.Objects;
import lock14.algebra.structure.AbelianGroup;
import lock14.algebra.structure.Field;
import lock14.algebra.structure.VectorSpace;

/**
 * The Coordinate Vector Space {@code F^n} of dimension {@code n} over a scalar {@link Field} {@code F}.
 *
 * @param <T>         scalar field element type
 * @param dimension   the positive coordinate dimension {@code n}
 * @param scalarField the underlying scalar field {@code F}
 */
public record CoordinateVectorSpace<T>(int dimension, Field<T> scalarField) implements VectorSpace<Vector<T>, T> {

    /**
     * Validates that {@code dimension} is positive and {@code scalarField} is non-null.
     *
     * @param dimension   the positive coordinate dimension
     * @param scalarField the underlying scalar field
     */
    public CoordinateVectorSpace {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Vector space dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(scalarField, "scalarField cannot be null");
    }

    /**
     * Views this vector space as a free {@link CoordinateModule} over {@link #scalarField()}.
     *
     * @return the underlying {@link CoordinateModule}
     */
    public CoordinateModule<T> asModule() {
        return new CoordinateModule<>(dimension, scalarField);
    }

    /**
     * Constructs a vector in this vector space from exactly {@link #dimension()} coordinates.
     *
     * @param elements the coordinate entries
     * @return the coordinate vector in {@code F^n}
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public final Vector<T> of(T... elements) {
        return asModule().of(elements);
    }

    /**
     * Returns the {@code index}-th standard unit basis vector {@code e_index}.
     *
     * @param index zero-based basis index in {@code [0, dimension())}
     * @return the standard basis vector {@code e_index}
     */
    public Vector<T> unitBasis(int index) {
        return asModule().unitBasis(index);
    }

    /**
     * Returns the ordered standard basis {@code (e_0, e_1, ..., e_{n-1})} of this vector space.
     *
     * @return an unmodifiable list of the {@link #dimension()} standard basis vectors
     */
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
