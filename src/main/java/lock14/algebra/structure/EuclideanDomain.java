package lock14.algebra.structure;

import java.util.Objects;

/**
 * A Euclidean Domain is an {@link IntegralDomain} endowed with a Euclidean function / division with remainder.
 *
 * @param <T> the carrier type of elements
 */
public interface EuclideanDomain<T> extends IntegralDomain<T> {

    /**
     * Quotient-remainder pair produced by Euclidean division {@code a = quotient * b + remainder}.
     *
     * @param <T>       the carrier type of elements
     * @param quotient  the Euclidean quotient
     * @param remainder the Euclidean remainder
     */
    record DivisionResult<T>(T quotient, T remainder) {
        /**
         * Validates that {@code quotient} and {@code remainder} are non-null.
         *
         * @param quotient  the Euclidean quotient
         * @param remainder the Euclidean remainder
         */
        public DivisionResult {
            Objects.requireNonNull(quotient, "quotient cannot be null");
            Objects.requireNonNull(remainder, "remainder cannot be null");
        }
    }

    /**
     * Result of the extended Euclidean algorithm satisfying {@code a * x + b * y = gcd}.
     *
     * @param <T> the carrier type of elements
     * @param gcd the normalized greatest common divisor {@code gcd(a, b)}
     * @param x   the first Bézout coefficient
     * @param y   the second Bézout coefficient
     */
    record ExtendedGcdResult<T>(T gcd, T x, T y) {
        /**
         * Validates that {@code gcd}, {@code x}, and {@code y} are non-null.
         *
         * @param gcd the normalized greatest common divisor
         * @param x   the first Bézout coefficient
         * @param y   the second Bézout coefficient
         */
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
     *
     * @param a the dividend
     * @param b the non-zero divisor
     * @return the Euclidean quotient of {@code a} divided by {@code b}
     */
    default T divide(T a, T b) {
        return divideAndRemainder(a, b).quotient();
    }

    /**
     * Computes the remainder of {@code a % b}.
     *
     * @param a the dividend
     * @param b the non-zero divisor
     * @return the Euclidean remainder of {@code a} divided by {@code b}
     */
    default T remainder(T a, T b) {
        return divideAndRemainder(a, b).remainder();
    }

    /**
     * Normalizes an element to its canonical associate representative (e.g., non-negative in {@code Z}, monic in {@code F[x]}).
     *
     * @param element the element to normalize
     * @return the canonical associate representative of {@code element}
     */
    default T normalize(T element) {
        return element;
    }

    /**
     * Computes the greatest common divisor of {@code a} and {@code b} using the Euclidean algorithm.
     *
     * @param a the first element
     * @param b the second element
     * @return the normalized greatest common divisor of {@code a} and {@code b}
     */
    default T gcd(T a, T b) {
        T r0 = a;
        T r1 = b;
        while (!isZero(r1)) {
            T rem = remainder(r0, r1);
            r0 = r1;
            r1 = rem;
        }
        return normalize(r0);
    }

    /**
     * Computes the least common multiple of {@code a} and {@code b}.
     *
     * @param a the first element
     * @param b the second element
     * @return the normalized least common multiple of {@code a} and {@code b}, or {@link #zero()} if either operand is zero
     */
    default T lcm(T a, T b) {
        if (isZero(a) || isZero(b)) {
            return zero();
        }
        T g = gcd(a, b);
        return normalize(multiply(divide(a, g), b));
    }

    /**
     * Computes Bézout coefficients {@code (x, y)} such that {@code a*x + b*y = gcd(a, b)},
     * where {@code gcd(a, b)} is normalized via {@link #normalize(Object)}.
     *
     * @param a the first element
     * @param b the second element
     * @return the normalized greatest common divisor together with Bézout coefficients {@code (x, y)}
     */
    default ExtendedGcdResult<T> extendedGcd(T a, T b) {
        Objects.requireNonNull(a, "a cannot be null");
        Objects.requireNonNull(b, "b cannot be null");
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
        if (isZero(oldR)) {
            return new ExtendedGcdResult<>(zero(), zero(), zero());
        }
        T normR = normalize(oldR);
        if (!Objects.equals(oldR, normR)) {
            T unit = divide(normR, oldR);
            oldR = normR;
            oldS = multiply(unit, oldS);
            oldT = multiply(unit, oldT);
        }
        return new ExtendedGcdResult<>(oldR, oldS, oldT);
    }
}
