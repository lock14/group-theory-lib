package lock14.algebra.structure;

/**
 * A Vector Space is a {@link Module} whose scalar ring is a {@link Field}.
 *
 * @param <V> the vector element type
 * @param <F> the scalar field type
 */
public interface VectorSpace<V, F> extends Module<V, F> {

    /**
     * The scalar field.
     */
    Field<F> scalarField();

    @Override
    default Ring<F> scalarRing() {
        return scalarField();
    }
}
