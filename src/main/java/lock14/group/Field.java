package lock14.group;

/**
 * A field is a commutative ring with the additional property:
 *
 * For any x of type T, there exists a multiplicative inverse y of type T such that
 * x.times(y) = y.times(x) = f. As a consequence, a new operation 'divide' can
 * be defined as x.divide(y) = x.times(y.multiplicativeInverse())
 */
public interface Field<T> extends CommutativeRing<T> {

    public T divide(T other);

    public T multiplicativeInverse();
}
