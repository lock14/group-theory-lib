package lock14.algebra.discrete;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.laws.CommutativeRingLaws;
import lock14.algebra.numbers.Quaternion;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.RationalField;
import lock14.algebra.structure.CommutativeRing;
import static org.assertj.core.api.Assertions.assertThat;

public class PolynomialTest extends CommutativeRingLaws<Polynomial<Rational>> {

    private final PolynomialRing<Rational> polyRing = new PolynomialRing<>(RationalField.INSTANCE);

    @Override
    protected CommutativeRing<Polynomial<Rational>> commutativeRing() {
        return polyRing;
    }

    @Provide
    Arbitrary<Polynomial<Rational>> elements() {
        Arbitrary<Rational> rationals = Arbitraries.integers().between(-10, 10)
                .map(Rational::of);
        return rationals.list().ofMinSize(1).ofMaxSize(5)
                .map(coeffs -> Polynomial.of(RationalField.INSTANCE, coeffs));
    }

    @Test
    public void testHornerEvaluation() {
        // P(x) = 1 + 2x + 3x^2
        Polynomial<Rational> p = Polynomial.of(RationalField.INSTANCE,
                Rational.of(1), Rational.of(2), Rational.of(3));

        // P(2) = 1 + 2(2) + 3(4) = 1 + 4 + 12 = 17
        Rational eval = p.evaluate(Rational.of(2));
        assertThat(eval).isEqualTo(Rational.of(17));
    }

    @Test
    public void testMultiplication() {
        // (x + 1)(x - 1) = x^2 - 1
        Polynomial<Rational> p1 = Polynomial.of(RationalField.INSTANCE, Rational.of(1), Rational.of(1));
        Polynomial<Rational> p2 = Polynomial.of(RationalField.INSTANCE, Rational.of(-1), Rational.of(1));
        Polynomial<Rational> prod = p1.multiply(p2);

        Polynomial<Rational> expected = Polynomial.of(RationalField.INSTANCE, Rational.of(-1), Rational.of(0), Rational.of(1));
        assertThat(prod).isEqualTo(expected);
    }

    @Test
    public void testNonCommutativePolynomialMultiplication() {
        // In H[x], (i*x) * j = (i*j)*x = k*x, whereas j * (i*x) = (j*i)*x = -k*x
        GeneralPolynomialRing<Quaternion> hPoly = new GeneralPolynomialRing<>(Quaternion.ring());
        Polynomial<Quaternion> ix = Polynomial.of(Quaternion.ring(), Quaternion.ZERO, Quaternion.I);
        Polynomial<Quaternion> j = Polynomial.constant(Quaternion.ring(), Quaternion.J);

        assertThat(hPoly.multiply(ix, j)).isEqualTo(Polynomial.of(Quaternion.ring(), Quaternion.ZERO, Quaternion.K));
        assertThat(hPoly.multiply(j, ix)).isEqualTo(Polynomial.of(Quaternion.ring(), Quaternion.ZERO, Quaternion.K.negate()));
    }

    @Test
    public void testDerivative() {
        // P(x) = 5 + 4x + 3x^2 + 2x^3 -> P'(x) = 4 + 6x + 6x^2
        Polynomial<Rational> p = Polynomial.of(RationalField.INSTANCE,
                Rational.of(5), Rational.of(4), Rational.of(3), Rational.of(2));
        Polynomial<Rational> deriv = p.derivative();

        Polynomial<Rational> expected = Polynomial.of(RationalField.INSTANCE,
                Rational.of(4), Rational.of(6), Rational.of(6));
        assertThat(deriv).isEqualTo(expected);
    }

    @Test
    public void testNullInteriorCoefficientRejectedAtConstruction() {
        java.util.List<Rational> coeffsWithNull = java.util.Arrays.asList(null, Rational.ONE);
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> Polynomial.of(RationalField.INSTANCE, coeffsWithNull)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testForeignCoefficientRingOperandsRejected() {
        lock14.algebra.numbers.ZModnRing z5 = new lock14.algebra.numbers.ZModnRing(5);
        lock14.algebra.numbers.ZModnRing z7 = new lock14.algebra.numbers.ZModnRing(7);
        PolynomialRing<lock14.algebra.numbers.ModuloInteger> z5x = new PolynomialRing<>(z5);
        GeneralPolynomialRing<lock14.algebra.numbers.ModuloInteger> genZ5x = new GeneralPolynomialRing<>(z5);

        Polynomial<lock14.algebra.numbers.ModuloInteger> p7a = Polynomial.of(z7, z7.element(1), z7.element(2));
        Polynomial<lock14.algebra.numbers.ModuloInteger> p7b = Polynomial.of(z7, z7.element(3));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> z5x.add(p7a, p7b))
                .isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> z5x.isZero(p7a))
                .isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> genZ5x.multiply(p7a, p7b))
                .isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> genZ5x.isOne(p7a))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
