package lock14.algebra.polynomial;

import lock14.algebra.laws.EuclideanDomainLaws;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import lock14.algebra.structure.EuclideanDomain;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class PolynomialEuclideanDomainTest extends EuclideanDomainLaws<Polynomial<Rational>> {

    private final PolynomialEuclideanDomain<Rational> qx = PolynomialRing.overField(Rationals.FIELD);

    @Override
    protected EuclideanDomain<Polynomial<Rational>> euclideanDomain() {
        return qx;
    }

    @Provide
    Arbitrary<Polynomial<Rational>> elements() {
        Arbitrary<Rational> coeffs = Arbitraries.integers().between(-8, 8).map(Rational::of);
        return coeffs.list().ofMinSize(1).ofMaxSize(4).map(qx::of);
    }

    @Provide
    Arbitrary<Polynomial<Rational>> nonZeroElements() {
        return elements().filter(p -> !p.isZero());
    }

    @Test
    public void testMonicGcdAndBezoutOverRationals() {
        Polynomial<Rational> x = qx.x();
        Polynomial<Rational> xMinus1 = x.subtract(qx.constant(Rational.ONE));
        Polynomial<Rational> xPlus2 = x.add(qx.constant(Rational.TWO));
        Polynomial<Rational> xPlus3 = x.add(qx.constant(Rational.of(3)));

        Polynomial<Rational> a = xMinus1.multiply(xPlus2).scale(Rational.of(4));
        Polynomial<Rational> b = xMinus1.multiply(xPlus3).scale(Rational.of(-6));

        assertThat(qx.gcd(a, b)).isEqualTo(xMinus1);
    }
}
