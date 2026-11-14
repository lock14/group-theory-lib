package lock14.algebra.numbers;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import lock14.algebra.structure.EuclideanDomain;
import org.junit.jupiter.api.Test;

class NumbersExamplesTest {

    @Test
    void gaussianGcdAndExactQuaternionsExample() {
        // @start region="gaussian-and-quaternions"
        GaussianIntegers zi = GaussianIntegers.DOMAIN;
        Complex<BigInteger> a = zi.of(11, 3);
        Complex<BigInteger> b = zi.of(1, 8);
        EuclideanDomain.ExtendedGcdResult<Complex<BigInteger>> bezout = zi.extendedGcd(a, b);

        QuaternionDivisionRing<Rational> hq = QuaternionDivisionRing.overRationals();
        Quaternion<Rational> q = hq.of(
                Rational.of(1), Rational.of(2), Rational.of(-1), Rational.of(3));
        Quaternion<Rational> inv = q.reciprocal();
        // @end

        assertThat(a.multiply(bezout.x()).add(b.multiply(bezout.y()))).isEqualTo(bezout.gcd());
        assertThat(q.multiply(inv)).isEqualTo(hq.one());
        assertThat(inv.multiply(q)).isEqualTo(hq.one());
    }
}
