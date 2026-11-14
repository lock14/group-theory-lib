package lock14.algebra.discrete;

import java.util.Objects;
import lock14.algebra.structure.Ring;

/**
 * The univariate Polynomial Ring R[x] over a general (potentially non-commutative) base {@link Ring} R.
 *
 * @param <T> the coefficient type
 */
public record GeneralPolynomialRing<T>(Ring<T> coefficientRing) implements Ring<Polynomial<T>> {

    public GeneralPolynomialRing {
        Objects.requireNonNull(coefficientRing, "coefficientRing cannot be null");
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
            throw new IllegalArgumentException(
                "Polynomial coefficient ring " + p.ring() + " does not match " + this.coefficientRing
            );
        }
    }
}
