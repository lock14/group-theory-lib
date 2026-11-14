package lock14.algebra.structure;

import java.util.Objects;
import java.util.Optional;
import lock14.algebra.exceptions.NonInvertibleElementException;

/**
 * A Division Ring (Skew-Field) is a non-zero {@link Ring} in which every non-zero element
 * has a two-sided multiplicative inverse ({@code a * reciprocal(a) == 1 == reciprocal(a) * a}).
 * Multiplication is not required to be commutative (e.g., Rational Quaternions {@code H(Q)}).
 *
 * @param <T> the carrier type of elements
 */
public interface DivisionRing<T> extends Ring<T> {

    /**
     * Returns the two-sided multiplicative inverse ({@code a^-1}) of non-zero element {@code a}.
     *
     * @param a the element to invert (must not be zero)
     * @return the reciprocal of {@code a}
     * @throws NonInvertibleElementException if {@code a} is zero
     */
    T reciprocal(T a);

    /**
     * Total invertibility query returning {@link Optional#of(Object)} the reciprocal when {@code a} is non-zero,
     * or {@link Optional#empty()} when {@code a} is zero.
     *
     * @param a the element to invert
     * @return an {@link Optional} containing {@code a^-1} if {@code a} is non-zero, or {@link Optional#empty()} if zero
     */
    default Optional<T> tryReciprocal(T a) {
        Objects.requireNonNull(a, "element cannot be null");
        return isZero(a) ? Optional.empty() : Optional.of(reciprocal(a));
    }

    /**
     * Computes right division: {@code a * b^-1}.
     *
     * @param a the dividend
     * @param b the right divisor (must be non-zero)
     * @return the right quotient {@code a * b^-1}
     * @throws NonInvertibleElementException if {@code b} is zero
     */
    default T divide(T a, T b) {
        Objects.requireNonNull(a, "dividend cannot be null");
        Objects.requireNonNull(b, "divisor cannot be null");
        if (isZero(b)) {
            throw new NonInvertibleElementException("Division by zero in division ring");
        }
        return multiply(a, reciprocal(b));
    }

    /**
     * Computes left division: {@code b^-1 * a}.
     *
     * @param a the dividend
     * @param b the left divisor (must be non-zero)
     * @return the left quotient {@code b^-1 * a}
     * @throws NonInvertibleElementException if {@code b} is zero
     */
    default T divideLeft(T a, T b) {
        Objects.requireNonNull(a, "dividend cannot be null");
        Objects.requireNonNull(b, "divisor cannot be null");
        if (isZero(b)) {
            throw new NonInvertibleElementException("Division by zero in division ring");
        }
        return multiply(reciprocal(b), a);
    }

    /**
     * Every non-zero element of a division ring is a unit.
     *
     * @param element the element to test
     * @return {@code true} if {@code element} is non-zero, {@code false} otherwise
     */
    default boolean isUnit(T element) {
        return !isZero(element);
    }

    /**
     * Views the non-zero elements of this division ring as a multiplicative {@link Group}.
     *
     * @return a {@link Group} view over the non-zero elements under multiplication
     */
    default Group<T> asMultiplicativeGroupOfUnits() {
        record UnitsGroupView<E>(DivisionRing<E> ring) implements Group<E> {
            @Override
            public E identity() {
                return ring.one();
            }

            @Override
            public E combine(E left, E right) {
                if (ring.isZero(left) || ring.isZero(right)) {
                    throw new IllegalArgumentException("Zero is not in the group of units");
                }
                return ring.multiply(left, right);
            }

            @Override
            public E inverse(E element) {
                return ring.reciprocal(element);
            }
        }
        return new UnitsGroupView<>(this);
    }
}
