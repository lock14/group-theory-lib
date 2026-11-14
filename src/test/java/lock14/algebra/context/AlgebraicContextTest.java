package lock14.algebra.context;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.linear.SquareMatrix;
import lock14.algebra.numbers.Complex;
import lock14.algebra.numbers.ComplexField;
import lock14.algebra.numbers.Quaternion;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.RealField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AlgebraicContextTest {

    @Test
    public void testAmbientEpsilonControlsApproximateEqualityAndPivotSingularity() {
        // Default epsilon is 1e-9
        assertThat(RealField.INSTANCE.approxEquals(1.0, 1.0 + 1e-10)).isTrue();
        assertThat(RealField.INSTANCE.approxEquals(1.0, 1.0 + 1e-6)).isFalse();
        assertThat(ComplexField.INSTANCE.approxEquals(Complex.ONE, Complex.of(1.0, 1e-10))).isTrue();
        assertThat(Quaternion.ONE.approxEquals(Quaternion.of(1.0, 0.0, 0.0, 1e-10))).isTrue();

        // Near-singular 3x3 matrix with residual pivot 1e-12
        SquareMatrix<Double> nearSingular = SquareMatrix.of(3, RealField.INSTANCE,
                1.0, 0.0, 0.0,
                0.0, 1.0, 2.0,
                0.0, 2.0, 4.0 + 1e-12);

        ScopedValue.where(AlgebraicContext.EPSILON, 1e-9).run(() -> {
            assertThat(nearSingular.determinant()).isEqualTo(0.0);
            assertThatThrownBy(nearSingular::inverse)
                    .isInstanceOf(NonInvertibleElementException.class);
        });

        ScopedValue.where(AlgebraicContext.EPSILON, 1e-4).run(() -> {
            assertThat(RealField.INSTANCE.approxEquals(1.0, 1.00005)).isTrue();
            assertThat(Complex.ONE.approxEquals(Complex.of(1.0, 5e-5))).isTrue();
        });

        ScopedValue.where(AlgebraicContext.EPSILON, -1.0).run(() -> {
            assertThatThrownBy(AlgebraicContext::currentEpsilon)
                    .isInstanceOf(IllegalStateException.class);
        });
    }

    @Test
    public void testAmbientMathContextControlsRationalBigDecimalConversion() {
        Rational oneThird = Rational.of(1, 3);

        // Unbound defaults to DECIMAL128 (34 digits)
        BigDecimal defaultDec = oneThird.bigDecimalValue();
        assertThat(defaultDec.precision()).isEqualTo(34);

        MathContext mc4 = new MathContext(4, RoundingMode.HALF_UP);
        ScopedValue.where(AlgebraicContext.MATH_CONTEXT, mc4).run(() -> {
            assertThat(oneThird.bigDecimalValue()).isEqualTo(new BigDecimal("0.3333"));
        });

        assertThat(Rational.ZERO.bigDecimalValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void testJpmsNamedModuleDescriptor() {
        Module mod = AlgebraicContext.class.getModule();
        assertThat(mod.isNamed()).isTrue();
        assertThat(mod.getName()).isEqualTo("lock14.algebra");
        assertThat(mod.getDescriptor().exports())
                .extracting(java.lang.module.ModuleDescriptor.Exports::source)
                .containsExactlyInAnyOrder(
                        "lock14.algebra.context",
                        "lock14.algebra.discrete",
                        "lock14.algebra.element",
                        "lock14.algebra.exceptions",
                        "lock14.algebra.linear",
                        "lock14.algebra.numbers",
                        "lock14.algebra.structure"
                );
    }
}
