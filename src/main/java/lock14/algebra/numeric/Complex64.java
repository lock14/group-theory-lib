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
 */
public record Complex64(double real, double imaginary) {

    public static final Complex64 ZERO = new Complex64(0.0, 0.0);
    public static final Complex64 ONE = new Complex64(1.0, 0.0);
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

    public Complex64 {
        if (real == 0.0) {
            real = 0.0;
        }
        if (imaginary == 0.0) {
            imaginary = 0.0;
        }
    }

    public static Complex64 of(double real, double imaginary) {
        return new Complex64(real, imaginary);
    }

    public static Complex64 ofReal(double real) {
        return new Complex64(real, 0.0);
    }

    public static Complex64 ofImaginary(double imaginary) {
        return new Complex64(0.0, imaginary);
    }

    public static Complex64 ofPolar(double r, double theta) {
        if (r < 0.0 || Double.isNaN(r)) {
            throw new IllegalArgumentException("Polar radius must be non-negative, got: " + r);
        }
        if (r == 0.0) {
            return ZERO;
        }
        return new Complex64(r * Math.cos(theta), r * Math.sin(theta));
    }

    public static ApproximateField<Complex64> field() {
        return FIELD;
    }

    public boolean isZero() {
        return real == 0.0 && imaginary == 0.0;
    }

    public double abs() {
        return Math.hypot(real, imaginary);
    }

    public double normSq() {
        return real * real + imaginary * imaginary;
    }

    public double arg() {
        return Math.atan2(imaginary, real);
    }

    public Complex64 conjugate() {
        return new Complex64(real, -imaginary);
    }

    public Complex64 scale(double factor) {
        return new Complex64(real * factor, imaginary * factor);
    }

    public Complex64 add(Complex64 other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Complex64(this.real + other.real, this.imaginary + other.imaginary);
    }

    public Complex64 negate() {
        return new Complex64(-real, -imaginary);
    }

    public Complex64 subtract(Complex64 other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Complex64(this.real - other.real, this.imaginary - other.imaginary);
    }

    public Complex64 multiply(Complex64 other) {
        Objects.requireNonNull(other, "other cannot be null");
        double re = this.real * other.real - this.imaginary * other.imaginary;
        double im = this.real * other.imaginary + this.imaginary * other.real;
        return new Complex64(re, im);
    }

    /**
     * Computes {@code 1 / z} using Smith's scaling formula to avoid intermediate overflow/underflow.
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

    public Optional<Complex64> tryReciprocal() {
        return isZero() ? Optional.empty() : Optional.of(reciprocal());
    }

    /**
     * Computes {@code this / other} using Smith's algorithm to prevent intermediate overflow/underflow.
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

    public Complex64 exp() {
        double expRe = Math.exp(real);
        return new Complex64(expRe * Math.cos(imaginary), expRe * Math.sin(imaginary));
    }

    public Complex64 log() {
        if (isZero()) {
            throw new UndefinedOperationException("Complex logarithm of 0 is undefined");
        }
        return new Complex64(Math.log(abs()), arg());
    }

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

    public boolean approxEquals(Complex64 other, double epsilon) {
        return FIELD.approxEquals(this, other, epsilon);
    }

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
