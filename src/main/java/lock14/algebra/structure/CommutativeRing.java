package lock14.algebra.structure;

/**
 * A Commutative Ring is a {@link Ring} whose multiplication operation is commutative:
 * <pre>
 *   multiply(a, b) == multiply(b, a)
 * </pre>
 *
 * @param <T> the carrier type of elements
 */
public interface CommutativeRing<T> extends Ring<T> {
}
