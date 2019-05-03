package lock14.group;

public interface Monoid<T> extends SemiGroup<T> {

    public T additiveIdentity();
}
