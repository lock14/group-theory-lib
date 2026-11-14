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
     * @return the two-sided multiplicative inverse of {@code this}
     * @throws NonInvertibleElementException if this element is zero
     */
    E reciprocal();

    /**
     * Returns {@link Optional#of(Object)} the multiplicative inverse if non-zero,
     * or {@link Optional#empty()} if zero.
     *
     * @return an {@link Optional} containing {@code this^-1} if non-zero, or {@link Optional#empty()} if zero
     */
    Optional<E> tryReciprocal();

    /**
     * Computes right division: {@code this * other^-1}.
     *
     * @param other the right divisor (must be non-zero)
     * @return the right quotient {@code this * other^-1}
     * @throws NonInvertibleElementException if {@code other} is zero
     */
    default E divide(E other) {
        return multiply(other.reciprocal());
    }

    /**
     * Computes left division: {@code other^-1 * this}.
     *
     * @param other the left divisor (must be non-zero)
     * @return the left quotient {@code other^-1 * this}
     * @throws NonInvertibleElementException if {@code other} is zero
     */
    @SuppressWarnings("unchecked")
    default E divideLeft(E other) {
        return other.reciprocal().multiply((E) this);
    }
}
