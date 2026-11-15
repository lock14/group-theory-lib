package lock14.algebra.structure;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A Group is a {@link Monoid} where every element {@code a} has a unique inverse {@code a^-1} such that:
 * <pre>
 *   combine(a, inverse(a)) == identity() == combine(inverse(a), a)
 * </pre>
 *
 * @param <T> the carrier type of elements
 */
public interface Group<T> extends Monoid<T> {

    /**
     * Returns the inverse of the given element.
     *
     * @param element the element to invert
     * @return the inverse {@code element^-1}
     */
    T inverse(T element);

    /**
     * Combines {@code left} with the inverse of {@code right}: {@code left * right^-1}.
     *
     * @param left  the left operand
     * @param right the element whose inverse is combined on the right
     * @return the result of {@code left * right^-1}
     */
    default T remove(T left, T right) {
        return combine(left, inverse(right));
    }

    @Override
    default T power(T element, int n) {
        return power(element, (long) n);
    }

    /**
     * Exponentiates an element to any integer power (positive, zero, or negative).
     *
     * @param element the base element
     * @param n       the integer exponent (positive, zero, or negative)
     * @return {@code element} raised to the {@code n}-th power under the group operation
     */
    default T power(T element, long n) {
        Objects.requireNonNull(element, "element cannot be null");
        if (n == 0) {
            return identity();
        }
        if (n < 0) {
            if (n == Long.MIN_VALUE) {
                // Avoid overflow when negating Long.MIN_VALUE
                return combine(inverse(element), power(inverse(element), -(n + 1)));
            }
            return powerPositive(inverse(element), -n);
        }
        return powerPositive(element, n);
    }

    @Override
    default T power(T element, BigInteger n) {
        Objects.requireNonNull(element, "element cannot be null");
        Objects.requireNonNull(n, "exponent cannot be null");
        int sig = n.signum();
        if (sig == 0) {
            return identity();
        }
        T base = sig < 0 ? inverse(element) : element;
        BigInteger exp = n.abs();
        return Monoid.super.power(base, exp);
    }

    private T powerPositive(T element, long n) {
        T result = null;
        T current = element;
        long p = n;
        while (p > 0) {
            if ((p & 1) == 1) {
                result = (result == null) ? current : combine(result, current);
            }
            p >>= 1;
            if (p > 0) {
                current = combine(current, current);
            }
        }
        return result;
    }

    /**
     * Conjugates {@code h} by {@code g}: {@code g * h * g^-1}.
     *
     * @param g the conjugating element
     * @param h the element being conjugated
     * @return the conjugate {@code g * h * g^-1}
     */
    default T conjugate(T g, T h) {
        return combine(combine(g, h), inverse(g));
    }

    /**
     * Computes the group commutator of {@code a} and {@code b}: {@code a * b * a^-1 * b^-1}.
     *
     * @param a the first element
     * @param b the second element
     * @return the commutator {@code a * b * a^-1 * b^-1}
     */
    default T commutator(T a, T b) {
        return combine(combine(a, b), combine(inverse(a), inverse(b)));
    }
}
