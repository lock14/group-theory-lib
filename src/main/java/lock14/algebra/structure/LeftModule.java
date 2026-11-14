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
     *
     * @return the additive {@link AbelianGroup} of module elements
     */
    AbelianGroup<M> moduleGroup();

    /**
     * The governing scalar ring {@code R}.
     *
     * @return the governing scalar {@link Ring}
     */
    Ring<R> scalarRing();

    /**
     * Left scalar multiplication: {@code scalar . element}.
     *
     * @param scalar  the scalar multiplier from {@link #scalarRing()}
     * @param element the module element to scale
     * @return the scaled module element {@code scalar . element}
     */
    M scale(R scalar, M element);

    /**
     * Returns the zero element of this module.
     *
     * @return the additive identity element of {@link #moduleGroup()}
     */
    default M zero() {
        return moduleGroup().identity();
    }

    /**
     * Computes the sum of two module elements.
     *
     * @param a the first module element
     * @param b the second module element
     * @return the module sum {@code a + b}
     */
    default M add(M a, M b) {
        return moduleGroup().combine(a, b);
    }

    /**
     * Returns the additive inverse of a module element.
     *
     * @param a the module element to negate
     * @return the additive inverse {@code -a}
     */
    default M negate(M a) {
        return moduleGroup().inverse(a);
    }

    /**
     * Computes the difference of two module elements.
     *
     * @param a the minuend
     * @param b the subtrahend
     * @return the module difference {@code a - b}
     */
    default M subtract(M a, M b) {
        return moduleGroup().remove(a, b);
    }
}
