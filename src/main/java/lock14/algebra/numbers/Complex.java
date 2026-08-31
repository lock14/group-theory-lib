package lock14.algebra.numbers;

import java.util.Objects;
import lock14.algebra.element.FieldElement;
import lock14.algebra.structure.Field;

/**
 * An immutable Complex number (a + bi) over IEEE-754 double-precision floating point.
 */
public record Complex(double real, double imaginary) implements FieldElement<Complex> {

    public static final Complex ZERO = new Complex(0.0, 0.0);
    public static final Complex ONE = new Complex(1.0, 0.0);
    public static final Complex I = new Complex(0.0, 1.0);

    public static Complex of(double real, double imaginary) {
        return new Complex(real, imaginary);
    }

    public static Complex real(double real) {
        return new Complex(real, 0.0);
    }

    public static Complex imaginary(double imaginary) {
        return new Complex(0.0, imaginary);
    }

    public static Complex ofPolar(double radius, double theta) {
        if (radius < 0.0) {
            throw new IllegalArgumentException("Radius must be non-negative: " + radius);
        }
        return new Complex(radius * Math.cos(theta), radius * Math.sin(theta));
    }

    public static Field<Complex> field() {
        return ComplexField.INSTANCE;
    }

    public boolean isZero() {
        return real == 0.0 && imaginary == 0.0;
    }

    public boolean isOne() {
        return real == 1.0 && imaginary == 0.0;
    }

    public double normSq() {
        return real * real + imaginary * imaginary;
    }

    public double abs() {
        return Math.hypot(real, imaginary);
    }

    public double arg() {
        return Math.atan2(imaginary, real);
    }

    public Complex conjugate() {
        return new Complex(real, imaginary == 0.0 ? 0.0 : -imaginary);
    }

    public Complex scale(double scalar) {
        return new Complex(real * scalar, imaginary * scalar);
    }

    @Override
    public Complex add(Complex other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Complex(this.real + other.real, this.imaginary + other.imaginary);
    }

    @Override
    public Complex negate() {
        return new Complex(
            real == 0.0 ? 0.0 : -real,
            imaginary == 0.0 ? 0.0 : -imaginary
        );
    }

    @Override
    public Complex subtract(Complex other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Complex(this.real - other.real, this.imaginary - other.imaginary);
    }

    @Override
    public Complex multiply(Complex other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Complex(
            this.real * other.real - this.imaginary * other.imaginary,
            this.real * other.imaginary + this.imaginary * other.real
        );
    }

    @Override
    public Complex reciprocal() {
        if (isZero()) {
            throw new ArithmeticException("Cannot invert complex zero");
        }
        double c = this.real;
        double d = this.imaginary;
        if (Math.abs(c) >= Math.abs(d)) {
            double ratio = d / c;
            double denom = c + d * ratio;
            return new Complex(1.0 / denom, -ratio / denom);
        } else {
            double ratio = c / d;
            double denom = c * ratio + d;
            return new Complex(ratio / denom, -1.0 / denom);
        }
    }

    @Override
    public Complex divide(Complex other) {
        Objects.requireNonNull(other, "other cannot be null");
        if (other.isZero()) {
            throw new ArithmeticException("Division by complex zero");
        }
        double a = this.real;
        double b = this.imaginary;
        double c = other.real;
        double d = other.imaginary;

        if (Math.abs(c) >= Math.abs(d)) {
            double ratio = d / c;
            double denom = c + d * ratio;
            return new Complex((a + b * ratio) / denom, (b - a * ratio) / denom);
        } else {
            double ratio = c / d;
            double denom = c * ratio + d;
            return new Complex((a * ratio + b) / denom, (b * ratio - a) / denom);
        }
    }

    public Complex exp() {
        double expReal = Math.exp(real);
        return new Complex(expReal * Math.cos(imaginary), expReal * Math.sin(imaginary));
    }

    public Complex log() {
        return new Complex(Math.log(abs()), arg());
    }

    public Complex pow(Complex exponent) {
        Objects.requireNonNull(exponent, "exponent cannot be null");
        if (isZero()) {
            if (exponent.isZero()) return ONE;
            return ZERO;
        }
        return this.log().multiply(exponent).exp();
    }

    @Override
    public String toString() {
        if (imaginary == 0.0) return Double.toString(real);
        if (real == 0.0) {
            if (imaginary == 1.0) return "i";
            if (imaginary == -1.0) return "-i";
            return imaginary + "i";
        }
        String sign = imaginary > 0 ? " + " : " - ";
        double absIm = Math.abs(imaginary);
        return real + sign + (absIm == 1.0 ? "" : absIm) + "i";
    }
}
