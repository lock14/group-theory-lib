package lock14.algebra.numbers;

import java.util.Objects;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.DivisionRing;
import lock14.algebra.structure.Field;

/**
 * The Exact Non-Commutative Division Ring {@code H(F)} of Hamilton Quaternions over a formally real
 * {@link Field} {@code F} (such as {@link Rationals#FIELD} {@code H(Q)}).
 *
 * @param <T>         scalar field element type
 * @param scalarField the underlying formally real scalar field {@code F}
 */
public record QuaternionDivisionRing<T>(Field<T> scalarField) implements DivisionRing<Quaternion<T>> {

    /**
     * Validates that {@code scalarField} is non-null.
     *
     * @param scalarField the underlying scalar field
     */
    public QuaternionDivisionRing {
        Objects.requireNonNull(scalarField, "scalarField cannot be null");
    }

    /**
     * Returns the exact rational quaternion division ring {@code H(Q)} over {@link Rationals#FIELD}.
     *
     * @return the division ring {@code H(Q)}
     */
    public static QuaternionDivisionRing<Rational> overRationals() {
        return new QuaternionDivisionRing<>(Rationals.FIELD);
    }

    /**
     * Constructs a quaternion {@code w + x*i + y*j + z*k} in this division ring.
     *
     * @param w the scalar component
     * @param x the {@code i} component
     * @param y the {@code j} component
     * @param z the {@code k} component
     * @return the quaternion {@code w + x*i + y*j + z*k}
     */
    public Quaternion<T> of(T w, T x, T y, T z) {
        return Quaternion.of(scalarField, w, x, y, z);
    }

    /**
     * Returns the basis unit {@code i = 0 + 1*i + 0*j + 0*k}.
     *
     * @return the quaternion basis unit {@code i}
     */
    public Quaternion<T> i() {
        return Quaternion.of(scalarField, scalarField.zero(), scalarField.one(), scalarField.zero(), scalarField.zero());
    }

    /**
     * Returns the basis unit {@code j = 0 + 0*i + 1*j + 0*k}.
     *
     * @return the quaternion basis unit {@code j}
     */
    public Quaternion<T> j() {
        return Quaternion.of(scalarField, scalarField.zero(), scalarField.zero(), scalarField.one(), scalarField.zero());
    }

    /**
     * Returns the basis unit {@code k = 0 + 0*i + 0*j + 1*k}.
     *
     * @return the quaternion basis unit {@code k}
     */
    public Quaternion<T> k() {
        return Quaternion.of(scalarField, scalarField.zero(), scalarField.zero(), scalarField.zero(), scalarField.one());
    }

    @Override
    public Quaternion<T> zero() {
        T z = scalarField.zero();
        return new Quaternion<>(z, z, z, z, scalarField);
    }

    @Override
    public Quaternion<T> one() {
        T z = scalarField.zero();
        return new Quaternion<>(scalarField.one(), z, z, z, scalarField);
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
    public Quaternion<T> reciprocal(Quaternion<T> a) {
        checkInRing(a);
        return a.reciprocal();
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

    private void checkInRing(Quaternion<T> q) {
        Objects.requireNonNull(q, "quaternion cannot be null");
        if (!Objects.equals(q.scalarRing(), this.scalarField)) {
            throw new IncompatibleAlgebraException("Quaternion scalar field mismatch");
        }
    }
}
