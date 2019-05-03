package lock14.group;

/**
 * A group is a set of elements of type T with an associated group operation, 'plus',
 * that has the following properties:
 *
 * 1. The 'plus' operation is closed, i.e. for every x, y of type T
 *    x.plus(y) is also of type T
 *
 * 2. The 'plus' operation is associative, that is for any three elements x, y, and z
 *    of type T, x.plus(y).plus(z) = x.plus(y.plus(z))
 *
 * 3. There is an additive identity element, e of type T, such that for any x of type T
 *    x.plus(e) = e.plus(x) = x
 *
 * 4. For any x of type T, there exists an additive inverse y of type T such that
 *    x.plus(y) = y.plus(x) = e. As a consequence, a new operation 'minus' can
 *    be defined as x.minus(y) = x.plus(y.additiveInverse())
 *
 * IMPORTANT: Note that commutativity is not guaranteed over the 'plus' operation.
 *            Therefore it is NOT safe to assume that for any x, y of type T
 *            x.plus(y) = y.plus(x)
 */
public interface Group<T> extends Monoid<T> {

    /**
     * Returns the additive identity of the group
     */
    @Override
    public T additiveIdentity();

    /**
     * Returns the additive inverse of this group member
     */
    public T additiveInverse();

    /**
     * returns the result of this - other
     */
    public T minus(T other);

    /**
     * returns the result of this + other
     */
    @Override
    public T plus(T other);
}
