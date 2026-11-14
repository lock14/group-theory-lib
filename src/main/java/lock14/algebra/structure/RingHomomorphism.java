package lock14.algebra.structure;

import java.util.Objects;
import java.util.function.Function;

/**
 * A unital ring homomorphism {@code phi: R -> S} satisfying {@code phi(a + b) = phi(a) + phi(b)},
 * {@code phi(a * b) = phi(a) * phi(b)}, and {@code phi(1_R) = 1_S}.
 *
 * @param <A> domain ring element type
 * @param <B> codomain ring element type
 */
@FunctionalInterface
public interface RingHomomorphism<A, B> extends Function<A, B> {

    @Override
    B apply(A element);

    default <C> RingHomomorphism<A, C> andThen(RingHomomorphism<B, C> after) {
        Objects.requireNonNull(after, "after cannot be null");
        return x -> after.apply(this.apply(x));
    }

    /**
     * Canonical ring characteristic map {@code Z -> R} sending {@code n} to {@code n . 1_R}.
     */
    static <R> RingHomomorphism<Long, R> fromIntegers(Ring<R> targetRing) {
        Objects.requireNonNull(targetRing, "targetRing cannot be null");
        return n -> targetRing.multiplyByScalar(targetRing.one(), n);
    }
}
