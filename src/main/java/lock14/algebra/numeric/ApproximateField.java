package lock14.algebra.numeric;

import java.util.Objects;
import java.util.Optional;
import lock14.algebra.exceptions.NonInvertibleElementException;

/**
 * Explicit algebraic contract for approximate floating-point field structures (such as IEEE-754 {@code double}
 * and {@link Complex64}).
 *
 * <p>Unlike {@link lock14.algebra.structure.Field}, implementations of {@code ApproximateField} are subject
 * to IEEE-754 rounding, overflow, and underflow, and therefore satisfy field laws up to a numerical tolerance
 * rather than exact bit-for-bit equality.</p>
 *
 * @param <T> the carrier type of approximate elements
 */
public interface ApproximateField<T> {

    /**
     * Returns the additive identity {@code 0}.
     *
     * @return the zero element
     */
    T zero();

    /**
     * Returns the multiplicative identity {@code 1}.
     *
     * @return the unit element
     */
    T one();

    /**
     * Computes the approximate sum {@code a + b}.
     *
     * @param a the left operand
     * @param b the right operand
     * @return the approximate sum {@code a + b}
     */
    T add(T a, T b);

    /**
     * Computes the additive inverse {@code -a}.
     *
     * @param a the element to negate
     * @return the negated element {@code -a}
     */
    T negate(T a);

    /**
     * Computes the approximate difference {@code a - b}.
     *
     * @param a the minuend
     * @param b the subtrahend
     * @return the approximate difference {@code a - b}
     */
    default T subtract(T a, T b) {
        return add(a, negate(b));
    }

    /**
     * Computes the approximate product {@code a * b}.
     *
     * @param a the left factor
     * @param b the right factor
     * @return the approximate product {@code a * b}
     */
    T multiply(T a, T b);

    /**
     * Computes the multiplicative inverse of {@code a}.
     *
     * @param a the element to invert
     * @return the approximate reciprocal {@code a^-1}
     * @throws NonInvertibleElementException if {@code a} is zero
     */
    T reciprocal(T a);

    /**
     * Attempts to compute the multiplicative inverse of {@code a}, returning {@link Optional#empty()} if {@code a} is zero.
     *
     * @param a the element to invert
     * @return an {@link Optional} containing {@code a^-1}, or {@link Optional#empty()} if {@link #isZero} returns {@code true}
     */
    default Optional<T> tryReciprocal(T a) {
        Objects.requireNonNull(a, "a cannot be null");
        if (isZero(a)) {
            return Optional.empty();
        }
        return Optional.of(reciprocal(a));
    }

    /**
     * Computes the approximate quotient {@code a / b}.
     *
     * @param a the dividend
     * @param b the non-zero divisor
     * @return the approximate quotient {@code a / b}
     * @throws NonInvertibleElementException if {@code b} is zero
     */
    default T divide(T a, T b) {
        return multiply(a, reciprocal(b));
    }

    /**
     * Computes the non-negative magnitude {@code |a|}.
     *
     * @param a the element whose magnitude is measured
     * @return the non-negative norm {@code |a|}
     */
    double abs(T a);

    /**
     * Computes the metric distance {@code |a - b|}.
     *
     * @param a the first element
     * @param b the second element
     * @return the non-negative distance {@code |a - b|}
     */
    default double distance(T a, T b) {
        return abs(subtract(a, b));
    }

    /**
     * Returns {@code true} if {@code abs(a) == 0.0}.
     *
     * @param a the element to test
     * @return {@code true} if {@code a} has exact magnitude zero
     */
    default boolean isZero(T a) {
        Objects.requireNonNull(a, "a cannot be null");
        return abs(a) == 0.0;
    }

    /**
     * Returns {@code true} if {@code abs(a) <= epsilon}.
     *
     * @param a       the element to test
     * @param epsilon the non-negative finite tolerance
     * @return {@code true} if {@code a} is within {@code epsilon} of zero
     */
    default boolean isApproxZero(T a, double epsilon) {
        validateEpsilon(epsilon);
        return abs(a) <= epsilon;
    }

    /**
     * Returns {@code true} if {@code a} is within {@link NumericContext#currentEpsilon()} of zero.
     *
     * @param a the element to test
     * @return {@code true} if {@code abs(a) <= NumericContext.currentEpsilon()}
     */
    default boolean isApproxZero(T a) {
        return isApproxZero(a, NumericContext.currentEpsilon());
    }

    /**
     * Returns {@code true} if {@code distance(a, b) <= epsilon}.
     *
     * @param a       the first element
     * @param b       the second element
     * @param epsilon the non-negative finite tolerance
     * @return {@code true} if {@code |a - b| <= epsilon}
     */
    default boolean approxEquals(T a, T b, double epsilon) {
        Objects.requireNonNull(a, "a cannot be null");
        Objects.requireNonNull(b, "b cannot be null");
        validateEpsilon(epsilon);
        return distance(a, b) <= epsilon;
    }

    /**
     * Returns {@code true} if {@code distance(a, b) <= NumericContext.currentEpsilon()}.
     *
     * @param a the first element
     * @param b the second element
     * @return {@code true} if {@code a} and {@code b} are approximately equal within the active tolerance
     */
    default boolean approxEquals(T a, T b) {
        return approxEquals(a, b, NumericContext.currentEpsilon());
    }

    private static void validateEpsilon(double epsilon) {
        if (Double.isNaN(epsilon) || Double.isInfinite(epsilon) || epsilon < 0.0) {
            throw new IllegalArgumentException("epsilon must be finite and non-negative, got: " + epsilon);
        }
    }
}
