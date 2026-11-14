package lock14.algebra.numeric;

import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.exceptions.UndefinedOperationException;
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
        return Arbitraries.doubles().between(-100.0, 100.0)
            .flatMap(re -> Arbitraries.doubles().between(-100.0, 100.0).map(im -> Complex64.of(re, im)));
    }

    @Provide
    Arbitrary<Complex64> nonZeroElements() {
        return elements().filter(c -> c.abs() > 1e-4);
    }

    @Test
    public void testEulerPolarSqrtPowAndSmithScaling() {
        Complex64 c = Complex64.ofPolar(2.0, Math.PI / 2.0);
        assertThat(c.real()).isCloseTo(0.0, within(1e-12));
        assertThat(c.imaginary()).isCloseTo(2.0, within(1e-12));

        // Euler's identity: e^(i * pi) + 1 = 0
        assertThat(Complex64.of(0.0, Math.PI).exp().add(Complex64.ONE).abs()).isLessThan(1e-14);

        // Principal square root of i is (1 + i)/sqrt(2)
        Complex64 rootI = Complex64.I.sqrt();
        assertThat(rootI.multiply(rootI).approxEquals(Complex64.I, 1e-12)).isTrue();

        assertThat(Complex64.ZERO.pow(Complex64.ZERO)).isEqualTo(Complex64.ONE);
        assertThat(Complex64.ZERO.pow(Complex64.of(2.0, 3.0))).isEqualTo(Complex64.ZERO);
        assertThatThrownBy(() -> Complex64.ZERO.pow(Complex64.I))
            .isInstanceOf(UndefinedOperationException.class);
        assertThatThrownBy(() -> Complex64.ONE.divide(Complex64.ZERO))
            .isInstanceOf(NonInvertibleElementException.class);

        // Extreme magnitude Smith division (c^2 + d^2 would overflow IEEE-754 double!)
        Complex64 hugeA = Complex64.of(3e250, 4e250);
        Complex64 hugeB = Complex64.of(3e250, 4e250);
        assertThat(hugeA.divide(hugeB).approxEquals(Complex64.ONE, 1e-12)).isTrue();

        Complex64 hugeInv = hugeA.reciprocal();
        assertThat(hugeA.multiply(hugeInv).approxEquals(Complex64.ONE, 1e-12)).isTrue();
    }
}
