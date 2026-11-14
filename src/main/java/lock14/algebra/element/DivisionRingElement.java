package lock14.algebra.element;

import java.util.Optional;
import lock14.algebra.exceptions.NonInvertibleElementException;

/**
 * Fluent self-typed interface for elements of a (possibly non-commutative) division ring.
 *
 * @param <E> the concrete element type
 */
public interface DivisionRingElement<E extends DivisionRingElement<E>> extends RingElement<E> {

    /**
     * Returns the two-sided multiplicative inverse ({@code this^-1}).
     *
     * @throws NonInvertibleElementException if this element is zero
     */
    E reciprocal();

    /**
     * Returns {@link Optional#of} the multiplicative inverse if non-zero, or {@link Optional#empty()} if zero.
     */
    Optional<E> tryReciprocal();

    /**
     * Computes right division: {@code this * other^-1}.
     */
    default E divide(E other) {
        return multiply(other.reciprocal());
    }

    /**
     * Computes left division: {@code other^-1 * this}.
     */
    @SuppressWarnings("unchecked")
    default E divideLeft(E other) {
        return other.reciprocal().multiply((E) this);
    }
}
