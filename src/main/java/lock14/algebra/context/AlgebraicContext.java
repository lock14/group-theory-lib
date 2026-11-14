package lock14.algebra.context;

import java.math.BigInteger;
import java.math.MathContext;

/**
 * Ambient algebraic configuration context using Java 25 Scoped Values.
 */
public final class AlgebraicContext {

    public static final ScopedValue<BigInteger> MODULUS = ScopedValue.newInstance();
    public static final ScopedValue<Double> EPSILON = ScopedValue.newInstance();
    public static final ScopedValue<MathContext> MATH_CONTEXT = ScopedValue.newInstance();

    public static final double DEFAULT_EPSILON = 1e-9;

    private AlgebraicContext() {}

    public static BigInteger currentModulus() {
        return MODULUS.orElseThrow(
            () -> new IllegalStateException("No ambient modulus bound in AlgebraicContext.MODULUS")
        );
    }

    public static double currentEpsilon() {
        double eps = EPSILON.orElse(DEFAULT_EPSILON);
        if (Double.isNaN(eps) || eps < 0.0) {
            throw new IllegalStateException("AlgebraicContext.EPSILON must be non-negative: " + eps);
        }
        return eps;
    }

    public static MathContext currentMathContext() {
        return MATH_CONTEXT.orElse(MathContext.DECIMAL128);
    }
}
