package lock14.algebra.structure;

import java.util.Objects;
import lock14.algebra.exceptions.NonInvertibleElementException;

/**
 * A Field is a {@link CommutativeRing} where every non-zero element has a multiplicative inverse.
 *
 * @param <T> the carrier type of elements
 */
public interface Field<T> extends EuclideanDomain<T> {

    /**
     * Returns the multiplicative inverse (1 / a) of non-zero element {@code a}.
     *
     * @param a the element to invert (must not be zero)
     * @return the reciprocal of {@code a}
     * @throws ArithmeticException or {@link NonInvertibleElementException} if {@code a} is zero
     */
    T reciprocal(T a);

    /**
     * Computes the quotient: {@code a / b = a * reciprocal(b)}.
     */
    @Override
    default T divide(T a, T b) {
        if (isZero(b)) {
            throw new ArithmeticException("Division by zero in field");
        }
        return multiply(a, reciprocal(b));
    }

    @Override
    default DivisionResult<T> divideAndRemainder(T a, T b) {
        if (isZero(b)) {
            throw new ArithmeticException("Division by zero in field");
        }
        return new DivisionResult<>(divide(a, b), zero());
    }

    @Override
    default boolean isUnit(T element) {
        return !isZero(element);
    }

    /**
     * Views the non-zero elements of this field as a multiplicative {@link AbelianGroup}.
     */
    default AbelianGroup<T> asMultiplicativeGroupOfUnits() {
        return new AbelianGroup<>() {
            @Override
            public T identity() {
                return Field.this.one();
            }

            @Override
            public T combine(T left, T right) {
                if (Field.this.isZero(left) || Field.this.isZero(right)) {
                    throw new IllegalArgumentException("Zero is not in the group of units");
                }
                return Field.this.multiply(left, right);
            }

            @Override
            public T inverse(T element) {
                return Field.this.reciprocal(element);
            }
        };
    }
}
