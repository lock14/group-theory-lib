package lock14.algebra.numeric;

import java.math.MathContext;

/**
 * Thread-scoped numerical configuration for approximate IEEE-754 and arbitrary-precision decimal operations,
 * powered by Java {@link ScopedValue}s.
 */
public final class NumericContext {

    /**
     * Default absolute tolerance ({@code 1e-9}) for approximate floating-point comparisons when
     * {@link #EPSILON} is not bound in the current scope.
     */
    public static final double DEFAULT_EPSILON = 1e-9;

    /**
     * Scoped tolerance for approximate equality and numerical singularity detection.
     */
    public static final ScopedValue<Double> EPSILON = ScopedValue.newInstance();

    /**
     * Scoped decimal precision and rounding configuration, defaulting to {@link MathContext#DECIMAL128}.
     */
    public static final ScopedValue<MathContext> MATH_CONTEXT = ScopedValue.newInstance();

    private NumericContext() {
    }

    /**
     * Returns the bound {@link #EPSILON} tolerance in the active scope, or {@link #DEFAULT_EPSILON} if unbound.
     *
     * @return the active non-negative numerical tolerance
     * @throws IllegalStateException if the bound epsilon is {@code null}, {@code NaN}, infinite, or negative
     */
    public static double currentEpsilon() {
        if (!EPSILON.isBound()) {
            return DEFAULT_EPSILON;
        }
        Double eps = EPSILON.get();
        if (eps == null || Double.isNaN(eps) || Double.isInfinite(eps) || eps < 0.0) {
            throw new IllegalStateException(
                "NumericContext.EPSILON must be a finite non-negative number, got: " + eps
            );
        }
        return eps;
    }

    /**
     * Returns the bound {@link #MATH_CONTEXT} in the active scope, or {@link MathContext#DECIMAL128} if unbound.
     *
     * @return the active {@link MathContext}
     * @throws IllegalStateException if the bound {@code MathContext} is {@code null}
     */
    public static MathContext currentMathContext() {
        if (!MATH_CONTEXT.isBound()) {
            return MathContext.DECIMAL128;
        }
        MathContext mc = MATH_CONTEXT.get();
        if (mc == null) {
            throw new IllegalStateException("NumericContext.MATH_CONTEXT cannot be null");
        }
        return mc;
    }
}
