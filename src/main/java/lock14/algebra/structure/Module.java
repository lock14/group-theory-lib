package lock14.algebra.structure;

/**
 * A Module is an abelian group (M, +) equipped with scalar multiplication from a Ring R.
 *
 * @param <M> the module element type
 * @param <R> the scalar ring type
 */
public interface Module<M, R> {

    /**
     * The additive abelian group of module elements.
     */
    AbelianGroup<M> moduleGroup();

    /**
     * The scalar ring.
     */
    Ring<R> scalarRing();

    /**
     * Computes scalar multiplication: {@code scalar * element}.
     */
    M scale(R scalar, M element);

    default M zero() {
        return moduleGroup().identity();
    }

    default M add(M a, M b) {
        return moduleGroup().combine(a, b);
    }

    default M negate(M a) {
        return moduleGroup().inverse(a);
    }

    default M subtract(M a, M b) {
        return moduleGroup().remove(a, b);
    }
}
