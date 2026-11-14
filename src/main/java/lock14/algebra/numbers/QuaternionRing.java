package lock14.algebra.numbers;

import java.util.Objects;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.Ring;

/**
 * The Non-Commutative Quaternion Algebra {@code H(R)} over any {@link CommutativeRing} {@code R}.
 *
 * @param <T>        scalar ring element type
 * @param scalarRing the underlying commutative scalar ring {@code R}
 */
public record QuaternionRing<T>(CommutativeRing<T> scalarRing) implements Ring<Quaternion<T>> {

    /**
     * Validates that {@code scalarRing} is non-null.
     *
     * @param scalarRing the underlying commutative scalar ring
     */
    public QuaternionRing {
        Objects.requireNonNull(scalarRing, "scalarRing cannot be null");
    }

    /**
     * Constructs a quaternion {@code w + x*i + y*j + z*k} in this quaternion ring.
     *
     * @param w the scalar component
     * @param x the {@code i} component
     * @param y the {@code j} component
     * @param z the {@code k} component
     * @return the quaternion {@code w + x*i + y*j + z*k}
     */
    public Quaternion<T> of(T w, T x, T y, T z) {
        return Quaternion.of(scalarRing, w, x, y, z);
    }

    /**
     * Returns the basis unit {@code i = 0 + 1*i + 0*j + 0*k}.
     *
     * @return the quaternion basis unit {@code i}
     */
    public Quaternion<T> i() {
        return Quaternion.of(scalarRing, scalarRing.zero(), scalarRing.one(), scalarRing.zero(), scalarRing.zero());
    }

    /**
     * Returns the basis unit {@code j = 0 + 0*i + 1*j + 0*k}.
     *
     * @return the quaternion basis unit {@code j}
     */
    public Quaternion<T> j() {
        return Quaternion.of(scalarRing, scalarRing.zero(), scalarRing.zero(), scalarRing.one(), scalarRing.zero());
    }

    /**
     * Returns the basis unit {@code k = 0 + 0*i + 0*j + 1*k}.
     *
     * @return the quaternion basis unit {@code k}
     */
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

    /**
     * Returns {@code true} if {@code element} has a multiplicative inverse in this quaternion ring.
     *
     * @param element the quaternion element to test
     * @return {@code true} if {@code element} is invertible
     */
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
