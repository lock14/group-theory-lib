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
 *
 * @param numerator   the canonical reduced numerator
 * @param denominator the canonical positive denominator coprime to {@code |numerator|}
 */
public record Rational(BigInteger numerator, BigInteger denominator)
    implements FieldElement<Rational>, Comparable<Rational> {

    /**
     * The additive identity {@code 0/1}.
     */
    public static final Rational ZERO = new Rational(BigInteger.ZERO, BigInteger.ONE);

    /**
     * The multiplicative identity {@code 1/1}.
     */
    public static final Rational ONE = new Rational(BigInteger.ONE, BigInteger.ONE);

    /**
     * The rational constant {@code 2/1}.
     */
    public static final Rational TWO = new Rational(BigInteger.TWO, BigInteger.ONE);

    /**
     * The rational constant {@code 10/1}.
     */
    public static final Rational TEN = new Rational(BigInteger.TEN, BigInteger.ONE);

    /**
     * Constructs a canonical reduced {@code Rational}, normalizing sign so {@code denominator > 0}
     * and dividing out {@code gcd(|numerator|, denominator)}.
     *
     * @param numerator   the unreduced numerator
     * @param denominator the non-zero unreduced denominator
     * @throws NonInvertibleElementException if {@code denominator} is zero
     */
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

    /**
     * Creates a canonical {@code Rational} from {@code long} numerator and denominator values.
     *
     * @param numerator   the numerator
     * @param denominator the non-zero denominator
     * @return the reduced rational {@code numerator / denominator}
     * @throws NonInvertibleElementException if {@code denominator == 0}
     */
    public static Rational of(long numerator, long denominator) {
        return new Rational(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    /**
     * Creates a canonical {@code Rational} representing an integer value.
     *
     * @param integer the integer value
     * @return the rational {@code integer / 1}
     */
    public static Rational of(long integer) {
        return new Rational(BigInteger.valueOf(integer), BigInteger.ONE);
    }

    /**
     * Creates a canonical {@code Rational} from {@link BigInteger} numerator and denominator values.
     *
     * @param numerator   the numerator
     * @param denominator the non-zero denominator
     * @return the reduced rational {@code numerator / denominator}
     * @throws NonInvertibleElementException if {@code denominator} is zero
     */
    public static Rational of(BigInteger numerator, BigInteger denominator) {
        return new Rational(numerator, denominator);
    }

    /**
     * Creates a canonical {@code Rational} representing an arbitrary-precision integer value.
     *
     * @param integer the integer value
     * @return the rational {@code integer / 1}
     */
    public static Rational of(BigInteger integer) {
        return new Rational(integer, BigInteger.ONE);
    }

    /**
     * Returns the exact {@link Field} structure over {@link Rational}.
     *
     * @return {@link Rationals#FIELD}
     */
    public static Field<Rational> field() {
        return Rationals.FIELD;
    }

    /**
     * Returns {@code true} if this rational equals {@code 0}.
     *
     * @return {@code true} if {@code numerator == 0}
     */
    public boolean isZero() {
        return numerator.signum() == 0;
    }

    /**
     * Returns {@code true} if this rational equals {@code 1}.
     *
     * @return {@code true} if {@code numerator == 1} and {@code denominator == 1}
     */
    public boolean isOne() {
        return numerator.equals(BigInteger.ONE) && denominator.equals(BigInteger.ONE);
    }

    /**
     * Returns the signum function of this rational ({@code -1}, {@code 0}, or {@code 1}).
     *
     * @return {@code -1} if negative, {@code 0} if zero, or {@code 1} if positive
     */
    public int signum() {
        return numerator.signum();
    }

    /**
     * Returns the non-negative absolute value {@code |this|}.
     *
     * @return {@code -this} if negative, otherwise {@code this}
     */
    public Rational abs() {
        return signum() < 0 ? negate() : this;
    }

    /**
     * Approximates this rational as a {@link BigDecimal} rounded according to {@code mathContext}.
     *
     * @param mathContext precision and rounding configuration
     * @return decimal representation of {@code numerator / denominator}
     */
    public BigDecimal bigDecimalValue(MathContext mathContext) {
        Objects.requireNonNull(mathContext, "mathContext cannot be null");
        if (isZero()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(numerator).divide(new BigDecimal(denominator), mathContext);
    }

    /**
     * Approximates this rational as a {@link BigDecimal} using {@link NumericContext#currentMathContext()}.
     *
     * @return decimal representation of {@code numerator / denominator}
     */
    public BigDecimal bigDecimalValue() {
        return bigDecimalValue(NumericContext.currentMathContext());
    }

    /**
     * Converts this rational to the closest IEEE-754 {@code double} approximation.
     *
     * @return floating-point approximation of {@code numerator / denominator}
     */
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
