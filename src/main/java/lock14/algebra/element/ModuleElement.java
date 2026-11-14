package lock14.algebra.element;

/**
 * Fluent self-typed interface for elements of a module (or vector space) over a scalar ring.
 *
 * @param <E> the concrete module element type
 * @param <R> the scalar ring element type
 */
public interface ModuleElement<E extends ModuleElement<E, R>, R> {

    /**
     * Computes the sum: {@code this + other}.
     */
    E add(E other);

    /**
     * Returns the additive inverse: {@code -this}.
     */
    E negate();

    /**
     * Computes the difference: {@code this - other}.
     */
    default E subtract(E other) {
        return add(other.negate());
    }

    /**
     * Computes the scalar multiple: {@code scalar * this}.
     */
    E scale(R scalar);
}
