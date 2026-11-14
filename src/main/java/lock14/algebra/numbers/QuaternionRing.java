package lock14.algebra.numbers;

import java.util.Objects;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.Ring;

/**
 * The Non-Commutative Quaternion Algebra {@code H(R)} over any {@link CommutativeRing} {@code R}.
 *
 * @param <T> scalar ring element type
 */
public record QuaternionRing<T>(CommutativeRing<T> scalarRing) implements Ring<Quaternion<T>> {

    public QuaternionRing {
        Objects.requireNonNull(scalarRing, "scalarRing cannot be null");
    }

    public Quaternion<T> of(T w, T x, T y, T z) {
        return Quaternion.of(scalarRing, w, x, y, z);
    }

    public Quaternion<T> i() {
        return Quaternion.of(scalarRing, scalarRing.zero(), scalarRing.one(), scalarRing.zero(), scalarRing.zero());
    }

    public Quaternion<T> j() {
        return Quaternion.of(scalarRing, scalarRing.zero(), scalarRing.zero(), scalarRing.one(), scalarRing.zero());
    }

    public Quaternion<T> k() {
        return Quaternion.of(scalarRing, scalarRing.zero(), scalarRing.zero(), scalarRing.zero(), scalarRing.one());
    }

    @Override
    public Quaternion<T> zero() {
        T z = scalarRing.zero();
        return new Quaternion<>(z, z, z, z, scalarRing);
    }

    @Override
    public Quaternion<T> one() {
        T z = scalarRing.zero();
        return new Quaternion<>(scalarRing.one(), z, z, z, scalarRing);
    }

    @Override
    public Quaternion<T> add(Quaternion<T> a, Quaternion<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.add(b);
    }

    @Override
    public Quaternion<T> negate(Quaternion<T> a) {
        checkInRing(a);
        return a.negate();
    }

    @Override
    public Quaternion<T> subtract(Quaternion<T> a, Quaternion<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.subtract(b);
    }

    @Override
    public Quaternion<T> multiply(Quaternion<T> a, Quaternion<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.multiply(b);
    }

    @Override
    public boolean isZero(Quaternion<T> element) {
        checkInRing(element);
        return element.isZero();
    }

    @Override
    public boolean isOne(Quaternion<T> element) {
        checkInRing(element);
        return element.isOne();
    }

    public boolean isUnit(Quaternion<T> element) {
        checkInRing(element);
        return element.tryReciprocal().isPresent();
    }

    private void checkInRing(Quaternion<T> q) {
        Objects.requireNonNull(q, "quaternion cannot be null");
        if (!Objects.equals(q.scalarRing(), this.scalarRing)) {
            throw new IncompatibleAlgebraException("Quaternion scalar ring mismatch");
        }
    }
}
