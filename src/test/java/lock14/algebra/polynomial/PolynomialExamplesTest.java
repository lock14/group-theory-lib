package lock14.algebra.polynomial;

import static org.assertj.core.api.Assertions.assertThat;

import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import lock14.algebra.structure.EuclideanDomain;
import org.junit.jupiter.api.Test;

class PolynomialExamplesTest {

    @Test
    void euclideanDivisionAndGcdExample() {
        // @start region="polynomial-euclidean"
        PolynomialEuclideanDomain<Rational> qx = new PolynomialEuclideanDomain<>(Rationals.FIELD);
        // p(x) = x^2 - 1 = (-1) + 0*x + 1*x^2
        Polynomial<Rational> p = qx.of(Rational.of(-1), Rational.ZERO, Rational.ONE);
        // d(x) = x^2 + 2x + 1 = 1 + 2*x + 1*x^2
        Polynomial<Rational> d = qx.of(Rational.ONE, Rational.TWO, Rational.ONE);

        Polynomial<Rational> monicGcd = qx.gcd(p, d);
        EuclideanDomain.DivisionResult<Polynomial<Rational>> qr = qx.divideAndRemainder(p, monicGcd);
        Rational valueAtThree = p.evaluate(Rational.of(3));
        // @end

        // monic gcd(x^2 - 1, (x + 1)^2) = x + 1
        assertThat(monicGcd).isEqualTo(qx.of(Rational.ONE, Rational.ONE));
        assertThat(qr.quotient()).isEqualTo(qx.of(Rational.of(-1), Rational.ONE));
        assertThat(qr.remainder().isZero()).isTrue();
        assertThat(valueAtThree).isEqualTo(Rational.of(8));
    }
}
