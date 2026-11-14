package lock14.algebra.numeric;

import java.util.Objects;
import java.util.Optional;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.exceptions.UndefinedOperationException;

/**
 * IEEE-754 64-bit floating-point complex number {@code a + bi}, equipped with overflow-resistant
 * Smith scaling for division/inversion and analytic transcendental operations.
 *
 * <p>For exact algebraic complex arithmetic (such as Gaussian integers {@code Z[i]} or Gaussian rationals
 * {@code Q(i)}), use {@link lock14.algebra.numbers.Complex}.</p>
 *
 * @param real      the IEEE-754 {@code double} real part {@code a}
 * @param imaginary the IEEE-754 {@code double} imaginary part {@code b}
 */
public record Complex64(double real, double imaginary) {

    /**
     * The additive identity {@code 0.0 + 0.0i}.
     */
    public static final Complex64 ZERO = new Complex64(0.0, 0.0);

    /**
     * The multiplicative identity {@code 1.0 + 0.0i}.
     */
    public static final Complex64 ONE = new Complex64(1.0, 0.0);

    /**
     * The imaginary unit {@code 0.0 + 1.0i}.
     */
    public static final Complex64 I = new Complex64(0.0, 1.0);

    private static final ApproximateField<Complex64> FIELD = new ApproximateField<>() {
        @Override
        public Complex64 zero() {
            return ZERO;
        }

        @Override
        public Complex64 one() {
            return ONE;
        }

        @Override
        public Complex64 add(Complex64 a, Complex64 b) {
            Objects.requireNonNull(a, "a cannot be null");
            return a.add(b);
        }

        @Override
        public Complex64 negate(Complex64 a) {
            Objects.requireNonNull(a, "a cannot be null");
            return a.negate();
        }

        @Override
        public Complex64 subtract(Complex64 a, Complex64 b) {
            Objects.requireNonNull(a, "a cannot be null");
            return a.subtract(b);
        }

        @Override
        public Complex64 multiply(Complex64 a, Complex64 b) {
            Objects.requireNonNull(a, "a cannot be null");
            return a.multiply(b);
        }

        @Override
        public Complex64 reciprocal(Complex64 a) {
            Objects.requireNonNull(a, "a cannot be null");
            return a.reciprocal();
        }

        @Override
        public Complex64 divide(Complex64 a, Complex64 b) {
            Objects.requireNonNull(a, "a cannot be null");
            return a.divide(b);
        }

        @Override
        public double abs(Complex64 a) {
            Objects.requireNonNull(a, "a cannot be null");
            return a.abs();
        }
    };

    /**
     * Canonicalizes signed zero ({@code -0.0}) components to positive {@code +0.0}.
     *
     * @param real      the real part
     * @param imaginary the imaginary part
     */
    public Complex64 {
        if (real == 0.0) {
            real = 0.0;
        }
        if (imaginary == 0.0) {
            imaginary = 0.0;
        }
    }

    /**
     * Constructs the complex number {@code real + imaginary * i}.
     *
     * @param real      the real component
     * @param imaginary the imaginary component
     * @return {@code real + imaginary * i}
     */
    public static Complex64 of(double real, double imaginary) {
        return new Complex64(real, imaginary);
    }

    /**
     * Constructs the purely real complex number {@code real + 0.0i}.
     *
     * @param real the real component
     * @return {@code real + 0.0i}
     */
    public static Complex64 ofReal(double real) {
        return new Complex64(real, 0.0);
    }

    /**
     * Constructs the purely imaginary complex number {@code 0.0 + imaginary * i}.
     *
     * @param imaginary the imaginary component
     * @return {@code 0.0 + imaginary * i}
     */
    public static Complex64 ofImaginary(double imaginary) {
        return new Complex64(0.0, imaginary);
    }

    /**
     * Constructs {@code r * (cos(theta) + i * sin(theta))} from polar coordinates.
     *
     * @param r     the non-negative polar radius
     * @param theta the phase angle in radians
     * @return the complex number in rectangular form
     */
    public static Complex64 ofPolar(double r, double theta) {
        if (r < 0.0 || Double.isNaN(r)) {
            throw new IllegalArgumentException("Polar radius must be non-negative, got: " + r);
        }
        if (r == 0.0) {
            return ZERO;
        }
        return new Complex64(r * Math.cos(theta), r * Math.sin(theta));
    }

    /**
     * Returns the {@link ApproximateField} instance over {@link Complex64}.
     *
     * @return the approximate complex field
     */
    public static ApproximateField<Complex64> field() {
        return FIELD;
    }

    /**
     * Returns {@code true} if both real and imaginary parts are {@code 0.0}.
     *
     * @return {@code true} if {@code real == 0.0 && imaginary == 0.0}
     */
    public boolean isZero() {
        return real == 0.0 && imaginary == 0.0;
    }

    /**
     * Computes the overflow-safe Euclidean modulus {@code sqrt(real^2 + imaginary^2)} via {@link Math#hypot(double, double)}.
     *
     * @return the non-negative magnitude {@code |this|}
     */
    public double abs() {
        return Math.hypot(real, imaginary);
    }

    /**
     * Computes the unscaled squared modulus {@code real^2 + imaginary^2}.
     *
     * @return {@code real * real + imaginary * imaginary}
     */
    public double normSq() {
        return real * real + imaginary * imaginary;
    }

    /**
     * Computes the principal argument (phase angle) in {@code [-pi, pi]} via {@link Math#atan2(double, double)}.
     *
     * @return the phase angle in radians
     */
    public double arg() {
        return Math.atan2(imaginary, real);
    }

    /**
     * Returns the complex conjugate {@code real - imaginary * i}.
     *
     * @return the conjugate {@link Complex64}
     */
    public Complex64 conjugate() {
        return new Complex64(real, -imaginary);
    }

    /**
     * Multiplies both real and imaginary components by {@code factor}.
     *
     * @param factor the real scalar multiplier
     * @return the scaled complex number {@code factor * this}
     */
    public Complex64 scale(double factor) {
        return new Complex64(real * factor, imaginary * factor);
    }

    /**
     * Computes the sum {@code this + other}.
     *
     * @param other the complex addend
     * @return {@code this + other}
     */
    public Complex64 add(Complex64 other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Complex64(this.real + other.real, this.imaginary + other.imaginary);
    }

    /**
     * Computes the additive inverse {@code -this}.
     *
     * @return {@code -real - imaginary * i}
     */
    public Complex64 negate() {
        return new Complex64(-real, -imaginary);
    }

    /**
     * Computes the difference {@code this - other}.
     *
     * @param other the complex subtrahend
     * @return {@code this - other}
     */
    public Complex64 subtract(Complex64 other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Complex64(this.real - other.real, this.imaginary - other.imaginary);
    }

    /**
     * Computes the complex product {@code this * other}.
     *
     * @param other the right-hand complex factor
     * @return {@code this * other}
     */
    public Complex64 multiply(Complex64 other) {
        Objects.requireNonNull(other, "other cannot be null");
        double re = this.real * other.real - this.imaginary * other.imaginary;
        double im = this.real * other.imaginary + this.imaginary * other.real;
        return new Complex64(re, im);
    }

    /**
     * Computes {@code 1 / z} using Smith's scaling formula to avoid intermediate overflow/underflow.
     *
     * @return the multiplicative inverse {@code 1 / this}
     * @throws NonInvertibleElementException if {@link #isZero()} is {@code true}
     */
    public Complex64 reciprocal() {
        if (isZero()) {
            throw new NonInvertibleElementException("Cannot compute reciprocal of zero Complex64");
        }
        if (Math.abs(real) >= Math.abs(imaginary)) {
            double q = imaginary / real;
            double denom = real + imaginary * q;
            return new Complex64(1.0 / denom, -q / denom);
        } else {
            double q = real / imaginary;
            double denom = imaginary + real * q;
            return new Complex64(q / denom, -1.0 / denom);
        }
    }

    /**
     * Attempts to compute the multiplicative inverse of this complex number.
     *
     * @return an {@link Optional} containing {@code 1 / this}, or {@link Optional#empty()} if {@link #isZero()}
     */
    public Optional<Complex64> tryReciprocal() {
        return isZero() ? Optional.empty() : Optional.of(reciprocal());
    }

    /**
     * Computes {@code this / other} using Smith's algorithm to prevent intermediate overflow/underflow.
     *
     * @param other the non-zero complex divisor
     * @return the quotient {@code this / other}
     * @throws NonInvertibleElementException if {@code other.isZero()} is {@code true}
     */
    public Complex64 divide(Complex64 other) {
        Objects.requireNonNull(other, "other cannot be null");
        if (other.isZero()) {
            throw new NonInvertibleElementException("Division by zero Complex64");
        }
        double c = other.real;
        double d = other.imaginary;
        if (Math.abs(c) >= Math.abs(d)) {
            double q = d / c;
            double denom = c + d * q;
            return new Complex64(
                (this.real + this.imaginary * q) / denom,
                (this.imaginary - this.real * q) / denom
            );
        } else {
            double q = c / d;
            double denom = d + c * q;
            return new Complex64(
                (this.real * q + this.imaginary) / denom,
                (this.imaginary * q - this.real) / denom
            );
        }
    }

    /**
     * Computes the complex exponential {@code exp(this) = exp(real) * (cos(imaginary) + i * sin(imaginary))}.
     *
     * @return {@code exp(this)}
     */
    public Complex64 exp() {
        double expRe = Math.exp(real);
        return new Complex64(expRe * Math.cos(imaginary), expRe * Math.sin(imaginary));
    }

    /**
     * Computes the principal branch of the natural logarithm {@code ln(|this|) + i * arg(this)}.
     *
     * @return the principal complex logarithm
     * @throws UndefinedOperationException if {@link #isZero()} is {@code true}
     */
    public Complex64 log() {
        if (isZero()) {
            throw new UndefinedOperationException("Complex logarithm of 0 is undefined");
        }
        return new Complex64(Math.log(abs()), arg());
    }

    /**
     * Computes the principal square root with non-negative real part.
     *
     * @return the principal square root {@code sqrt(this)}
     */
    public Complex64 sqrt() {
        if (isZero()) {
            return ZERO;
        }
        double r = abs();
        double re = Math.sqrt(0.5 * (r + Math.abs(real)));
        double im = Math.abs(imaginary) / (2.0 * re);
        if (real >= 0.0) {
            return new Complex64(re, imaginary < 0.0 ? -im : im);
        } else {
            return new Complex64(im, imaginary < 0.0 ? -re : re);
        }
    }

    /**
     * Computes the principal complex power {@code this^exponent = exp(exponent * log(this))}.
     *
     * @param exponent the complex exponent
     * @return {@code this^exponent}
     * @throws UndefinedOperationException if {@code this} is zero and {@code exponent} is non-zero with {@code exponent.real() <= 0}
     */
    public Complex64 pow(Complex64 exponent) {
        Objects.requireNonNull(exponent, "exponent cannot be null");
        if (this.isZero()) {
            if (exponent.isZero()) {
                return ONE;
            }
            if (exponent.real > 0.0) {
                return ZERO;
            }
            throw new UndefinedOperationException("0 raised to non-positive real-part exponent is undefined");
        }
        return exponent.multiply(this.log()).exp();
    }

    /**
     * Returns {@code true} if {@code |this - other| <= epsilon}.
     *
     * @param other   the complex number to compare with
     * @param epsilon the non-negative finite tolerance
     * @return {@code true} if within {@code epsilon} in Euclidean complex distance
     */
    public boolean approxEquals(Complex64 other, double epsilon) {
        return FIELD.approxEquals(this, other, epsilon);
    }

    /**
     * Returns {@code true} if {@code |this - other| <= NumericContext.currentEpsilon()}.
     *
     * @param other the complex number to compare with
     * @return {@code true} if approximately equal within the active tolerance
     */
    public boolean approxEquals(Complex64 other) {
        return FIELD.approxEquals(this, other);
    }

    @Override
    public String toString() {
        if (imaginary == 0.0) {
            return Double.toString(real);
        }
        if (real == 0.0) {
            return imaginary + "i";
        }
        return imaginary < 0.0 ? real + " - " + (-imaginary) + "i" : real + " + " + imaginary + "i";
    }
}
