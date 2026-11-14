package lock14.algebra.structure;

/**
 * A Vector Space is a {@link LeftModule} whose scalar ring is a commutative {@link Field}.
 *
 * @param <V> the vector element type
 * @param <F> the scalar field element type
 */
public interface VectorSpace<V, F> extends LeftModule<V, F> {

    /**
     * The scalar field {@code F}.
     *
     * @return the governing scalar {@link Field}
     */
    Field<F> scalarField();

    @Override
    default Ring<F> scalarRing() {
        return scalarField();
    }
}
