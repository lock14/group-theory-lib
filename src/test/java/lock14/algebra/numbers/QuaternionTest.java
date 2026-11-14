package lock14.algebra.numbers;

import java.math.BigInteger;
import lock14.algebra.laws.DivisionRingLaws;
import lock14.algebra.structure.DivisionRing;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class QuaternionTest extends DivisionRingLaws<Quaternion<Rational>> {

    private final QuaternionDivisionRing<Rational> hq = QuaternionDivisionRing.overRationals();

    @Override
    protected DivisionRing<Quaternion<Rational>> divisionRing() {
        return hq;
    }

    @Provide
    Arbitrary<Quaternion<Rational>> elements() {
        Arbitrary<Rational> coords = Arbitraries.integers().between(-15, 15).map(Rational::of);
        return coords.list().ofSize(4)
            .map(c -> hq.of(c.get(0), c.get(1), c.get(2), c.get(3)));
    }

    @Provide
    Arbitrary<Quaternion<Rational>> nonZeroElements() {
        return elements().filter(q -> !q.isZero());
    }

    @Test
    public void testHamiltonBasisIdentitiesAndLipschitzQuaternions() {
        Quaternion<Rational> i = hq.i();
        Quaternion<Rational> j = hq.j();
        Quaternion<Rational> k = hq.k();
        Quaternion<Rational> minusOne = hq.negate(hq.one());

        assertThat(i.multiply(i)).isEqualTo(minusOne);
        assertThat(j.multiply(j)).isEqualTo(minusOne);
        assertThat(k.multiply(k)).isEqualTo(minusOne);
        assertThat(i.multiply(j).multiply(k)).isEqualTo(minusOne);

        assertThat(i.multiply(j)).isEqualTo(k);
        assertThat(j.multiply(i)).isEqualTo(k.negate());

        // Exact rational reciprocal without any floating-point rounding!
        Quaternion<Rational> q = Quaternion.ofRational(1, 2, 3, 4);
        assertThat(q.normSq()).isEqualTo(Rational.of(30));
        assertThat(q.multiply(q.reciprocal())).isEqualTo(hq.one());
        assertThat(q.reciprocal().multiply(q)).isEqualTo(hq.one());

        // Lipschitz integer quaternions over Z
        QuaternionRing<BigInteger> lipschitz = new QuaternionRing<>(Integers.RING);
        Quaternion<BigInteger> a = lipschitz.of(
            BigInteger.ONE, BigInteger.TWO, BigInteger.ZERO, BigInteger.valueOf(-1)
        );
        Quaternion<BigInteger> b = lipschitz.of(
            BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(3), BigInteger.ONE
        );
        assertThat(lipschitz.multiply(a, b)).isNotEqualTo(lipschitz.multiply(b, a));
        assertThat(lipschitz.isUnit(lipschitz.i())).isTrue();
        assertThat(lipschitz.isUnit(a)).isFalse();
    }
}
