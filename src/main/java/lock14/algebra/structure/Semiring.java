package lock14.algebra.structure;

import java.util.Objects;

/**
 * A Semiring (Rig) is a set equipped with two binary operations: addition (+) and multiplication (*),
 * such that (R, +) is a commutative monoid, (R, *) is a monoid, multiplication distributes over addition,
 * and multiplication by zero annihilates the element.
 *
 * @param <T> the carrier type of elements
 */
public interface Semiring<T> {

    /**
     * Returns the additive identity (0).
     */
    T zero();

    /**
     * Returns the multiplicative identity (1).
     */
    T one();

    /**
     * Computes the sum of {@code a} and {@code b}.
     */
    T add(T a, T b);

    /**
     * Computes the product of {@code a} and {@code b}.
     */
    T multiply(T a, T b);

    /**
     * Checks if {@code element} is the additive identity (zero).
     */
    default boolean isZero(T element) {
        return Objects.equals(zero(), element);
    }

    /**
     * Checks if {@code element} is the multiplicative identity (one).
     */
    default boolean isOne(T element) {
        return Objects.equals(one(), element);
    }
}
