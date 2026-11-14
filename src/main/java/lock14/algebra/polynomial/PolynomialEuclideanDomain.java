package lock14.algebra.polynomial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.structure.EuclideanDomain;
import lock14.algebra.structure.Field;

/**
 * The Exact Euclidean Domain {@code F[x]} of univariate polynomials over a {@link Field} {@code F},
 * equipped with polynomial long division and monic canonical normalization.
 *
 * @param <T> scalar field element type
 * @param coefficientField the underlying field of scalar coefficients
 */
public record PolynomialEuclideanDomain<T>(Field<T> coefficientField) implements EuclideanDomain<Polynomial<T>> {

    /**
     * Validates that the scalar coefficient field is non-null.
     *
     * @param coefficientField the underlying field of scalar coefficients
     */
    public PolynomialEuclideanDomain {
        Objects.requireNonNull(coefficientField, "coefficientField cannot be null");
    }

    /**
     * Constructs a polynomial in {@code F[x]} from ascending-degree coefficients.
     *
     * @param coeffs coefficients in ascending degree order {@code (a_0, a_1, ...)}
     * @return the canonical polynomial over {@link #coefficientField()}
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public final Polynomial<T> of(T... coeffs) {
        return Polynomial.of(coefficientField, coeffs);
    }

    /**
     * Constructs a polynomial in {@code F[x]} from a list of ascending-degree coefficients.
     *
     * @param coeffs coefficients in ascending degree order {@code [a_0, a_1, ...]}
     * @return the canonical polynomial over {@link #coefficientField()}
     */
    public Polynomial<T> of(List<T> coeffs) {
        return Polynomial.of(coefficientField, coeffs);
    }

    /**
     * Returns the indeterminate polynomial {@code x} in {@code F[x]}.
     *
     * @return the linear monomial {@code x}
     */
    public Polynomial<T> x() {
        return Polynomial.x(coefficientField);
    }

    /**
     * Embeds a scalar {@code c} as a constant polynomial in {@code F[x]}.
     *
     * @param c the scalar constant term
     * @return the constant polynomial {@code c}
     */
    public Polynomial<T> constant(T c) {
        return Polynomial.constant(coefficientField, c);
    }

    @Override
    public Polynomial<T> zero() {
        return Polynomial.zero(coefficientField);
    }

    @Override
    public Polynomial<T> one() {
        return Polynomial.one(coefficientField);
    }

    @Override
    public Polynomial<T> add(Polynomial<T> a, Polynomial<T> b) {
        checkInDomain(a);
        checkInDomain(b);
        return a.add(b);
    }

    @Override
    public Polynomial<T> negate(Polynomial<T> a) {
        checkInDomain(a);
        return a.negate();
    }

    @Override
    public Polynomial<T> subtract(Polynomial<T> a, Polynomial<T> b) {
        checkInDomain(a);
        checkInDomain(b);
        return a.subtract(b);
    }

    @Override
    public Polynomial<T> multiply(Polynomial<T> a, Polynomial<T> b) {
        checkInDomain(a);
        checkInDomain(b);
        return a.multiply(b);
    }

    @Override
    public boolean isZero(Polynomial<T> element) {
        checkInDomain(element);
        return element.isZero();
    }

    @Override
    public boolean isOne(Polynomial<T> element) {
        checkInDomain(element);
        return element.isOne();
    }

    @Override
    public boolean isUnit(Polynomial<T> element) {
        checkInDomain(element);
        return element.degree() == 0;
    }

    @Override
    public Polynomial<T> normalize(Polynomial<T> element) {
        checkInDomain(element);
        if (element.isZero()) {
            return zero();
        }
        T lead = element.leadingCoefficient();
        if (coefficientField.isOne(lead)) {
            return element;
        }
        return element.scale(coefficientField.reciprocal(lead));
    }

    @Override
    public DivisionResult<Polynomial<T>> divideAndRemainder(Polynomial<T> a, Polynomial<T> b) {
        checkInDomain(a);
        checkInDomain(b);
        if (b.isZero()) {
            throw new NonInvertibleElementException("Polynomial division by zero");
        }
        if (a.isZero() || a.degree() < b.degree()) {
            return new DivisionResult<>(zero(), a);
        }

        int degA = a.degree();
        int degB = b.degree();
        List<T> rem = new ArrayList<>(a.coefficients());
        List<T> quot = new ArrayList<>(Collections.nCopies(degA - degB + 1, coefficientField.zero()));
        T invLeadB = coefficientField.reciprocal(b.leadingCoefficient());

        for (int shift = degA - degB; shift >= 0; shift--) {
            T topCoeff = rem.get(shift + degB);
            if (coefficientField.isZero(topCoeff)) {
                continue;
            }
            T qCoeff = coefficientField.multiply(topCoeff, invLeadB);
            quot.set(shift, qCoeff);
            for (int j = 0; j <= degB; j++) {
                int idx = shift + j;
                T sub = coefficientField.multiply(qCoeff, b.coefficient(j));
                rem.set(idx, coefficientField.subtract(rem.get(idx), sub));
            }
        }

        return new DivisionResult<>(
            Polynomial.canonicalize(quot, coefficientField),
            Polynomial.canonicalize(rem, coefficientField)
        );
    }

    private void checkInDomain(Polynomial<T> p) {
        Objects.requireNonNull(p, "polynomial cannot be null");
        if (!Objects.equals(p.ring(), this.coefficientField)) {
            throw new IncompatibleAlgebraException(
                "Polynomial coefficient ring " + p.ring() + " does not match " + this.coefficientField
            );
        }
    }
}
