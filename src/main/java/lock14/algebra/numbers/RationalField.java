package lock14.algebra.numbers;

import java.util.Objects;
import lock14.algebra.structure.Field;

/**
 * The Field of Rational Numbers (Q, +, *).
 */
public final class RationalField implements Field<Rational> {

    public static final RationalField INSTANCE = new RationalField();

    private RationalField() {}

    @Override
    public Rational zero() {
        return Rational.ZERO;
    }

    @Override
    public Rational one() {
        return Rational.ONE;
    }

    @Override
    public Rational add(Rational a, Rational b) {
        return a.add(b);
    }

    @Override
    public Rational negate(Rational a) {
        return a.negate();
    }

    @Override
    public Rational subtract(Rational a, Rational b) {
        return a.subtract(b);
    }

    @Override
    public Rational multiply(Rational a, Rational b) {
        return a.multiply(b);
    }

    @Override
    public Rational reciprocal(Rational a) {
        return a.reciprocal();
    }

    @Override
    public Rational divide(Rational a, Rational b) {
        return a.divide(b);
    }

    @Override
    public boolean isZero(Rational element) {
        return element.isZero();
    }

    @Override
    public boolean isOne(Rational element) {
        return element.isOne();
    }
}
