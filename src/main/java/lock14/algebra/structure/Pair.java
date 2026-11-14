package lock14.algebra.structure;

import java.util.Objects;

/**
 * Canonical immutable ordered pair {@code (first, second)} used as the carrier for {@link DirectProduct} algebras.
 *
 * @param <A>    left component type
 * @param <B>    right component type
 * @param first  the first (left) component
 * @param second the second (right) component
 */
public record Pair<A, B>(A first, B second) {
    /**
     * Validates that both pair components are non-null.
     *
     * @param first  the first (left) component
     * @param second the second (right) component
     */
    public Pair {
        Objects.requireNonNull(first, "first cannot be null");
        Objects.requireNonNull(second, "second cannot be null");
    }

    /**
     * Creates an ordered pair {@code (first, second)}.
     *
     * @param <A>    left component type
     * @param <B>    right component type
     * @param first  the first (left) component
     * @param second the second (right) component
     * @return a new {@code Pair} containing {@code first} and {@code second}
     */
    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }
}
