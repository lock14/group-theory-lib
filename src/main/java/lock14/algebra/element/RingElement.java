package lock14.algebra.element;

/**
 * Fluent self-typed interface for elements of a ring.
 *
 * @param <E> the concrete element type
 */
public interface RingElement<E extends RingElement<E>> {

    /**
     * Computes the sum: {@code this + other}.
     *
     * @param other the addend
     * @return the sum {@code this + other}
     */
    E add(E other);

    /**
     * Returns the additive inverse: {@code -this}.
     *
     * @return the additive inverse {@code -this}
     */
    E negate();

    /**
     * Computes the difference: {@code this - other}.
     *
     * @param other the subtrahend
     * @return the difference {@code this - other}
     */
    default E subtract(E other) {
        return add(other.negate());
    }

    /**
     * Computes the product: {@code this * other}.
     *
     * @param other the right-hand multiplier
     * @return the product {@code this * other}
     */
    E multiply(E other);
}
