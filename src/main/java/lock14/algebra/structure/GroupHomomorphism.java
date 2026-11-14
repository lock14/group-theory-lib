package lock14.algebra.structure;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * A structure-preserving map {@code phi: G -> H} between {@link Group}s satisfying
 * {@code phi(a * b) = phi(a) * phi(b)}.
 *
 * @param <A> domain group element type
 * @param <B> codomain group element type
 */
@FunctionalInterface
public interface GroupHomomorphism<A, B> extends Function<A, B> {

    @Override
    B apply(A element);

    /**
     * Composes this homomorphism with {@code after}: {@code x -> after.apply(this.apply(x))}.
     */
    default <C> GroupHomomorphism<A, C> andThen(GroupHomomorphism<B, C> after) {
        Objects.requireNonNull(after, "after cannot be null");
        return x -> after.apply(this.apply(x));
    }

    /**
     * Computes the carrier of {@code ker(phi) = { g in G | phi(g) = e_H }} for a finite domain group.
     */
    default Set<A> kernelElements(FiniteGroup<A> domain, Group<B> codomain) {
        Objects.requireNonNull(domain, "domain cannot be null");
        Objects.requireNonNull(codomain, "codomain cannot be null");
        B targetId = codomain.identity();
        Set<A> ker = new LinkedHashSet<>();
        for (A g : domain.elements()) {
            if (Objects.equals(apply(g), targetId)) {
                ker.add(g);
            }
        }
        return Set.copyOf(ker);
    }

    /**
     * Computes the carrier of {@code im(phi) = { phi(g) | g in G }} for a finite domain group.
     */
    default Set<B> imageElements(FiniteGroup<A> domain) {
        Objects.requireNonNull(domain, "domain cannot be null");
        Set<B> img = new LinkedHashSet<>();
        for (A g : domain.elements()) {
            img.add(apply(g));
        }
        return Set.copyOf(img);
    }

    /**
     * Verifies exhaustively that {@code phi(a * b) == phi(a) * phi(b)} across a finite group {@code G}.
     */
    default boolean isValidHomomorphism(FiniteGroup<A> domain, Group<B> codomain) {
        Objects.requireNonNull(domain, "domain cannot be null");
        Objects.requireNonNull(codomain, "codomain cannot be null");
        if (!Objects.equals(apply(domain.identity()), codomain.identity())) {
            return false;
        }
        for (A a : domain.elements()) {
            for (A b : domain.elements()) {
                B lhs = apply(domain.combine(a, b));
                B rhs = codomain.combine(apply(a), apply(b));
                if (!Objects.equals(lhs, rhs)) {
                    return false;
                }
            }
        }
        return true;
    }
}
