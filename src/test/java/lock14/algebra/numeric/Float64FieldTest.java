package lock14.algebra.numeric;

import java.math.MathContext;
import java.util.Objects;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.ApproximateFieldLaws;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class Float64FieldTest extends ApproximateFieldLaws<Double> {

    @Override
    protected ApproximateField<Double> field() {
        return Float64Field.INSTANCE;
    }

    @Override
    protected double tolerance() {
        return 1e-8;
    }

    @Provide
    Arbitrary<Double> elements() {
        return Arbitraries.oneOf(
            Arbitraries.of(0.0, -0.0, 1.0, -1.0, 0.5, -0.5),
            Arbitraries.doubles().between(-1_000.0, 1_000.0)
        );
    }

    @Provide
    Arbitrary<Double> nonZeroElements() {
        return elements().filter(d -> Math.abs(d) > 1e-4);
    }

    @Test
    public void testSignedZeroCanonicalizationAndArithmeticEdgeCases() {
        Float64Field f = Float64Field.INSTANCE;
        assertThat(f.zero()).isEqualTo(0.0);
        assertThat(f.one()).isEqualTo(1.0);

        assertThat(Double.doubleToRawLongBits(f.negate(0.0))).isZero();
        assertThat(Double.doubleToRawLongBits(f.negate(-0.0))).isZero();
        assertThat(f.negate(3.5)).isEqualTo(-3.5);

        assertThat(Double.doubleToRawLongBits(f.add(-0.0, -0.0))).isZero();
        assertThat(Double.doubleToRawLongBits(f.add(2.5, -2.5))).isZero();
        assertThat(f.add(2.5, 1.5)).isEqualTo(4.0);

        assertThat(Double.doubleToRawLongBits(f.subtract(-0.0, 0.0))).isZero();
        assertThat(Double.doubleToRawLongBits(f.subtract(3.0, 3.0))).isZero();
        assertThat(f.subtract(5.0, 2.0)).isEqualTo(3.0);

        assertThat(Double.doubleToRawLongBits(f.multiply(-2.0, 0.0))).isZero();
        assertThat(f.multiply(2.0, -3.0)).isEqualTo(-6.0);

        assertThat(Double.doubleToRawLongBits(f.reciprocal(Double.NEGATIVE_INFINITY))).isZero();
        assertThat(f.reciprocal(4.0)).isEqualTo(0.25);
        assertThat(f.tryReciprocal(4.0)).contains(0.25);
        assertThat(f.tryReciprocal(0.0)).isEmpty();

        assertThat(Double.doubleToRawLongBits(f.divide(-0.0, 5.0))).isZero();
        assertThat(f.divide(6.0, 2.0)).isEqualTo(3.0);
        assertThat(f.abs(-7.5)).isEqualTo(7.5);

        assertThatThrownBy(() -> f.reciprocal(0.0)).isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> f.reciprocal(-0.0)).isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> f.divide(1.0, 0.0)).isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> f.divide(1.0, -0.0)).isInstanceOf(NonInvertibleElementException.class);

        assertThatThrownBy(() -> f.add(null, 1.0)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.add(1.0, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.negate(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.subtract(null, 1.0)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.subtract(1.0, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.multiply(null, 1.0)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.multiply(1.0, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.reciprocal(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.divide(null, 1.0)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.divide(1.0, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.abs(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testApproximateFieldDefaultMethods() {
        ApproximateField<Double> minimal = new ApproximateField<>() {
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
                return Objects.requireNonNull(a) + Objects.requireNonNull(b);
            }

            @Override
            public Double negate(Double a) {
                return -Objects.requireNonNull(a);
            }

            @Override
            public Double multiply(Double a, Double b) {
                return Objects.requireNonNull(a) * Objects.requireNonNull(b);
            }

            @Override
            public Double reciprocal(Double a) {
                if (Objects.requireNonNull(a) == 0.0) {
                    throw new NonInvertibleElementException("Division by zero");
                }
                return 1.0 / a;
            }

            @Override
            public double abs(Double a) {
                return Math.abs(Objects.requireNonNull(a));
            }
        };

        assertThat(minimal.subtract(5.0, 2.0)).isCloseTo(3.0, within(1e-12));
        assertThat(minimal.divide(6.0, 2.0)).isCloseTo(3.0, within(1e-12));
        assertThatThrownBy(() -> minimal.divide(6.0, 0.0)).isInstanceOf(NonInvertibleElementException.class);

        assertThat(minimal.tryReciprocal(0.0)).isEmpty();
        assertThat(minimal.tryReciprocal(2.0)).contains(0.5);
        assertThatThrownBy(() -> minimal.tryReciprocal(null)).isInstanceOf(NullPointerException.class);

        assertThat(minimal.distance(5.0, 2.0)).isCloseTo(3.0, within(1e-12));
        assertThat(minimal.isZero(0.0)).isTrue();
        assertThat(minimal.isZero(1e-12)).isFalse();
        assertThatThrownBy(() -> minimal.isZero(null)).isInstanceOf(NullPointerException.class);

        assertThat(minimal.isApproxZero(0.05, 0.1)).isTrue();
        assertThat(minimal.isApproxZero(0.2, 0.1)).isFalse();
        assertThat(minimal.isApproxZero(1e-10)).isTrue();
        assertThat(minimal.isApproxZero(1e-5)).isFalse();
        assertThat(ScopedValue.where(NumericContext.EPSILON, 1e-4)
            .call(() -> minimal.isApproxZero(1e-5))).isTrue();

        assertThatThrownBy(() -> minimal.isApproxZero(0.0, Double.NaN))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> minimal.isApproxZero(0.0, Double.POSITIVE_INFINITY))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> minimal.isApproxZero(0.0, -1e-6))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(minimal.approxEquals(1.0, 1.05, 0.1)).isTrue();
        assertThat(minimal.approxEquals(1.0, 1.2, 0.1)).isFalse();
        assertThatThrownBy(() -> minimal.approxEquals(null, 1.0, 0.1))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> minimal.approxEquals(1.0, null, 0.1))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> minimal.approxEquals(1.0, 1.0, Double.NaN))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> minimal.approxEquals(1.0, 1.0, Double.NEGATIVE_INFINITY))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> minimal.approxEquals(1.0, 1.0, -0.5))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNumericContextEpsilonAndMathContextScopes() {
        Float64Field f = Float64Field.INSTANCE;
        assertThat(NumericContext.currentEpsilon()).isEqualTo(NumericContext.DEFAULT_EPSILON);
        assertThat(NumericContext.currentMathContext()).isEqualTo(MathContext.DECIMAL128);

        assertThat(f.approxEquals(1.0, 1.0 + 1e-6)).isFalse();
        boolean withinCustomScope = ScopedValue.where(NumericContext.EPSILON, 1e-5)
            .call(() -> f.approxEquals(1.0, 1.0 + 1e-6));
        assertThat(withinCustomScope).isTrue();
        assertThat(ScopedValue.where(NumericContext.EPSILON, 0.0)
            .call(NumericContext::currentEpsilon)).isEqualTo(0.0);

        assertThatThrownBy(() -> ScopedValue.where(NumericContext.EPSILON, null)
            .call(NumericContext::currentEpsilon))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ScopedValue.where(NumericContext.EPSILON, Double.NaN)
            .call(NumericContext::currentEpsilon))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ScopedValue.where(NumericContext.EPSILON, Double.POSITIVE_INFINITY)
            .call(NumericContext::currentEpsilon))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ScopedValue.where(NumericContext.EPSILON, Double.NEGATIVE_INFINITY)
            .call(NumericContext::currentEpsilon))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ScopedValue.where(NumericContext.EPSILON, -0.1)
            .call(NumericContext::currentEpsilon))
            .isInstanceOf(IllegalStateException.class);

        assertThat(ScopedValue.where(NumericContext.MATH_CONTEXT, MathContext.DECIMAL64)
            .call(NumericContext::currentMathContext))
            .isEqualTo(MathContext.DECIMAL64);
        assertThatThrownBy(() -> ScopedValue.where(NumericContext.MATH_CONTEXT, null)
            .call(NumericContext::currentMathContext))
            .isInstanceOf(IllegalStateException.class);
    }
}
