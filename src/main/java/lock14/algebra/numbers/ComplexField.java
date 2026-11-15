package lock14.algebra.numbers;

import java.util.Objects;
import java.util.Optional;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.Field;

/**
 * The Quadratic Extension Field {@code F(i) = F[x]/(x^2 + 1)} over any {@link Field} {@code F}
 * in which {@code -1} is not a square (such as {@link Rationals#FIELD} {@code Q(i)} or {@code F_p} with {@code p = 3 mod 4}).
 *
 * @param <T>         scalar field element type
 * @param scalarField the underlying scalar field {@code F}
 */
public record ComplexField<T>(Field<T> scalarField) implements Field<Complex<T>> {

    /**
     * Validates that {@code scalarField} is non-null.
     *
     * @param scalarField the underlying scalar field
     */
    public ComplexField {
        Objects.requireNonNull(scalarField, "scalarField cannot be null");
    }

    /**
     * Returns the exact Gaussian rational field {@code Q(i)} over {@link Rationals#FIELD}.
     *
     * @return the field {@code Q(i)}
     */
    public static ComplexField<Rational> overRationals() {
        return new ComplexField<>(Rationals.FIELD);
    }

    /**
     * Constructs an element {@code real + imaginary * i} in this complex field.
     *
     * @param real      the real part
     * @param imaginary the imaginary part
     * @return the complex element {@code real + imaginary * i}
     */
    public Complex<T> of(T real, T imaginary) {
        return Complex.of(scalarField, real, imaginary);
    }

    /**
     * Returns the imaginary unit {@code i = 0 + 1*i} in this complex field.
     *
     * @return the imaginary unit {@code i}
     */
    public Complex<T> i() {
        return Complex.imaginary(scalarField, scalarField.one());
    }

    @Override
    public Complex<T> zero() {
        return new Complex<>(scalarField.zero(), scalarField.zero(), scalarField);
    }

    @Override
    public Complex<T> one() {
        return new Complex<>(scalarField.one(), scalarField.zero(), scalarField);
    }

    @Override
    public Complex<T> add(Complex<T> a, Complex<T> b) {
        checkInField(a);
        checkInField(b);
        return a.add(b);
    }

    @Override
    public Complex<T> negate(Complex<T> a) {
        checkInField(a);
        return a.negate();
    }

    @Override
    public Complex<T> subtract(Complex<T> a, Complex<T> b) {
        checkInField(a);
        checkInField(b);
        return a.subtract(b);
    }

    @Override
    public Complex<T> multiply(Complex<T> a, Complex<T> b) {
        checkInField(a);
        checkInField(b);
        return a.multiply(b);
    }

    @Override
    public Optional<Complex<T>> tryReciprocal(Complex<T> a) {
        checkInField(a);
        return a.tryReciprocal(scalarField);
    }

    @Override
    public Complex<T> reciprocal(Complex<T> a) {
        checkInField(a);
        return a.reciprocal(scalarField);
    }

    @Override
    public boolean isZero(Complex<T> element) {
        checkInField(element);
        return element.isZero();
    }

    @Override
    public boolean isOne(Complex<T> element) {
        checkInField(element);
        return element.isOne();
    }

    private void checkInField(Complex<T> c) {
        Objects.requireNonNull(c, "complex element cannot be null");
        if (!Objects.equals(c.scalarRing(), this.scalarField)) {
            throw new IncompatibleAlgebraException(
                "Complex scalar ring " + c.scalarRing() + " does not match field " + this.scalarField
            );
        }
    }
}
