package lock14.group;

import java.util.Objects;

public final class Complex implements Field<Complex> {
    public static final Complex ZERO = new Complex(0.0, 0.0);
    public static final Complex ONE = new Complex(1.0, 0.0);

    private final double real;
    private final double imaginary;

    private Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public static Complex of(double real, double imaginary) {
        return new Complex(real, imaginary);
    }

    public static Complex ofPolar(double radius, double angle) {
        return new Complex(radius * Math.cos(angle), radius * Math.sin(angle));
    }

    @Override
    public Complex additiveIdentity() {
        return ZERO;
    }

    @Override
    public Complex additiveInverse() {
        return Complex.of(-this.real, -this.imaginary);
    }

    @Override
    public Complex divide(Complex other) {
        double divisor = divisor(other.real, other.imaginary);
        double newReal = ((this.real * other.real) + (this.imaginary * other.imaginary)) / divisor;
        double newImaginary = ((this.imaginary * other.real) - (this.real * other.imaginary)) / divisor;
        return Complex.of(newReal, newImaginary);
    }

    @Override
    public Complex minus(Complex other) {
        return Complex.of(this.real - other.real, this.imaginary - other.imaginary);
    }

    @Override
    public Complex multiplicativeIdentity() {
        return ONE;
    }

    @Override
    public Complex multiplicativeInverse() {
        double divisor = divisor(this.real, this.imaginary);
        return Complex.of(this.real / divisor, -this.imaginary / divisor);
    }

    @Override
    public Complex plus(Complex other) {
        return Complex.of(this.real + other.real, this.imaginary + other.imaginary);
    }

    @Override
    public Complex times(Complex other) {
        return Complex.of((this.real * other.real) - (this.imaginary * other.imaginary),
                          (this.real * other.imaginary) + (this.imaginary * other.real));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Complex)) {
            return false;
        }
        Complex complex = (Complex) o;
        return Double.compare(complex.real, real) == 0 &&
               Double.compare(complex.imaginary, imaginary) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(real, imaginary);
    }

    @Override
    public String toString() {
        return real + " + " + imaginary + "i";
    }

    private static double divisor(double real, double imaginary) {
        return (real * real) + (imaginary * imaginary);
    }
}
