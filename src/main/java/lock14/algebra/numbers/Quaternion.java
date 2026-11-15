package lock14.algebra.numbers;

import java.util.Objects;
import java.util.Optional;
import lock14.algebra.element.RingElement;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.Field;

/**
 * Exact Hamilton Quaternion {@code w + x*i + y*j + z*k} parameterized over any {@link CommutativeRing} {@code T}
 * (for example, {@link Rationals#FIELD} for the exact rational division ring {@code H(Q)} or {@link Integers#RING}
 * for Lipschitz integer quaternions).
 *
 * @param <T>        scalar ring element type
 * @param w          the scalar (real) component
 * @param x          the {@code i} component
 * @param y          the {@code j} component
 * @param z          the {@code k} component
 * @param scalarRing the underlying commutative scalar ring
 */
public record Quaternion<T>(T w, T x, T y, T z, CommutativeRing<T> scalarRing)
    implements RingElement<Quaternion<T>> {

    /**
     * Validates that all quaternion components and {@code scalarRing} are non-null.
     *
     * @param w          the scalar (real) component
     * @param x          the {@code i} component
     * @param y          the {@code j} component
     * @param z          the {@code k} component
     * @param scalarRing the underlying commutative scalar ring
     */
    public Quaternion {
        Objects.requireNonNull(w, "w cannot be null");
        Objects.requireNonNull(x, "x cannot be null");
        Objects.requireNonNull(y, "y cannot be null");
        Objects.requireNonNull(z, "z cannot be null");
        Objects.requireNonNull(scalarRing, "scalarRing cannot be null");
    }

    /**
     * Constructs a quaternion {@code w + x*i + y*j + z*k} over {@code ring}.
     *
     * @param <T>  scalar ring element type
     * @param ring the underlying commutative scalar ring
     * @param w    the scalar (real) component
     * @param x    the {@code i} component
     * @param y    the {@code j} component
     * @param z    the {@code k} component
     * @return the quaternion {@code w + x*i + y*j + z*k}
     */
    public static <T> Quaternion<T> of(CommutativeRing<T> ring, T w, T x, T y, T z) {
        return new Quaternion<>(w, x, y, z, ring);
    }

    /**
     * Embeds a scalar {@code w + 0*i + 0*j + 0*k} into the quaternion algebra over {@code ring}.
     *
     * @param <T>  scalar ring element type
     * @param ring the underlying commutative scalar ring
     * @param w    the scalar component
     * @return the scalar quaternion {@code w}
     */
    public static <T> Quaternion<T> scalar(CommutativeRing<T> ring, T w) {
        T zero = ring.zero();
        return new Quaternion<>(w, zero, zero, zero, ring);
    }

    /**
     * Constructs an exact rational quaternion {@code w + x*i + y*j + z*k} in {@code H(Q)} from integer components.
     *
     * @param w the integer scalar component
     * @param x the integer {@code i} component
     * @param y the integer {@code j} component
     * @param z the integer {@code k} component
     * @return the rational quaternion {@code w + x*i + y*j + z*k}
     */
    public static Quaternion<Rational> ofRational(long w, long x, long y, long z) {
        return new Quaternion<>(Rational.of(w), Rational.of(x), Rational.of(y), Rational.of(z), Rationals.FIELD);
    }

    /**
     * Returns {@code true} if all four components are zero in {@code scalarRing}.
     *
     * @return {@code true} if this quaternion equals {@code 0}
     */
    public boolean isZero() {
        return scalarRing.isZero(w) && scalarRing.isZero(x) && scalarRing.isZero(y) && scalarRing.isZero(z);
    }

    /**
     * Returns {@code true} if {@code w} is one and {@code x, y, z} are zero in {@code scalarRing}.
     *
     * @return {@code true} if this quaternion equals {@code 1}
     */
    public boolean isOne() {
        return scalarRing.isOne(w) && scalarRing.isZero(x) && scalarRing.isZero(y) && scalarRing.isZero(z);
    }

    /**
     * Returns the Hamilton conjugate {@code w - x*i - y*j - z*k}.
     *
     * @return the conjugate quaternion
     */
    public Quaternion<T> conjugate() {
        return new Quaternion<>(
            w,
            scalarRing.negate(x),
            scalarRing.negate(y),
            scalarRing.negate(z),
            scalarRing
        );
    }

    /**
     * Computes the reduced norm (squared norm) {@code N(q) = w^2 + x^2 + y^2 + z^2} in {@code scalarRing}.
     *
     * @return the squared norm {@code w^2 + x^2 + y^2 + z^2}
     */
    public T normSq() {
        T ww = scalarRing.multiply(w, w);
        T xx = scalarRing.multiply(x, x);
        T yy = scalarRing.multiply(y, y);
        T zz = scalarRing.multiply(z, z);
        return scalarRing.add(scalarRing.add(ww, xx), scalarRing.add(yy, zz));
    }

    /**
     * Scales all four components of this quaternion by {@code scalar}.
     *
     * @param scalar the scalar multiplier in {@code scalarRing}
     * @return the scaled quaternion
     */
    public Quaternion<T> scale(T scalar) {
        Objects.requireNonNull(scalar, "scalar cannot be null");
        return new Quaternion<>(
            scalarRing.multiply(scalar, w),
            scalarRing.multiply(scalar, x),
            scalarRing.multiply(scalar, y),
            scalarRing.multiply(scalar, z),
            scalarRing
        );
    }

    @Override
    public Quaternion<T> add(Quaternion<T> o) {
        checkSameRing(o);
        return new Quaternion<>(
            scalarRing.add(w, o.w),
            scalarRing.add(x, o.x),
            scalarRing.add(y, o.y),
            scalarRing.add(z, o.z),
            scalarRing
        );
    }

    @Override
    public Quaternion<T> negate() {
        return new Quaternion<>(
            scalarRing.negate(w),
            scalarRing.negate(x),
            scalarRing.negate(y),
            scalarRing.negate(z),
            scalarRing
        );
    }

    @Override
    public Quaternion<T> subtract(Quaternion<T> o) {
        checkSameRing(o);
        return new Quaternion<>(
            scalarRing.subtract(w, o.w),
            scalarRing.subtract(x, o.x),
            scalarRing.subtract(y, o.y),
            scalarRing.subtract(z, o.z),
            scalarRing
        );
    }

    @Override
    public Quaternion<T> multiply(Quaternion<T> o) {
        checkSameRing(o);
        T newW = scalarRing.subtract(
            scalarRing.subtract(scalarRing.multiply(w, o.w), scalarRing.multiply(x, o.x)),
            scalarRing.add(scalarRing.multiply(y, o.y), scalarRing.multiply(z, o.z))
        );
        T newX = scalarRing.subtract(
            scalarRing.add(scalarRing.multiply(w, o.x), scalarRing.multiply(x, o.w)),
            scalarRing.subtract(scalarRing.multiply(z, o.y), scalarRing.multiply(y, o.z))
        );
        T newY = scalarRing.add(
            scalarRing.subtract(scalarRing.multiply(w, o.y), scalarRing.multiply(x, o.z)),
            scalarRing.add(scalarRing.multiply(y, o.w), scalarRing.multiply(z, o.x))
        );
        T newZ = scalarRing.add(
            scalarRing.add(scalarRing.multiply(w, o.z), scalarRing.multiply(x, o.y)),
            scalarRing.subtract(scalarRing.multiply(z, o.w), scalarRing.multiply(y, o.x))
        );
        return new Quaternion<>(newW, newX, newY, newZ, scalarRing);
    }

    /**
     * Computes the exact multiplicative inverse {@code conjugate() / normSq()} over {@code scalarField}.
     *
     * @param scalarField the governing scalar {@link Field} matching {@link #scalarRing()}
     * @return the multiplicative inverse {@code this^-1}
     * @throws IncompatibleAlgebraException  if {@code scalarField} does not match {@link #scalarRing()}
     * @throws NonInvertibleElementException if {@code normSq()} is zero in {@code scalarField}
     */
    public Quaternion<T> reciprocal(Field<T> scalarField) {
        return tryReciprocal(scalarField).orElseThrow(
            () -> new NonInvertibleElementException("Quaternion element is not invertible: " + this)
        );
    }

    /**
     * Attempts to compute the exact multiplicative inverse of this quaternion over {@code scalarField}.
     *
     * @param scalarField the governing scalar {@link Field} matching {@link #scalarRing()}
     * @return an {@link Optional} containing {@code this^-1} if invertible, or {@link Optional#empty()} if {@code normSq()} is zero
     * @throws IncompatibleAlgebraException if {@code scalarField} does not match {@link #scalarRing()}
     */
    public Optional<Quaternion<T>> tryReciprocal(Field<T> scalarField) {
        Objects.requireNonNull(scalarField, "scalarField cannot be null");
        if (!Objects.equals(this.scalarRing, scalarField)) {
            throw new IncompatibleAlgebraException(
                "Quaternion scalar ring " + this.scalarRing + " does not match field " + scalarField
            );
        }
        T nSq = normSq();
        if (scalarField.isZero(nSq)) {
            return Optional.empty();
        }
        T invNorm = scalarField.reciprocal(nSq);
        return Optional.of(conjugate().scale(invNorm));
    }

    private void checkSameRing(Quaternion<T> other) {
        Objects.requireNonNull(other, "other quaternion cannot be null");
        if (!Objects.equals(this.scalarRing, other.scalarRing)) {
            throw new IncompatibleAlgebraException(
                "Quaternion scalar rings do not match: " + this.scalarRing + " vs " + other.scalarRing
            );
        }
    }

    @Override
    public String toString() {
        return "(" + w + " + " + x + "i + " + y + "j + " + z + "k)";
    }
}
