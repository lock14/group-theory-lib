package lock14.algebra.structure;

import java.util.Objects;

/**
 * A Ring (Unital Ring) is a set equipped with addition and multiplication such that
 * (R, +, 0) is an Abelian Group and (R, *, 1) is a Monoid, and multiplication distributes over addition.
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
     * Multiplies {@code a} by integer scalar {@code n} (repeated addition / negation).
     */
    default T multiplyByScalar(T a, long n) {
        return asAdditiveGroup().power(a, n);
    }

    /**
     * Computes integer power {@code element^n} for n >= 0 under multiplication.
     */
    default T power(T element, int n) {
        return asMultiplicativeMonoid().power(element, n);
    }

    /**
     * Views this ring's additive structure as an {@link AbelianGroup}.
     */
    default AbelianGroup<T> asAdditiveGroup() {
        return new AbelianGroup<>() {
            @Override
            public T identity() {
                return Ring.this.zero();
            }

            @Override
            public T combine(T left, T right) {
                return Ring.this.add(left, right);
            }

            @Override
            public T inverse(T element) {
                return Ring.this.negate(element);
            }
        };
    }

    /**
     * Views this ring's multiplicative structure as a {@link Monoid}.
     */
    default Monoid<T> asMultiplicativeMonoid() {
        return new Monoid<>() {
            @Override
            public T identity() {
                return Ring.this.one();
            }

            @Override
            public T combine(T left, T right) {
                return Ring.this.multiply(left, right);
            }
        };
    }
}
