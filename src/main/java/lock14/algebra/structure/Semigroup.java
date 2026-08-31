package lock14.algebra.structure;

import java.util.Objects;

/**
 * A Semigroup is a {@link Magma} whose binary operation is associative:
 * <pre>
 *   combine(combine(a, b), c) == combine(a, combine(b, c))
 * </pre>
 *
 * @param <T> the carrier type of elements
 */
@FunctionalInterface
public interface Semigroup<T> extends Magma<T> {

    /**
     * Repeats the combination of {@code element} with itself {@code n} times (n >= 1).
     * Uses binary exponentiation for O(log n) evaluations.
     *
     * @param element the element to repeat
     * @param n       the number of times to combine (must be >= 1)
     * @return the result of element^n
     */
    default T repeat(T element, int n) {
        Objects.requireNonNull(element, "element cannot be null");
        if (n <= 0) {
            throw new IllegalArgumentException("Repeat count must be positive: " + n);
        }
        T result = null;
        T current = element;
        int p = n;
        while (p > 0) {
            if ((p & 1) == 1) {
                result = (result == null) ? current : combine(result, current);
            }
            p >>= 1;
            if (p > 0) {
                current = combine(current, current);
            }
        }
        return result;
    }
}
