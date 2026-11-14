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

    T zero();

    T one();

    T add(T a, T b);

    T negate(T a);

    default T subtract(T a, T b) {
        return add(a, negate(b));
    }

    T multiply(T a, T b);

    /**
     * Computes the multiplicative inverse of {@code a}.
     *
     * @throws NonInvertibleElementException if {@code a} is zero
     */
    T reciprocal(T a);

    default Optional<T> tryReciprocal(T a) {
        Objects.requireNonNull(a, "a cannot be null");
        if (isZero(a)) {
            return Optional.empty();
        }
        return Optional.of(reciprocal(a));
    }

    default T divide(T a, T b) {
        return multiply(a, reciprocal(b));
    }

    /**
     * Computes the non-negative magnitude {@code |a|}.
     */
    double abs(T a);

    /**
     * Computes the metric distance {@code |a - b|}.
     */
    default double distance(T a, T b) {
        return abs(subtract(a, b));
    }

    default boolean isZero(T a) {
        Objects.requireNonNull(a, "a cannot be null");
        return abs(a) == 0.0;
    }

    default boolean isApproxZero(T a, double epsilon) {
        validateEpsilon(epsilon);
        return abs(a) <= epsilon;
    }

    default boolean isApproxZero(T a) {
        return isApproxZero(a, NumericContext.currentEpsilon());
    }

    default boolean approxEquals(T a, T b, double epsilon) {
        Objects.requireNonNull(a, "a cannot be null");
        Objects.requireNonNull(b, "b cannot be null");
        validateEpsilon(epsilon);
        return distance(a, b) <= epsilon;
    }

    default boolean approxEquals(T a, T b) {
        return approxEquals(a, b, NumericContext.currentEpsilon());
    }

    private static void validateEpsilon(double epsilon) {
        if (Double.isNaN(epsilon) || Double.isInfinite(epsilon) || epsilon < 0.0) {
            throw new IllegalArgumentException("epsilon must be finite and non-negative, got: " + epsilon);
        }
    }
}
