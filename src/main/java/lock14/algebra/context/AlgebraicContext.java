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

    public static double currentEpsilon() {
        return EPSILON.orElse(DEFAULT_EPSILON);
    }

    public static MathContext currentMathContext() {
        return MATH_CONTEXT.orElse(MathContext.DECIMAL128);
    }
}
