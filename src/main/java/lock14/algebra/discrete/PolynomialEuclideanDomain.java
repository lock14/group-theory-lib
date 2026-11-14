package lock14.algebra.discrete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lock14.algebra.structure.EuclideanDomain;
import lock14.algebra.structure.Field;

/**
 * The Euclidean Domain F[x] of univariate polynomials over a coefficient {@link Field} F,
 * equipped with polynomial degree as the Euclidean function, polynomial long division,
 * and monic canonical normalization.
 *
 * @param <T> the scalar field element type
 */
public record PolynomialEuclideanDomain<T>(Field<T> coefficientField) implements EuclideanDomain<Polynomial<T>> {

    public PolynomialEuclideanDomain {
        Objects.requireNonNull(coefficientField, "coefficientField cannot be null");
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
        T invLead = coefficientField.reciprocal(lead);
        List<T> monicCoeffs = new ArrayList<>(element.coefficients().size());
        for (T c : element.coefficients()) {
            monicCoeffs.add(coefficientField.multiply(invLead, c));
        }
        return new Polynomial<>(monicCoeffs, coefficientField);
    }

    @Override
    public DivisionResult<Polynomial<T>> divideAndRemainder(Polynomial<T> a, Polynomial<T> b) {
        checkInDomain(a);
        checkInDomain(b);
        if (b.isZero()) {
            throw new ArithmeticException("Polynomial division by zero");
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
            new Polynomial<>(quot, coefficientField),
            new Polynomial<>(rem, coefficientField)
        );
    }

    private void checkInDomain(Polynomial<T> p) {
        Objects.requireNonNull(p, "polynomial cannot be null");
        if (!Objects.equals(p.ring(), this.coefficientField)) {
            throw new IllegalArgumentException(
                "Polynomial coefficient ring " + p.ring() + " does not match " + this.coefficientField
            );
        }
    }
}
