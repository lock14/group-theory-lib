package lock14.algebra.numbers;

import lock14.algebra.structure.Field;

/**
 * The Exact Field of Rational Numbers {@code (Q, +, *)} over {@link Rational}.
 */
public final class Rationals implements Field<Rational> {

    /**
     * Singleton instance of the exact field of rational numbers {@code Q}.
     */
    public static final Rationals FIELD = new Rationals();

    private Rationals() {}

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

    @Override
    public String toString() {
        return "Q";
    }
}
