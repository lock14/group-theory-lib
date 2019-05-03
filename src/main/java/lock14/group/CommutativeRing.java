package lock14.group;

/**
 * An commutative ring has all the same properties of a Ring.
 * In addition, it has the proprety that the 'times' operation
 * is commutative. In other words:
 *
 * For any x, y of type T, x.times(y) = y.times(x)
 */
public interface CommutativeRing<T> extends Ring<T> {
}
