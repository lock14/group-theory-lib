package lock14.algebra.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Objects;
import java.util.Optional;
import lock14.algebra.element.FieldElement;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.numeric.NumericContext;
import lock14.algebra.structure.Field;

/**
 * An exact, immutable rational number {@code p / q} in canonical reduced form ({@code gcd(|p|, q) = 1, q > 0}).
 */
public record Rational(BigInteger numerator, BigInteger denominator)
    implements FieldElement<Rational>, Comparable<Rational> {

    public static final Rational ZERO = new Rational(BigInteger.ZERO, BigInteger.ONE);
    public static final Rational ONE = new Rational(BigInteger.ONE, BigInteger.ONE);
    public static final Rational TWO = new Rational(BigInteger.TWO, BigInteger.ONE);
    public static final Rational TEN = new Rational(BigInteger.TEN, BigInteger.ONE);

    public Rational {
        Objects.requireNonNull(numerator, "numerator cannot be null");
        Objects.requireNonNull(denominator, "denominator cannot be null");
        if (denominator.signum() == 0) {
            throw new NonInvertibleElementException("Rational denominator cannot be zero");
        }
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
        if (numerator.signum() == 0) {
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
        return Rationals.FIELD;
    }

    public boolean isZero() {
        return numerator.signum() == 0;
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

    public BigDecimal bigDecimalValue(MathContext mathContext) {
        Objects.requireNonNull(mathContext, "mathContext cannot be null");
        if (isZero()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(numerator).divide(new BigDecimal(denominator), mathContext);
    }

    public BigDecimal bigDecimalValue() {
        return bigDecimalValue(NumericContext.currentMathContext());
    }

    public double doubleValue() {
        if (isZero()) {
            return 0.0;
        }
        if (numerator.bitLength() <= 53 && denominator.bitLength() <= 53) {
            return numerator.doubleValue() / denominator.doubleValue();
        }
        return bigDecimalValue(MathContext.DECIMAL128).doubleValue();
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
            throw new NonInvertibleElementException("Cannot compute reciprocal of zero rational");
        }
        return new Rational(this.denominator, this.numerator);
    }

    @Override
    public Optional<Rational> tryReciprocal() {
        return isZero() ? Optional.empty() : Optional.of(reciprocal());
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
