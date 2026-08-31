package lock14.algebra.numbers;

import java.math.BigInteger;
import java.util.Objects;
import lock14.algebra.element.FieldElement;
import lock14.algebra.structure.Field;

/**
 * An exact, immutable rational number (fraction) backed by {@link BigInteger}.
 * Fractions are always maintained in canonical reduced form (irreducible with positive denominator).
 */
public record Rational(BigInteger numerator, BigInteger denominator) implements FieldElement<Rational>, Comparable<Rational> {

    public static final Rational ZERO = new Rational(BigInteger.ZERO, BigInteger.ONE);
    public static final Rational ONE = new Rational(BigInteger.ONE, BigInteger.ONE);
    public static final Rational TWO = new Rational(BigInteger.TWO, BigInteger.ONE);
    public static final Rational TEN = new Rational(BigInteger.TEN, BigInteger.ONE);

    public Rational {
        Objects.requireNonNull(numerator, "numerator cannot be null");
        Objects.requireNonNull(denominator, "denominator cannot be null");
        if (denominator.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Denominator cannot be zero");
        }
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
        if (numerator.equals(BigInteger.ZERO)) {
            denominator = BigInteger.ONE;
        } else {
            BigInteger gcd = numerator.gcd(denominator);
            if (!gcd.equals(BigInteger.ONE)) {
                numerator = numerator.divide(gcd);
                denominator = denominator.divide(gcd);
            }
        }
    }

    public static Rational of(long numerator, long denominator) {
        return new Rational(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    public static Rational of(long integer) {
        return new Rational(BigInteger.valueOf(integer), BigInteger.ONE);
    }

    public static Rational of(BigInteger numerator, BigInteger denominator) {
        return new Rational(numerator, denominator);
    }

    public static Rational of(BigInteger integer) {
        return new Rational(integer, BigInteger.ONE);
    }

    public static Field<Rational> field() {
        return RationalField.INSTANCE;
    }

    public boolean isZero() {
        return numerator.equals(BigInteger.ZERO);
    }

    public boolean isOne() {
        return numerator.equals(BigInteger.ONE) && denominator.equals(BigInteger.ONE);
    }

    public int signum() {
        return numerator.signum();
    }

    public Rational abs() {
        return signum() < 0 ? negate() : this;
    }

    public double doubleValue() {
        return numerator.doubleValue() / denominator.doubleValue();
    }

    @Override
    public Rational add(Rational other) {
        Objects.requireNonNull(other, "other cannot be null");
        if (this.isZero()) return other;
        if (other.isZero()) return this;
        if (this.denominator.equals(other.denominator)) {
            return new Rational(this.numerator.add(other.numerator), this.denominator);
        }
        BigInteger num = this.numerator.multiply(other.denominator).add(other.numerator.multiply(this.denominator));
        BigInteger den = this.denominator.multiply(other.denominator);
        return new Rational(num, den);
    }

    @Override
    public Rational negate() {
        if (isZero()) return this;
        return new Rational(this.numerator.negate(), this.denominator);
    }

    @Override
    public Rational subtract(Rational other) {
        Objects.requireNonNull(other, "other cannot be null");
        return add(other.negate());
    }

    @Override
    public Rational multiply(Rational other) {
        Objects.requireNonNull(other, "other cannot be null");
        if (this.isZero() || other.isZero()) return ZERO;
        if (this.isOne()) return other;
        if (other.isOne()) return this;
        return new Rational(
            this.numerator.multiply(other.numerator),
            this.denominator.multiply(other.denominator)
        );
    }

    @Override
    public Rational reciprocal() {
        if (isZero()) {
            throw new ArithmeticException("Cannot compute reciprocal of zero rational");
        }
        return new Rational(this.denominator, this.numerator);
    }

    @Override
    public Rational divide(Rational other) {
        Objects.requireNonNull(other, "other cannot be null");
        return multiply(other.reciprocal());
    }

    @Override
    public int compareTo(Rational other) {
        Objects.requireNonNull(other, "other cannot be null");
        if (this.denominator.equals(other.denominator)) {
            return this.numerator.compareTo(other.numerator);
        }
        return this.numerator.multiply(other.denominator)
                   .compareTo(other.numerator.multiply(this.denominator));
    }

    @Override
    public String toString() {
        return denominator.equals(BigInteger.ONE) ? numerator.toString() : numerator + "/" + denominator;
    }
}
