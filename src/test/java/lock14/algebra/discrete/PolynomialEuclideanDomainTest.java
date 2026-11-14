package lock14.algebra.discrete;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.laws.EuclideanDomainLaws;
import lock14.algebra.linear.SquareMatrix;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.RationalField;
import lock14.algebra.numbers.ZModpField;
import lock14.algebra.structure.EuclideanDomain;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PolynomialEuclideanDomainTest extends EuclideanDomainLaws<Polynomial<Rational>> {

    private final PolynomialEuclideanDomain<Rational> qx = PolynomialRing.overField(RationalField.INSTANCE);

    @Override
    protected EuclideanDomain<Polynomial<Rational>> euclideanDomain() {
        return qx;
    }

    @Provide
    Arbitrary<Polynomial<Rational>> elements() {
        Arbitrary<Rational> coeffs = Arbitraries.integers().between(-5, 5).map(Rational::of);
        return coeffs.list().ofMinSize(1).ofMaxSize(4)
                .map(list -> Polynomial.of(RationalField.INSTANCE, list));
    }

    @Provide
    Arbitrary<Polynomial<Rational>> nonZeroElements() {
        return elements().filter(p -> !p.isZero());
    }

    @Test
    public void testPolynomialLongDivisionMonicGcdAndUnits() {
        // p(x) = 2(x - 1)(x + 2) = -4 + 2x + 2x^2
        Polynomial<Rational> p = Polynomial.of(RationalField.INSTANCE,
                Rational.of(-4), Rational.of(2), Rational.of(2));
        // q(x) = 3(x - 1)(x - 3) = 9 - 12x + 3x^2
        Polynomial<Rational> q = Polynomial.of(RationalField.INSTANCE,
                Rational.of(9), Rational.of(-12), Rational.of(3));

        // Monic GCD must be (x - 1) = -1 + x
        Polynomial<Rational> expectedGcd = Polynomial.of(RationalField.INSTANCE,
                Rational.of(-1), Rational.of(1));
        assertThat(qx.gcd(p, q)).isEqualTo(expectedGcd);
        assertThat(qx.extendedGcd(p, q).gcd()).isEqualTo(expectedGcd);

        // Units in Q[x] are non-zero constants (degree 0)
        assertThat(qx.isUnit(Polynomial.constant(RationalField.INSTANCE, Rational.of(7, 3)))).isTrue();
        assertThat(qx.isUnit(qx.zero())).isFalse();
        assertThat(qx.isUnit(expectedGcd)).isFalse();
    }

    @Test
    public void testBareissDeterminantOverPolynomialEuclideanDomain() {
        Polynomial<Rational> x = Polynomial.x(RationalField.INSTANCE);
        Polynomial<Rational> c1 = Polynomial.constant(RationalField.INSTANCE, Rational.of(1));
        Polynomial<Rational> c2 = Polynomial.constant(RationalField.INSTANCE, Rational.of(2));
        Polynomial<Rational> c3 = Polynomial.constant(RationalField.INSTANCE, Rational.of(3));

        // 3x3 upper-triangular + coupled matrix over Euclidean domain Q[x]
        SquareMatrix<Polynomial<Rational>> m = SquareMatrix.of(3, qx,
                x.subtract(c1), c2, c1,
                qx.zero(), x.subtract(c2), c3,
                qx.zero(), qx.zero(), x.subtract(c3));
        Polynomial<Rational> expected = x.subtract(c1).multiply(x.subtract(c2)).multiply(x.subtract(c3));
        assertThat(m.determinant()).isEqualTo(expected);
    }

    @Test
    public void testForeignCoefficientFieldRejected() {
        var f5x = PolynomialRing.overField(new ZModpField(5));
        var f7 = new ZModpField(7);
        var foreign = Polynomial.constant(f7, f7.element(3));
        assertThatThrownBy(() -> f5x.normalize(foreign))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> f5x.divideAndRemainder(foreign, f5x.one()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
