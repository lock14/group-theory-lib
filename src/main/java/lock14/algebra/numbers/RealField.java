package lock14.algebra.numbers;

import java.util.Objects;
import lock14.algebra.context.AlgebraicContext;
import lock14.algebra.structure.Field;

/**
 * The Field of Real Numbers (R, +, *) over IEEE-754 {@link Double}.
 */
public final class RealField implements Field<Double> {

    public static final RealField INSTANCE = new RealField();

    private RealField() {}

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
        return a + b;
    }

    @Override
    public Double negate(Double a) {
        return -a;
    }

    @Override
    public Double subtract(Double a, Double b) {
        return a - b;
    }

    @Override
    public Double multiply(Double a, Double b) {
        return a * b;
    }

    @Override
    public Double reciprocal(Double a) {
        if (a == 0.0) {
            throw new ArithmeticException("Division by zero");
        }
        return 1.0 / a;
    }

    @Override
    public Double divide(Double a, Double b) {
        if (b == 0.0) {
            throw new ArithmeticException("Division by zero");
        }
        return a / b;
    }

    @Override
    public boolean isZero(Double element) {
        return element == 0.0;
    }

    @Override
    public boolean isOne(Double element) {
        return element == 1.0;
    }

    /**
     * Checks whether {@code a} and {@code b} are within the ambient {@link AlgebraicContext#currentEpsilon()}.
     */
    public boolean approxEquals(Double a, Double b) {
        Objects.requireNonNull(a, "a cannot be null");
        Objects.requireNonNull(b, "b cannot be null");
        if (a.equals(b)) {
            return true;
        }
        return Math.abs(a - b) <= AlgebraicContext.currentEpsilon();
    }
}
