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
 */
public record PolynomialRing<T>(CommutativeRing<T> coefficientRing) implements CommutativeRing<Polynomial<T>> {

    public PolynomialRing {
        Objects.requireNonNull(coefficientRing, "coefficientRing cannot be null");
    }

    public static <T> PolynomialEuclideanDomain<T> overField(Field<T> coefficientField) {
        return new PolynomialEuclideanDomain<>(coefficientField);
    }

    @SafeVarargs
    public final Polynomial<T> of(T... coeffs) {
        return Polynomial.of(coefficientRing, coeffs);
    }

    public Polynomial<T> of(List<T> coeffs) {
        return Polynomial.of(coefficientRing, coeffs);
    }

    public Polynomial<T> x() {
        return Polynomial.x(coefficientRing);
    }

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
