package lock14.algebra.structure;

import java.util.Objects;
import lock14.algebra.exceptions.NonInvertibleElementException;

/**
 * A Field is an exact {@link CommutativeRing} in which every non-zero element has a multiplicative inverse.
 *
 * @param <T> the carrier type of elements
 */
public interface Field<T> extends EuclideanDomain<T>, DivisionRing<T> {

    @Override
    T reciprocal(T a);

    @Override
    default T divide(T a, T b) {
        Objects.requireNonNull(a, "dividend cannot be null");
        Objects.requireNonNull(b, "divisor cannot be null");
        if (isZero(b)) {
            throw new NonInvertibleElementException("Division by zero in field");
        }
        return multiply(a, reciprocal(b));
    }

    @Override
    default T divideLeft(T a, T b) {
        return divide(a, b);
    }

    @Override
    default DivisionResult<T> divideAndRemainder(T a, T b) {
        if (isZero(b)) {
            throw new NonInvertibleElementException("Division by zero in field");
        }
        return new DivisionResult<>(divide(a, b), zero());
    }

    @Override
    default boolean isUnit(T element) {
        return !isZero(element);
    }

    @Override
    default T normalize(T element) {
        Objects.requireNonNull(element, "element cannot be null");
        return isZero(element) ? zero() : one();
    }

    @Override
    default AbelianGroup<T> asMultiplicativeGroupOfUnits() {
        record FieldUnitsView<E>(Field<E> field) implements AbelianGroup<E> {
            @Override
            public E identity() {
                return field.one();
            }

            @Override
            public E combine(E left, E right) {
                if (field.isZero(left) || field.isZero(right)) {
                    throw new IllegalArgumentException("Zero is not in the group of units");
                }
                return field.multiply(left, right);
            }

            @Override
            public E inverse(E element) {
                return field.reciprocal(element);
            }
        }
        return new FieldUnitsView<>(this);
    }
}
