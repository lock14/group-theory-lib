package lock14.algebra.discrete;

import java.util.Objects;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.Field;
import lock14.algebra.structure.Ring;

/**
 * The Polynomial Ring R[x] over a base Ring R.
 *
 * @param <T> the coefficient type
 */
public class PolynomialRing<T> implements CommutativeRing<Polynomial<T>> {

    private final Ring<T> coefficientRing;

    public PolynomialRing(Ring<T> coefficientRing) {
        this.coefficientRing = Objects.requireNonNull(coefficientRing, "coefficientRing cannot be null");
    }

    public Ring<T> coefficientRing() {
        return coefficientRing;
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
        return a.add(b);
    }

    @Override
    public Polynomial<T> negate(Polynomial<T> a) {
        return a.negate();
    }

    @Override
    public Polynomial<T> subtract(Polynomial<T> a, Polynomial<T> b) {
        return a.subtract(b);
    }

    @Override
    public Polynomial<T> multiply(Polynomial<T> a, Polynomial<T> b) {
        return a.multiply(b);
    }

    @Override
    public boolean isZero(Polynomial<T> element) {
        return element.isZero();
    }

    @Override
    public boolean isOne(Polynomial<T> element) {
        return element.isOne();
    }
}
