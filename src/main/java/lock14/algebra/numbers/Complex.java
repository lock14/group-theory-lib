package lock14.algebra.numbers;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import lock14.algebra.element.RingElement;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.Field;

/**
 * Exact quadratic hypercomplex element {@code a + b*i} ({@code i^2 = -1}) parameterized over any
 * {@link CommutativeRing} {@code T} (such as {@link Integers#RING} for Gaussian integers {@code Z[i]}
 * or {@link Rationals#FIELD} for Gaussian rationals {@code Q(i)}).
 *
 * @param <T>        the scalar ring element type
 * @param real       the real component {@code a}
 * @param imaginary  the imaginary component {@code b}
 * @param scalarRing the underlying commutative scalar ring
 */
public record Complex<T>(T real, T imaginary, CommutativeRing<T> scalarRing) implements RingElement<Complex<T>> {

    /**
     * Validates that the components and scalar ring are non-null.
     *
     * @param real       the real component
     * @param imaginary  the imaginary component
     * @param scalarRing the underlying commutative scalar ring
     */
    public Complex {
        Objects.requireNonNull(real, "real cannot be null");
        Objects.requireNonNull(imaginary, "imaginary cannot be null");
        Objects.requireNonNull(scalarRing, "scalarRing cannot be null");
    }

    /**
     * Constructs a complex element {@code real + imaginary * i} over {@code ring}.
     *
     * @param <T>       the scalar ring element type
     * @param ring      the scalar commutative ring
     * @param real      the real component
     * @param imaginary the imaginary component
     * @return the complex element {@code real + imaginary * i}
     */
    public static <T> Complex<T> of(CommutativeRing<T> ring, T real, T imaginary) {
        return new Complex<>(real, imaginary, ring);
    }

    /**
     * Embeds a purely real scalar {@code real + 0 * i} into the complex extension over {@code ring}.
     *
     * @param <T>  the scalar ring element type
     * @param ring the scalar commutative ring
     * @param real the real component
     * @return the purely real complex element {@code real + 0 * i}
     */
    public static <T> Complex<T> real(CommutativeRing<T> ring, T real) {
        return new Complex<>(real, ring.zero(), ring);
    }

    /**
     * Constructs a purely imaginary element {@code 0 + imaginary * i} over {@code ring}.
     *
     * @param <T>       the scalar ring element type
     * @param ring      the scalar commutative ring
     * @param imaginary the imaginary component
     * @return the purely imaginary complex element {@code imaginary * i}
     */
    public static <T> Complex<T> imaginary(CommutativeRing<T> ring, T imaginary) {
        return new Complex<>(ring.zero(), imaginary, ring);
    }

    /**
     * Constructs a Gaussian integer {@code real + imaginary * i} in {@code Z[i]}.
     *
     * @param real      the integer real part
     * @param imaginary the integer imaginary part
     * @return the Gaussian integer {@code real + imaginary * i}
     */
    public static Complex<BigInteger> gaussian(long real, long imaginary) {
        return new Complex<>(BigInteger.valueOf(real), BigInteger.valueOf(imaginary), Integers.RING);
    }

    /**
     * Constructs a Gaussian integer {@code real + imaginary * i} in {@code Z[i]}.
     *
     * @param real      the arbitrary-precision integer real part
     * @param imaginary the arbitrary-precision integer imaginary part
     * @return the Gaussian integer {@code real + imaginary * i}
     */
    public static Complex<BigInteger> gaussian(BigInteger real, BigInteger imaginary) {
        return new Complex<>(real, imaginary, Integers.RING);
    }

    /**
     * Constructs a Gaussian rational {@code real + imaginary * i} in {@code Q(i)}.
     *
     * @param real      the rational real part
     * @param imaginary the rational imaginary part
     * @return the Gaussian rational {@code real + imaginary * i}
     */
    public static Complex<Rational> ofRational(Rational real, Rational imaginary) {
        return new Complex<>(real, imaginary, Rationals.FIELD);
    }

    /**
     * Constructs a Gaussian rational {@code real + imaginary * i} in {@code Q(i)} from integer parts.
     *
     * @param real      the integer real part
     * @param imaginary the integer imaginary part
     * @return the Gaussian rational {@code real + imaginary * i}
     */
    public static Complex<Rational> ofRational(long real, long imaginary) {
        return new Complex<>(Rational.of(real), Rational.of(imaginary), Rationals.FIELD);
    }

    /**
     * Returns {@code true} if both real and imaginary components are zero in {@code scalarRing}.
     *
     * @return {@code true} if this element equals {@code 0}
     */
    public boolean isZero() {
        return scalarRing.isZero(real) && scalarRing.isZero(imaginary);
    }

    /**
     * Returns {@code true} if the real component is one and the imaginary component is zero in {@code scalarRing}.
     *
     * @return {@code true} if this element equals {@code 1}
     */
    public boolean isOne() {
        return scalarRing.isOne(real) && scalarRing.isZero(imaginary);
    }

    /**
     * Returns the complex conjugate {@code a - b*i}.
     *
     * @return the conjugate complex element
     */
    public Complex<T> conjugate() {
        return new Complex<>(real, scalarRing.negate(imaginary), scalarRing);
    }

    /**
     * Algebraic norm {@code N(a + bi) = a^2 + b^2} in {@code scalarRing}.
     *
     * @return the squared norm {@code real^2 + imaginary^2}
     */
    public T normSq() {
        return scalarRing.add(
            scalarRing.multiply(real, real),
            scalarRing.multiply(imaginary, imaginary)
        );
    }

    /**
     * Scales both components of this complex element by {@code scalar}.
     *
     * @param scalar the scalar multiplier in {@code scalarRing}
     * @return the scaled complex element {@code (scalar * real) + (scalar * imaginary) * i}
     */
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
     * Computes the exact multiplicative inverse {@code conjugate() / normSq()} over {@code scalarField}.
     *
     * @param scalarField the governing scalar {@link Field} matching {@link #scalarRing()}
     * @return the multiplicative inverse {@code this^-1}
     * @throws IncompatibleAlgebraException  if {@code scalarField} does not match {@link #scalarRing()}
     * @throws NonInvertibleElementException if {@code normSq()} is zero in {@code scalarField}
     */
    public Complex<T> reciprocal(Field<T> scalarField) {
        return tryReciprocal(scalarField).orElseThrow(
            () -> new NonInvertibleElementException("Complex element is not invertible: " + this)
        );
    }

    /**
     * Attempts to compute the exact multiplicative inverse of this complex element over {@code scalarField}.
     *
     * @param scalarField the governing scalar {@link Field} matching {@link #scalarRing()}
     * @return an {@link Optional} containing {@code this^-1} if invertible, or {@link Optional#empty()} if {@code normSq()} is zero
     * @throws IncompatibleAlgebraException if {@code scalarField} does not match {@link #scalarRing()}
     */
    public Optional<Complex<T>> tryReciprocal(Field<T> scalarField) {
        Objects.requireNonNull(scalarField, "scalarField cannot be null");
        if (!Objects.equals(this.scalarRing, scalarField)) {
            throw new IncompatibleAlgebraException(
                "Complex scalar ring " + this.scalarRing + " does not match field " + scalarField
            );
        }
        T nSq = normSq();
        if (scalarField.isZero(nSq)) {
            return Optional.empty();
        }
        T invNorm = scalarField.reciprocal(nSq);
        return Optional.of(new Complex<>(
            scalarField.multiply(real, invNorm),
            scalarField.multiply(scalarField.negate(imaginary), invNorm),
            scalarRing
        ));
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
