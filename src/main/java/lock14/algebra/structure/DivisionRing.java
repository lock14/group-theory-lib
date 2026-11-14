package lock14.algebra.structure;

import lock14.algebra.exceptions.NonInvertibleElementException;

/**
 * A Division Ring (Skew-Field) is a non-zero {@link Ring} in which every non-zero element
 * has a two-sided multiplicative inverse ({@code a * reciprocal(a) == 1 == reciprocal(a) * a}).
 * Multiplication is not required to be commutative (e.g., Hamilton Quaternions {@code H}).
 *
 * @param <T> the carrier type of elements
 */
public interface DivisionRing<T> extends Ring<T> {

    /**
     * Returns the two-sided multiplicative inverse ({@code a^-1}) of non-zero element {@code a}.
     *
     * @param a the element to invert (must not be zero)
     * @return the reciprocal of {@code a}
     * @throws ArithmeticException or {@link NonInvertibleElementException} if {@code a} is zero
     */
    T reciprocal(T a);

    /**
     * Computes right division: {@code a * b^-1}.
     */
    default T divide(T a, T b) {
        if (isZero(b)) {
            throw new ArithmeticException("Division by zero in division ring");
        }
        return multiply(a, reciprocal(b));
    }

    /**
     * Computes left division: {@code b^-1 * a}.
     */
    default T divideLeft(T a, T b) {
        if (isZero(b)) {
            throw new ArithmeticException("Division by zero in division ring");
        }
        return multiply(reciprocal(b), a);
    }

    /**
     * Every non-zero element of a division ring is a unit.
     */
    default boolean isUnit(T element) {
        return !isZero(element);
    }

    /**
     * Views the non-zero elements of this division ring as a multiplicative {@link Group}.
     */
    default Group<T> asMultiplicativeGroupOfUnits() {
        return new Group<>() {
            @Override
            public T identity() {
                return DivisionRing.this.one();
            }

            @Override
            public T combine(T left, T right) {
                if (DivisionRing.this.isZero(left) || DivisionRing.this.isZero(right)) {
                    throw new IllegalArgumentException("Zero is not in the group of units");
                }
                return DivisionRing.this.multiply(left, right);
            }

            @Override
            public T inverse(T element) {
                return DivisionRing.this.reciprocal(element);
            }
        };
    }
}
