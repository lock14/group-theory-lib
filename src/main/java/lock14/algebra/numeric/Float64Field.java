package lock14.algebra.numeric;

import java.util.Objects;
import lock14.algebra.exceptions.NonInvertibleElementException;

/**
 * Approximate IEEE-754 64-bit floating-point field over {@link Double}.
 */
public final class Float64Field implements ApproximateField<Double> {

    /**
     * Singleton instance of the approximate IEEE-754 64-bit floating-point field.
     */
    public static final Float64Field INSTANCE = new Float64Field();

    private Float64Field() {
    }

    private static double canonicalize(double value) {
        return value == 0.0 ? 0.0 : value;
    }

    @Override
    public Double zero() {
        return 0.0;
    }

    @Override
    public Double one() {
        return 1.0;
    }

    @Override
    public Double add(Double a, Double b) {
        Objects.requireNonNull(a, "a cannot be null");
        Objects.requireNonNull(b, "b cannot be null");
        return canonicalize(a + b);
    }

    @Override
    public Double negate(Double a) {
        Objects.requireNonNull(a, "a cannot be null");
        return canonicalize(-a);
    }

    @Override
    public Double subtract(Double a, Double b) {
        Objects.requireNonNull(a, "a cannot be null");
        Objects.requireNonNull(b, "b cannot be null");
        return canonicalize(a - b);
    }

    @Override
    public Double multiply(Double a, Double b) {
        Objects.requireNonNull(a, "a cannot be null");
        Objects.requireNonNull(b, "b cannot be null");
        return canonicalize(a * b);
    }

    @Override
    public Double reciprocal(Double a) {
        Objects.requireNonNull(a, "a cannot be null");
        if (a == 0.0) {
            throw new NonInvertibleElementException("Cannot compute reciprocal of 0.0");
        }
        return canonicalize(1.0 / a);
    }

    @Override
    public Double divide(Double a, Double b) {
        Objects.requireNonNull(a, "a cannot be null");
        Objects.requireNonNull(b, "b cannot be null");
        if (b == 0.0) {
            throw new NonInvertibleElementException("Division by 0.0");
        }
        return canonicalize(a / b);
    }

    @Override
    public double abs(Double a) {
        Objects.requireNonNull(a, "a cannot be null");
        return Math.abs(a);
    }
}
