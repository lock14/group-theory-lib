package lock14.algebra.structure;

import java.util.Objects;

/**
 * A Ring (Unital Ring) is a set equipped with addition and multiplication such that
 * (R, +, 0) is an Abelian Group, (R, *, 1) is a Monoid, and multiplication distributes over addition.
 *
 * @param <T> the carrier type of elements
 */
public interface Ring<T> extends Semiring<T> {

    /**
     * Returns the additive inverse (-a) of {@code a}.
     */
    T negate(T a);

    /**
     * Computes the difference: {@code a - b = a + (-b)}.
     */
    default T subtract(T a, T b) {
        return add(a, negate(b));
    }

    /**
     * Multiplies {@code a} by integer scalar {@code n} using O(log |n|) double-and-add without wrapper allocation.
     */
    default T multiplyByScalar(T a, long n) {
        Objects.requireNonNull(a, "element cannot be null");
        if (n == 0 || isZero(a)) {
            return zero();
        }
        if (n < 0) {
            if (n == Long.MIN_VALUE) {
                T negA = negate(a);
                return add(negA, scalarPositive(negA, -(n + 1)));
            }
            return scalarPositive(negate(a), -n);
        }
        return scalarPositive(a, n);
    }

    private T scalarPositive(T element, long n) {
        T result = null;
        T current = element;
        long p = n;
        while (p > 0) {
            if ((p & 1L) == 1L) {
                result = (result == null) ? current : add(result, current);
            }
            p >>= 1;
            if (p > 0) {
                current = add(current, current);
            }
        }
        return result;
    }

    /**
     * Computes integer power {@code element^n} for {@code n >= 0} using O(log n) square-and-multiply.
     */
    default T power(T element, int n) {
        Objects.requireNonNull(element, "element cannot be null");
        if (n < 0) {
            throw new IllegalArgumentException("Ring exponent must be non-negative: " + n);
        }
        if (n == 0) {
            return one();
        }
        T result = null;
        T current = element;
        int p = n;
        while (p > 0) {
            if ((p & 1) == 1) {
                result = (result == null) ? current : multiply(result, current);
            }
            p >>= 1;
            if (p > 0) {
                current = multiply(current, current);
            }
        }
        return result;
    }

    /**
     * Views this ring's additive structure as an {@link AbelianGroup}.
     */
    default AbelianGroup<T> asAdditiveGroup() {
        record AdditiveGroupView<E>(Ring<E> ring) implements AbelianGroup<E> {
            @Override
            public E identity() {
                return ring.zero();
            }

            @Override
            public E combine(E left, E right) {
                return ring.add(left, right);
            }

            @Override
            public E inverse(E element) {
                return ring.negate(element);
            }
        }
        return new AdditiveGroupView<>(this);
    }

    /**
     * Views this ring's multiplicative structure as a {@link Monoid}.
     */
    default Monoid<T> asMultiplicativeMonoid() {
        record MultiplicativeMonoidView<E>(Ring<E> ring) implements Monoid<E> {
            @Override
            public E identity() {
                return ring.one();
            }

            @Override
            public E combine(E left, E right) {
                return ring.multiply(left, right);
            }
        }
        return new MultiplicativeMonoidView<>(this);
    }
}
