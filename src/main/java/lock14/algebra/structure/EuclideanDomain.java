package lock14.algebra.structure;

import java.util.Objects;
import lock14.algebra.exceptions.UndefinedOperationException;

/**
 * A Euclidean Domain is an {@link IntegralDomain} endowed with a Euclidean function / division with remainder.
 *
 * @param <T> the carrier type of elements
 */
public interface EuclideanDomain<T> extends IntegralDomain<T> {

    record DivisionResult<T>(T quotient, T remainder) {
        public DivisionResult {
            Objects.requireNonNull(quotient, "quotient cannot be null");
            Objects.requireNonNull(remainder, "remainder cannot be null");
        }
    }

    record ExtendedGcdResult<T>(T gcd, T x, T y) {
        public ExtendedGcdResult {
            Objects.requireNonNull(gcd, "gcd cannot be null");
            Objects.requireNonNull(x, "x cannot be null");
            Objects.requireNonNull(y, "y cannot be null");
        }
    }

    /**
     * Computes the quotient and remainder such that {@code a = quotient * b + remainder}.
     *
     * @param a the dividend
     * @param b the divisor (must be non-zero)
     * @return the quotient and remainder
     */
    DivisionResult<T> divideAndRemainder(T a, T b);

    /**
     * Computes the quotient of {@code a / b}.
     */
    default T divide(T a, T b) {
        return divideAndRemainder(a, b).quotient();
    }

    /**
     * Computes the remainder of {@code a % b}.
     */
    default T remainder(T a, T b) {
        return divideAndRemainder(a, b).remainder();
    }

    /**
     * Computes the greatest common divisor of {@code a} and {@code b} using the Euclidean algorithm.
     */
    default T gcd(T a, T b) {
        T r0 = a;
        T r1 = b;
        while (!isZero(r1)) {
            T rem = remainder(r0, r1);
            r0 = r1;
            r1 = rem;
        }
        return r0;
    }

    /**
     * Computes the least common multiple of {@code a} and {@code b}.
     */
    default T lcm(T a, T b) {
        if (isZero(a) || isZero(b)) {
            return zero();
        }
        T g = gcd(a, b);
        return multiply(divide(a, g), b);
    }

    /**
     * Computes Bézout coefficients (x, y) such that {@code a*x + b*y = gcd(a, b)}.
     */
    default ExtendedGcdResult<T> extendedGcd(T a, T b) {
        T oldR = a, r = b;
        T oldS = one(), s = zero();
        T oldT = zero(), t = one();

        while (!isZero(r)) {
            DivisionResult<T> div = divideAndRemainder(oldR, r);
            T q = div.quotient();

            oldR = r;
            r = div.remainder();

            T tempS = s;
            s = subtract(oldS, multiply(q, s));
            oldS = tempS;

            T tempT = t;
            t = subtract(oldT, multiply(q, t));
            oldT = tempT;
        }
        return new ExtendedGcdResult<>(oldR, oldS, oldT);
    }
}
