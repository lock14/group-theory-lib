package lock14.algebra.numeric;

import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.ApproximateFieldLaws;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class Complex64Test extends ApproximateFieldLaws<Complex64> {

    @Override
    protected ApproximateField<Complex64> field() {
        return Complex64.field();
    }

    @Override
    protected double tolerance() {
        return 1e-7;
    }

    @Provide
    Arbitrary<Complex64> elements() {
        Arbitrary<Complex64> random = Arbitraries.doubles().between(-100.0, 100.0)
            .flatMap(re -> Arbitraries.doubles().between(-100.0, 100.0).map(im -> Complex64.of(re, im)));
        return Arbitraries.oneOf(
            Arbitraries.of(
                Complex64.ZERO,
                Complex64.ONE,
                Complex64.I,
                Complex64.ONE.negate(),
                Complex64.I.negate()
            ),
            random
        );
    }

    @Provide
    Arbitrary<Complex64> nonZeroElements() {
        return elements().filter(c -> c.abs() > 1e-4);
    }

    @Test
    public void testFactoriesComponentsAndSignedZeroCanonicalization() {
        Complex64 canonicalZero = new Complex64(-0.0, -0.0);
        assertThat(Double.doubleToRawLongBits(canonicalZero.real())).isZero();
        assertThat(Double.doubleToRawLongBits(canonicalZero.imaginary())).isZero();
        assertThat(canonicalZero.isZero()).isTrue();

        Complex64 purelyReal = Complex64.ofReal(3.5);
        assertThat(purelyReal.real()).isEqualTo(3.5);
        assertThat(purelyReal.imaginary()).isEqualTo(0.0);
        assertThat(purelyReal.isZero()).isFalse();

        Complex64 purelyImag = Complex64.ofImaginary(-2.5);
        assertThat(purelyImag.real()).isEqualTo(0.0);
        assertThat(purelyImag.imaginary()).isEqualTo(-2.5);
        assertThat(purelyImag.isZero()).isFalse();

        assertThat(Complex64.ofPolar(0.0, 2.0)).isEqualTo(Complex64.ZERO);
        Complex64 polar = Complex64.ofPolar(2.0, Math.PI / 2.0);
        assertThat(polar.real()).isCloseTo(0.0, within(1e-12));
        assertThat(polar.imaginary()).isCloseTo(2.0, within(1e-12));
        assertThatThrownBy(() -> Complex64.ofPolar(-1.0, 0.0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Complex64.ofPolar(Double.NaN, 0.0))
            .isInstanceOf(IllegalArgumentException.class);

        Complex64 z = Complex64.of(3.0, -4.0);
        assertThat(z.normSq()).isEqualTo(25.0);
        assertThat(z.abs()).isEqualTo(5.0);
        assertThat(Complex64.I.arg()).isCloseTo(Math.PI / 2.0, within(1e-12));
        assertThat(z.conjugate()).isEqualTo(Complex64.of(3.0, 4.0));
        assertThat(z.scale(2.5)).isEqualTo(Complex64.of(7.5, -10.0));
        assertThat(z.scale(0.0)).isEqualTo(Complex64.ZERO);
    }

    @Test
    public void testEulerPolarSqrtPowAndSmithScaling() {
        // Euler's identity: e^(i * pi) + 1 = 0
        assertThat(Complex64.of(0.0, Math.PI).exp().add(Complex64.ONE).abs()).isLessThan(1e-14);

        // All 5 principal square root branches: zero, Q1, Q4, Q2 (including negative real), and Q3
        assertThat(Complex64.ZERO.sqrt()).isEqualTo(Complex64.ZERO);
        Complex64 rootI = Complex64.I.sqrt();
        assertThat(rootI.multiply(rootI).approxEquals(Complex64.I, 1e-12)).isTrue();
        assertThat(Complex64.of(3.0, 4.0).sqrt().approxEquals(Complex64.of(2.0, 1.0), 1e-12)).isTrue();
        assertThat(Complex64.of(3.0, -4.0).sqrt().approxEquals(Complex64.of(2.0, -1.0), 1e-12)).isTrue();
        assertThat(Complex64.of(-4.0, 0.0).sqrt().approxEquals(Complex64.of(0.0, 2.0), 1e-12)).isTrue();
        assertThat(Complex64.of(-3.0, 4.0).sqrt().approxEquals(Complex64.of(1.0, 2.0), 1e-12)).isTrue();
        assertThat(Complex64.of(-3.0, -4.0).sqrt().approxEquals(Complex64.of(1.0, -2.0), 1e-12)).isTrue();

        assertThat(Complex64.ZERO.tryLog()).isEmpty();
        assertThat(Complex64.ONE.tryLog()).contains(Complex64.ZERO);
        assertThat(Complex64.ofReal(Math.E).log().approxEquals(Complex64.ONE, 1e-12)).isTrue();
        assertThatThrownBy(Complex64.ZERO::log).isInstanceOf(ArithmeticException.class);

        // Zero and non-zero complex power branches, including i^i = e^(-pi/2)
        assertThat(Complex64.ZERO.pow(Complex64.ZERO)).isEqualTo(Complex64.ONE);
        assertThat(Complex64.ZERO.pow(Complex64.of(2.0, 3.0))).isEqualTo(Complex64.ZERO);
        assertThat(Complex64.ZERO.tryPow(Complex64.I)).isEmpty();
        assertThat(Complex64.ZERO.tryPow(Complex64.of(-1.0, 2.0))).isEmpty();
        assertThatThrownBy(() -> Complex64.ZERO.pow(Complex64.I))
            .isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> Complex64.ZERO.pow(Complex64.of(-1.0, 0.0)))
            .isInstanceOf(ArithmeticException.class);
        assertThat(Complex64.of(2.0, 1.0).pow(Complex64.of(2.0, 0.0))
            .approxEquals(Complex64.of(3.0, 4.0), 1e-12)).isTrue();
        assertThat(Complex64.I.pow(Complex64.I)
            .approxEquals(Complex64.ofReal(Math.exp(-Math.PI / 2.0)), 1e-12)).isTrue();

        assertThat(Complex64.ZERO.tryReciprocal()).isEmpty();
        assertThat(Complex64.ofReal(2.0).tryReciprocal()).contains(Complex64.ofReal(0.5));
        assertThatThrownBy(Complex64.ZERO::reciprocal)
            .isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> Complex64.ONE.divide(Complex64.ZERO))
            .isInstanceOf(NonInvertibleElementException.class);

        // Both Smith scaling branches: |c| < |d| and |c| >= |d| at extreme magnitudes
        Complex64 hugeImagDominant = Complex64.of(3e250, 4e250);
        assertThat(hugeImagDominant.divide(hugeImagDominant).approxEquals(Complex64.ONE, 1e-12)).isTrue();
        assertThat(hugeImagDominant.multiply(hugeImagDominant.reciprocal())
            .approxEquals(Complex64.ONE, 1e-12)).isTrue();

        Complex64 hugeRealDominant = Complex64.of(4e250, 3e250);
        assertThat(hugeRealDominant.divide(hugeRealDominant).approxEquals(Complex64.ONE, 1e-12)).isTrue();
        assertThat(hugeRealDominant.multiply(hugeRealDominant.reciprocal())
            .approxEquals(Complex64.ONE, 1e-12)).isTrue();

        Complex64 tinyImagDominant = Complex64.of(3e-250, 4e-250);
        assertThat(tinyImagDominant.divide(tinyImagDominant).approxEquals(Complex64.ONE, 1e-12)).isTrue();
        assertThat(tinyImagDominant.multiply(tinyImagDominant.reciprocal())
            .approxEquals(Complex64.ONE, 1e-12)).isTrue();
    }

    @Test
    public void testApproxEqualsEqualityHashCodeFormattingAndNullGuards() {
        Complex64 base = Complex64.of(1.0, 2.0);
        assertThat(base.approxEquals(Complex64.of(1.0, 2.0 + 1e-12))).isTrue();
        assertThat(base.approxEquals(Complex64.of(1.0, 2.0 + 1e-5))).isFalse();
        assertThat(ScopedValue.where(NumericContext.EPSILON, 1e-4)
            .call(() -> base.approxEquals(Complex64.of(1.0, 2.0 + 1e-5)))).isTrue();

        assertThat(Complex64.of(1.0, -0.0)).isEqualTo(Complex64.ONE);
        assertThat(Complex64.of(1.0, -0.0).hashCode()).isEqualTo(Complex64.ONE.hashCode());
        assertThat(Complex64.ONE).isNotEqualTo(Complex64.I);
        assertThat(Complex64.ONE).isNotEqualTo(null);
        assertThat(Complex64.ONE).isNotEqualTo("1.0");

        assertThat(Complex64.ZERO.toString()).isEqualTo("0.0");
        assertThat(Complex64.ofReal(-3.5).toString()).isEqualTo("-3.5");
        assertThat(Complex64.ofImaginary(2.5).toString()).isEqualTo("2.5i");
        assertThat(Complex64.ofImaginary(-2.5).toString()).isEqualTo("-2.5i");
        assertThat(Complex64.of(3.0, 4.0).toString()).isEqualTo("3.0 + 4.0i");
        assertThat(Complex64.of(3.0, -4.0).toString()).isEqualTo("3.0 - 4.0i");

        assertThatThrownBy(() -> base.add(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> base.subtract(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> base.multiply(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> base.divide(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> base.tryPow(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> base.pow(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> base.approxEquals(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> base.approxEquals(base, -1.0)).isInstanceOf(IllegalArgumentException.class);

        ApproximateField<Complex64> f = Complex64.field();
        assertThat(f.tryReciprocal(Complex64.ofReal(2.0))).contains(Complex64.ofReal(0.5));
        assertThatThrownBy(() -> f.add(null, base)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.negate(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.subtract(null, base)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.multiply(null, base)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.reciprocal(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.divide(null, base)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.abs(null)).isInstanceOf(NullPointerException.class);
    }
}
