package lock14.algebra.numbers;

import java.util.Objects;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.CommutativeRing;

/**
 * The Commutative Ring {@code R[i] = R[x]/(x^2 + 1)} over any {@link CommutativeRing} {@code R}.
 *
 * @param <T>        scalar ring element type
 * @param scalarRing the underlying commutative scalar ring {@code R}
 */
public record ComplexRing<T>(CommutativeRing<T> scalarRing) implements CommutativeRing<Complex<T>> {

    /**
     * Validates that {@code scalarRing} is non-null.
     *
     * @param scalarRing the underlying commutative scalar ring
     */
    public ComplexRing {
        Objects.requireNonNull(scalarRing, "scalarRing cannot be null");
    }

    /**
     * Constructs an element {@code real + imaginary * i} in this complex ring.
     *
     * @param real      the real part
     * @param imaginary the imaginary part
     * @return the complex element {@code real + imaginary * i}
     */
    public Complex<T> of(T real, T imaginary) {
        return Complex.of(scalarRing, real, imaginary);
    }

    /**
     * Returns the imaginary unit {@code i = 0 + 1*i} in this complex ring.
     *
     * @return the imaginary unit {@code i}
     */
    public Complex<T> i() {
        return Complex.imaginary(scalarRing, scalarRing.one());
    }

    @Override
    public Complex<T> zero() {
        return new Complex<>(scalarRing.zero(), scalarRing.zero(), scalarRing);
    }

    @Override
    public Complex<T> one() {
        return new Complex<>(scalarRing.one(), scalarRing.zero(), scalarRing);
    }

    @Override
    public Complex<T> add(Complex<T> a, Complex<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.add(b);
    }

    @Override
    public Complex<T> negate(Complex<T> a) {
        checkInRing(a);
        return a.negate();
    }

    @Override
    public Complex<T> subtract(Complex<T> a, Complex<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.subtract(b);
    }

    @Override
    public Complex<T> multiply(Complex<T> a, Complex<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.multiply(b);
    }

    @Override
    public boolean isZero(Complex<T> element) {
        checkInRing(element);
        return element.isZero();
    }

    @Override
    public boolean isOne(Complex<T> element) {
        checkInRing(element);
        return element.isOne();
    }

    private void checkInRing(Complex<T> c) {
        Objects.requireNonNull(c, "complex element cannot be null");
        if (!Objects.equals(c.scalarRing(), this.scalarRing)) {
            throw new IncompatibleAlgebraException(
                "Complex scalar ring " + c.scalarRing() + " does not match " + this.scalarRing
            );
        }
    }
}
