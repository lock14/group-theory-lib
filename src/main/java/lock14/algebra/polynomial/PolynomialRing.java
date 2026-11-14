package lock14.algebra.polynomial;

import java.util.List;
import java.util.Objects;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.Field;

/**
 * The Commutative Polynomial Ring {@code R[x]} over a {@link CommutativeRing} {@code R}.
 *
 * @param <T> coefficient element type
 * @param coefficientRing the underlying commutative ring of coefficients
 */
public record PolynomialRing<T>(CommutativeRing<T> coefficientRing) implements CommutativeRing<Polynomial<T>> {

    /**
     * Validates that the coefficient ring is non-null.
     *
     * @param coefficientRing the underlying commutative ring of coefficients
     */
    public PolynomialRing {
        Objects.requireNonNull(coefficientRing, "coefficientRing cannot be null");
    }

    /**
     * Constructs the Euclidean domain {@code F[x]} of univariate polynomials over a {@link Field} {@code F}.
     *
     * @param <T> the scalar field element type
     * @param coefficientField the field of scalar coefficients
     * @return the polynomial Euclidean domain {@code F[x]}
     */
    public static <T> PolynomialEuclideanDomain<T> overField(Field<T> coefficientField) {
        return new PolynomialEuclideanDomain<>(coefficientField);
    }

    /**
     * Constructs a polynomial in {@code R[x]} from ascending-degree coefficients.
     *
     * @param coeffs coefficients in ascending degree order {@code (a_0, a_1, ...)}
     * @return the canonical polynomial over {@link #coefficientRing()}
     */
    @SafeVarargs
    public final Polynomial<T> of(T... coeffs) {
        return Polynomial.of(coefficientRing, coeffs);
    }

    /**
     * Constructs a polynomial in {@code R[x]} from a list of ascending-degree coefficients.
     *
     * @param coeffs coefficients in ascending degree order {@code [a_0, a_1, ...]}
     * @return the canonical polynomial over {@link #coefficientRing()}
     */
    public Polynomial<T> of(List<T> coeffs) {
        return Polynomial.of(coefficientRing, coeffs);
    }

    /**
     * Returns the indeterminate polynomial {@code x} in {@code R[x]}.
     *
     * @return the linear monomial {@code x}
     */
    public Polynomial<T> x() {
        return Polynomial.x(coefficientRing);
    }

    /**
     * Embeds a scalar {@code c} as a constant polynomial in {@code R[x]}.
     *
     * @param c the scalar constant term
     * @return the constant polynomial {@code c}
     */
    public Polynomial<T> constant(T c) {
        return Polynomial.constant(coefficientRing, c);
    }

    @Override
    public Polynomial<T> zero() {
        return Polynomial.zero(coefficientRing);
    }

    @Override
    public Polynomial<T> one() {
        return Polynomial.one(coefficientRing);
    }

    @Override
    public Polynomial<T> add(Polynomial<T> a, Polynomial<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.add(b);
    }

    @Override
    public Polynomial<T> negate(Polynomial<T> a) {
        checkInRing(a);
        return a.negate();
    }

    @Override
    public Polynomial<T> subtract(Polynomial<T> a, Polynomial<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.subtract(b);
    }

    @Override
    public Polynomial<T> multiply(Polynomial<T> a, Polynomial<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.multiply(b);
    }

    @Override
    public boolean isZero(Polynomial<T> element) {
        checkInRing(element);
        return element.isZero();
    }

    @Override
    public boolean isOne(Polynomial<T> element) {
        checkInRing(element);
        return element.isOne();
    }

    private void checkInRing(Polynomial<T> p) {
        Objects.requireNonNull(p, "polynomial cannot be null");
        if (!Objects.equals(p.ring(), this.coefficientRing)) {
            throw new IncompatibleAlgebraException(
                "Polynomial coefficient ring " + p.ring() + " does not match " + this.coefficientRing
            );
        }
    }
}
