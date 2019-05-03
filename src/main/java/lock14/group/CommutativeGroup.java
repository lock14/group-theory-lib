package lock14.group;

/**
 * An commutative group (otherwise known as an abelian group)
 * has all the same properties of a Group. In addition, it has
 * the proprety that the 'plus' operation is commutative.
 * In other words:
 * For any x, y of type T, x.plus(y) = y.plus(x)
 */
public interface CommutativeGroup<T> extends Group<T>, CommutativeMonoid<T> {
}
