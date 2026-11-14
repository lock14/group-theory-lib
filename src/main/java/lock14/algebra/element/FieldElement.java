package lock14.algebra.element;

/**
 * Fluent self-typed interface for elements of a commutative field.
 *
 * @param <E> the concrete element type
 */
public interface FieldElement<E extends FieldElement<E>> extends DivisionRingElement<E> {

    @Override
    default E divideLeft(E other) {
        return divide(other);
    }
}
