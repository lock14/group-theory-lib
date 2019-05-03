package lock14.group;

/**
 * A ring is an abelian group with a second operation 'times'
 * with the following properties:
 *
 * 1. The 'times' operation is closed, i.e. for every x, y of type T
 *    x.times(y) is also of type T
 *
 * 2. The 'times' operation is associative, that is for any three elements x, y, and z
 *    of type T, x.times(y).times(z) = x.times(y.times(z))
 *
 * 3. There is a multiplicative identity element, f of type  T, such that for any x of type T
 *    x.times(f) = f.times(x) = x
 *
 * 4. 'times' is distributive over 'plus', i.e. for any x,y, and z of Type T the following is true
 *     i. (right distributivity): x.plus(y).times(z) = x.times(z).plus(y.times(z))
 *     ii. (left distributivity): x.times(y.plus(z)) = x.times(y).plus(x.times(z))
 *
 * IMPORTANT: Note that commutativity is not guaranteed over the 'times' operation.
 *            Therefore it is NOT safe to assume that for any x, y of type T
 *            x.times(y) = y.times(x)
 */
public interface Ring<T> extends CommutativeGroup<T> {

    public T multiplicativeIdentity();

    public T times(T other);
}
