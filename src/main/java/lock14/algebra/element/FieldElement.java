package lock14.algebra.element;

/**
 * Fluent self-typed interface for elements of a field.
 *
 * @param <E> the concrete element type
 */
public interface FieldElement<E extends FieldElement<E>> extends RingElement<E> {

    /**
     * Returns the multiplicative inverse (reciprocal): {@code 1 / this}.
     */
    E reciprocal();

    /**
     * Computes the quotient: {@code this / other = this * other.reciprocal()}.
     */
    default E divide(E other) {
        return multiply(other.reciprocal());
    }
}
