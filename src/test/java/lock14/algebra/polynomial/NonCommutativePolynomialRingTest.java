package lock14.algebra.polynomial;

import lock14.algebra.laws.RingLaws;
import lock14.algebra.numbers.Quaternion;
import lock14.algebra.numbers.QuaternionDivisionRing;
import lock14.algebra.numbers.Rational;
import lock14.algebra.structure.Ring;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class NonCommutativePolynomialRingTest extends RingLaws<Polynomial<Quaternion<Rational>>> {

    private final QuaternionDivisionRing<Rational> hq = QuaternionDivisionRing.overRationals();
    private final NonCommutativePolynomialRing<Quaternion<Rational>> hqx = new NonCommutativePolynomialRing<>(hq);

    @Override
    protected Ring<Polynomial<Quaternion<Rational>>> ring() {
        return hqx;
    }

    @Provide
    Arbitrary<Polynomial<Quaternion<Rational>>> elements() {
        Arbitrary<Rational> r = Arbitraries.integers().between(-6, 6).map(Rational::of);
        Arbitrary<Quaternion<Rational>> q = r.list().ofSize(4)
            .map(c -> hq.of(c.get(0), c.get(1), c.get(2), c.get(3)));
        return q.list().ofMinSize(1).ofMaxSize(4)
            .map(coeffs -> Polynomial.of(hq, coeffs));
    }

    @Test
    public void testNonCommutativeCoefficientMultiplication() {
        Polynomial<Quaternion<Rational>> ix = hqx.of(hq.zero(), hq.i());
        Polynomial<Quaternion<Rational>> jx = hqx.of(hq.zero(), hq.j());

        // (i*x) * (j*x) = k*x^2 != -k*x^2 = (j*x) * (i*x)
        assertThat(ix.multiply(jx)).isEqualTo(hqx.of(hq.zero(), hq.zero(), hq.k()));
        assertThat(jx.multiply(ix)).isEqualTo(hqx.of(hq.zero(), hq.zero(), hq.k().negate()));
    }
}
