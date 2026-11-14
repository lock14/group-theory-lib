package lock14.algebra.numbers;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import lock14.algebra.element.RingElement;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.exceptions.UndefinedOperationException;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.Field;

/**
 * Exact quadratic hypercomplex element {@code a + b*i} ({@code i^2 = -1}) parameterized over any
 * {@link CommutativeRing} {@code T} (such as {@link Integers#RING} for Gaussian integers {@code Z[i]}
 * or {@link Rationals#FIELD} for Gaussian rationals {@code Q(i)}).
 *
 * @param <T> the scalar ring element type
 */
public record Complex<T>(T real, T imaginary, CommutativeRing<T> scalarRing) implements RingElement<Complex<T>> {

    public Complex {
        Objects.requireNonNull(real, "real cannot be null");
        Objects.requireNonNull(imaginary, "imaginary cannot be null");
        Objects.requireNonNull(scalarRing, "scalarRing cannot be null");
    }

    public static <T> Complex<T> of(CommutativeRing<T> ring, T real, T imaginary) {
        return new Complex<>(real, imaginary, ring);
    }

    public static <T> Complex<T> real(CommutativeRing<T> ring, T real) {
        return new Complex<>(real, ring.zero(), ring);
    }

    public static <T> Complex<T> imaginary(CommutativeRing<T> ring, T imaginary) {
        return new Complex<>(ring.zero(), imaginary, ring);
    }

    public static Complex<BigInteger> gaussian(long real, long imaginary) {
        return new Complex<>(BigInteger.valueOf(real), BigInteger.valueOf(imaginary), Integers.RING);
    }

    public static Complex<BigInteger> gaussian(BigInteger real, BigInteger imaginary) {
        return new Complex<>(real, imaginary, Integers.RING);
    }

    public static Complex<Rational> ofRational(Rational real, Rational imaginary) {
        return new Complex<>(real, imaginary, Rationals.FIELD);
    }

    public static Complex<Rational> ofRational(long real, long imaginary) {
        return new Complex<>(Rational.of(real), Rational.of(imaginary), Rationals.FIELD);
    }

    public boolean isZero() {
        return scalarRing.isZero(real) && scalarRing.isZero(imaginary);
    }

    public boolean isOne() {
        return scalarRing.isOne(real) && scalarRing.isZero(imaginary);
    }

    public Complex<T> conjugate() {
        return new Complex<>(real, scalarRing.negate(imaginary), scalarRing);
    }

    /**
     * Algebraic norm {@code N(a + bi) = a^2 + b^2} in {@code scalarRing}.
     */
    public T normSq() {
        return scalarRing.add(
            scalarRing.multiply(real, real),
            scalarRing.multiply(imaginary, imaginary)
        );
    }

    public Complex<T> scale(T scalar) {
        Objects.requireNonNull(scalar, "scalar cannot be null");
        return new Complex<>(
            scalarRing.multiply(scalar, real),
            scalarRing.multiply(scalar, imaginary),
            scalarRing
        );
    }

    @Override
    public Complex<T> add(Complex<T> other) {
        checkSameRing(other);
        return new Complex<>(
            scalarRing.add(this.real, other.real),
            scalarRing.add(this.imaginary, other.imaginary),
            scalarRing
        );
    }

    @Override
    public Complex<T> negate() {
        return new Complex<>(
            scalarRing.negate(real),
            scalarRing.negate(imaginary),
            scalarRing
        );
    }

    @Override
    public Complex<T> subtract(Complex<T> other) {
        checkSameRing(other);
        return new Complex<>(
            scalarRing.subtract(this.real, other.real),
            scalarRing.subtract(this.imaginary, other.imaginary),
            scalarRing
        );
    }

    @Override
    public Complex<T> multiply(Complex<T> other) {
        checkSameRing(other);
        T re = scalarRing.subtract(
            scalarRing.multiply(this.real, other.real),
            scalarRing.multiply(this.imaginary, other.imaginary)
        );
        T im = scalarRing.add(
            scalarRing.multiply(this.real, other.imaginary),
            scalarRing.multiply(this.imaginary, other.real)
        );
        return new Complex<>(re, im, scalarRing);
    }

    /**
     * Computes the exact multiplicative inverse {@code conjugate() / normSq()} when {@code scalarRing}
     * is a {@link Field} (or {@code normSq()} is a unit).
     */
    public Complex<T> reciprocal() {
        return tryReciprocal().orElseThrow(
            () -> new NonInvertibleElementException("Complex element is not invertible: " + this)
        );
    }

    public Optional<Complex<T>> tryReciprocal() {
        T nSq = normSq();
        if (scalarRing instanceof Field<T> field) {
            if (field.isZero(nSq)) {
                return Optional.empty();
            }
            T invNorm = field.reciprocal(nSq);
            return Optional.of(new Complex<>(
                field.multiply(real, invNorm),
                field.multiply(field.negate(imaginary), invNorm),
                scalarRing
            ));
        }
        if (scalarRing.isOne(nSq)) {
            return Optional.of(conjugate());
        }
        throw new UndefinedOperationException(
            "General reciprocal requires a scalar Field; use GaussianIntegers or ComplexRing for ring operations"
        );
    }

    private void checkSameRing(Complex<T> other) {
        Objects.requireNonNull(other, "other cannot be null");
        if (!Objects.equals(this.scalarRing, other.scalarRing)) {
            throw new IncompatibleAlgebraException(
                "Complex scalar rings do not match: " + this.scalarRing + " vs " + other.scalarRing
            );
        }
    }

    @Override
    public String toString() {
        if (scalarRing.isZero(imaginary)) {
            return real.toString();
        }
        if (scalarRing.isZero(real)) {
            return scalarRing.isOne(imaginary) ? "i" : imaginary + "i";
        }
        return real + " + " + imaginary + "i";
    }
}
