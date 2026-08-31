package lock14.algebra.numbers;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.laws.ApproximateFieldLaws;
import lock14.algebra.structure.Field;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class ComplexTest extends ApproximateFieldLaws<Complex> {

    @Override
    protected Field<Complex> field() {
        return ComplexField.INSTANCE;
    }

    @Override
    protected double distance(Complex a, Complex b) {
        return a.subtract(b).abs();
    }

    @Override
    protected double tolerance() {
        return 1e-7;
    }

    @Provide
    Arbitrary<Complex> elements() {
        return Arbitraries.doubles().between(-100.0, 100.0)
            .flatMap(re ->
                Arbitraries.doubles().between(-100.0, 100.0)
                    .map(im -> Complex.of(re, im))
            );
    }

    @Provide
    Arbitrary<Complex> nonZeroElements() {
        return elements().filter(c -> c.abs() > 1e-4);
    }

    @Test
    public void testMultiplyAndDivide() {
        Complex a = Complex.of(2, 3);
        Complex b = Complex.of(4, 5);
        assertThat(a.multiply(b)).isEqualTo(Complex.of(-7, 22));

        Complex quotient = a.divide(b);
        assertThat(quotient.real()).isCloseTo(23.0 / 41.0, within(1e-9));
        assertThat(quotient.imaginary()).isCloseTo(2.0 / 41.0, within(1e-9));
    }

    @Test
    public void testPolarAndEuler() {
        Complex c = Complex.ofPolar(2.0, Math.PI / 2.0);
        assertThat(c.real()).isCloseTo(0.0, within(1e-9));
        assertThat(c.imaginary()).isCloseTo(2.0, within(1e-9));

        // Euler's identity: e^(i * pi) + 1 = 0
        Complex eIpi = Complex.of(0, Math.PI).exp();
        Complex sum = eIpi.add(Complex.ONE);
        assertThat(sum.abs()).isCloseTo(0.0, within(1e-9));
    }

    @Test
    public void testDivisionByZeroThrows() {
        assertThatThrownBy(() -> Complex.ONE.divide(Complex.ZERO))
            .isInstanceOf(ArithmeticException.class);
    }

    @Test
    public void testSmithDivisionExtremeScale() {
        // High magnitude numbers that would overflow c^2 + d^2 in naive division
        Complex a = Complex.of(1e150, 2e150);
        Complex b = Complex.of(3e150, 4e150);
        Complex res = a.divide(b);
        assertThat(Double.isFinite(res.real())).isTrue();
        assertThat(Double.isFinite(res.imaginary())).isTrue();
    }
}
