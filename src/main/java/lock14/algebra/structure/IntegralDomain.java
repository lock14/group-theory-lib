package lock14.algebra.structure;

/**
 * An Integral Domain is a non-zero {@link CommutativeRing} with no zero divisors.
 * That is, if {@code a * b == 0}, then either {@code a == 0} or {@code b == 0}.
 *
 * @param <T> the carrier type of elements
 */
public interface IntegralDomain<T> extends CommutativeRing<T> {

    /**
     * Checks if the given element is a unit (has a multiplicative inverse in the ring).
     *
     * @param element the element to test
     * @return {@code true} if {@code element} has a multiplicative inverse in this domain, {@code false} otherwise
     */
    boolean isUnit(T element);
}
