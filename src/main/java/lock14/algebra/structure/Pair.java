package lock14.algebra.structure;

import java.util.Objects;

/**
 * Canonical immutable ordered pair {@code (first, second)} used as the carrier for {@link DirectProduct} algebras.
 *
 * @param <A> left component type
 * @param <B> right component type
 */
public record Pair<A, B>(A first, B second) {
    public Pair {
        Objects.requireNonNull(first, "first cannot be null");
        Objects.requireNonNull(second, "second cannot be null");
    }

    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }
}
