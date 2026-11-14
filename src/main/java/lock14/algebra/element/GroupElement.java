package lock14.algebra.element;

/**
 * Fluent self-typed interface for elements of a group.
 *
 * @param <E> the concrete element type
 */
public interface GroupElement<E extends GroupElement<E>> {

    /**
     * Combines this element with another element under the group operation.
     *
     * @param other the right-hand group element
     * @return the group product of {@code this} and {@code other}
     */
    E combine(E other);

    /**
     * Returns the inverse of this element.
     *
     * @return the group inverse {@code this^-1}
     */
    E inverse();

    /**
     * Combines this element with the inverse of another: {@code this * other^-1}.
     *
     * @param other the element whose inverse is combined on the right
     * @return the result of {@code this * other^-1}
     */
    default E remove(E other) {
        return combine(other.inverse());
    }
}
