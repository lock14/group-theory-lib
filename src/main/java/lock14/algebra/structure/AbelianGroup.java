package lock14.algebra.structure;

/**
 * An Abelian (Commutative) Group is a {@link Group} whose operation is commutative:
 * <pre>
 *   combine(a, b) == combine(b, a)
 * </pre>
 *
 * @param <T> the carrier type of elements
 */
public interface AbelianGroup<T> extends Group<T> {
}
