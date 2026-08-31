package lock14.algebra.element;

/**
 * Fluent self-typed interface for elements of a group.
 *
 * @param <E> the concrete element type
 */
public interface GroupElement<E extends GroupElement<E>> {

    /**
     * Combines this element with another element under the group operation.
     */
    E combine(E other);

    /**
     * Returns the inverse of this element.
     */
    E inverse();

    /**
     * Combines this element with the inverse of another: {@code this * other^-1}.
     */
    @SuppressWarnings("unchecked")
    default E remove(E other) {
        return combine(other.inverse());
    }
}
