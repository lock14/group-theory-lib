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
     * Returns the additive identity ({@code 0}).
     *
     * @return the additive identity element
     */
    T zero();

    /**
     * Returns the multiplicative identity ({@code 1}).
     *
     * @return the multiplicative identity element
     */
    T one();

    /**
     * Computes the sum of {@code a} and {@code b}.
     *
     * @param a the first addend
     * @param b the second addend
     * @return the sum {@code a + b}
     */
    T add(T a, T b);

    /**
     * Computes the product of {@code a} and {@code b}.
     *
     * @param a the left factor
     * @param b the right factor
     * @return the product {@code a * b}
     */
    T multiply(T a, T b);

    /**
     * Checks if {@code element} is the additive identity (zero).
     *
     * @param element the element to test
     * @return {@code true} if {@code element} equals {@link #zero()}, {@code false} otherwise
     */
    default boolean isZero(T element) {
        return Objects.equals(zero(), element);
    }

    /**
     * Checks if {@code element} is the multiplicative identity (one).
     *
     * @param element the element to test
     * @return {@code true} if {@code element} equals {@link #one()}, {@code false} otherwise
     */
    default boolean isOne(T element) {
        return Objects.equals(one(), element);
    }
}
