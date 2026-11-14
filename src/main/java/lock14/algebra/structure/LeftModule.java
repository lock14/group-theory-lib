package lock14.algebra.structure;

/**
 * A Left R-Module is an abelian group {@code (M, +)} equipped with a left scalar action
 * {@code R x M -> M} satisfying {@code (r * s) . m = r . (s . m)}, distributivity, and {@code 1 . m = m}.
 *
 * @param <M> the module element type
 * @param <R> the scalar ring element type
 */
public interface LeftModule<M, R> {

    /**
     * The additive abelian group of module elements.
     */
    AbelianGroup<M> moduleGroup();

    /**
     * The governing scalar ring {@code R}.
     */
    Ring<R> scalarRing();

    /**
     * Left scalar multiplication: {@code scalar . element}.
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
