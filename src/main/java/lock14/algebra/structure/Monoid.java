package lock14.algebra.structure;

import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;

/**
 * A Monoid is a {@link Semigroup} with an identity element {@code e} such that:
 * <pre>
 *   combine(identity(), a) == a == combine(a, identity())
 * </pre>
 *
 * @param <T> the carrier type of elements
 */
public interface Monoid<T> extends Semigroup<T> {

    /**
     * Returns the neutral/identity element of the monoid.
     *
     * @return the identity element of this monoid
     */
    T identity();

    /**
     * Checks if the given element is the identity element.
     *
     * @param element the element to test
     * @return {@code true} if {@code element} equals {@link #identity()}, {@code false} otherwise
     */
    default boolean isIdentity(T element) {
        return Objects.equals(identity(), element);
    }

    /**
     * Folds an iterable collection of elements under the monoid operation,
     * starting from {@link #identity()}.
     *
     * @param elements the sequence of elements to combine
     * @return the folded result, or {@link #identity()} if {@code elements} is empty
     */
    default T fold(Iterable<T> elements) {
        T acc = identity();
        for (T elem : elements) {
            acc = combine(acc, elem);
        }
        return acc;
    }

    /**
     * Power / repeated combination including {@code n = 0} returning {@link #identity()}.
     *
     * @param element the base element
     * @param n       the non-negative exponent ({@code n >= 0})
     * @return {@code element} combined with itself {@code n} times, or {@link #identity()} when {@code n == 0}
     * @throws IllegalArgumentException if {@code n < 0}
     */
    default T power(T element, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Exponent in a Monoid must be non-negative: " + n);
        }
        if (n == 0) {
            return identity();
        }
        return repeat(element, n);
    }

    /**
     * Returns a standard Java {@link Collector} that accumulates stream elements using this monoid.
     *
     * @return a {@link Collector} reducing stream elements via {@link #combine(Object, Object)} from {@link #identity()}
     */
    default Collector<T, ?, T> collector() {
        return Collector.of(
            () -> {
                @SuppressWarnings("unchecked")
                T[] box = (T[]) new Object[] { identity() };
                return box;
            },
            (box, elem) -> box[0] = combine(box[0], elem),
            (box1, box2) -> {
                box1[0] = combine(box1[0], box2[0]);
                return box1;
            },
            box -> box[0]
        );
    }

    /**
     * Returns a Java 25 Stream {@link Gatherer} that produces a rolling prefix scan of the elements.
     *
     * @return a {@link Gatherer} emitting running prefix combinations starting from {@link #identity()}
     */
    default Gatherer<T, ?, T> scanGatherer() {
        return Gatherers.scan(this::identity, this::combine);
    }
}
