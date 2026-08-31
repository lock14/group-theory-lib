package lock14.algebra.structure;

import java.util.Objects;
import java.util.function.BinaryOperator;

/**
 * A Magma is a set equipped with a single binary operation that is closed over the set.
 *
 * @param <T> the carrier type of elements
 */
@FunctionalInterface
public interface Magma<T> {

    /**
     * Combines two elements of type {@code T} under the magma operation.
     *
     * @param left  the first operand
     * @param right the second operand
     * @return the result of combining left and right
     */
    T combine(T left, T right);

    /**
     * Converts this magma operation into a standard {@link BinaryOperator}.
     */
    default BinaryOperator<T> asBinaryOperator() {
        return this::combine;
    }
}
