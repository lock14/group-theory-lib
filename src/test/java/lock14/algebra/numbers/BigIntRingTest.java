package lock14.algebra.numbers;

import java.math.BigInteger;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.laws.CommutativeRingLaws;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.EuclideanDomain;
import static org.assertj.core.api.Assertions.assertThat;

public class BigIntRingTest extends CommutativeRingLaws<BigInteger> {

    @Override
    protected CommutativeRing<BigInteger> commutativeRing() {
        return BigIntRing.INSTANCE;
    }

    @Provide
    Arbitrary<BigInteger> elements() {
        return Arbitraries.bigIntegers().between(BigInteger.valueOf(-10000), BigInteger.valueOf(10000));
    }

    @Test
    public void testEuclideanDivisionAndGcd() {
        BigIntRing ring = BigIntRing.INSTANCE;
        BigInteger a = BigInteger.valueOf(105);
        BigInteger b = BigInteger.valueOf(24);

        EuclideanDomain.DivisionResult<BigInteger> div = ring.divideAndRemainder(a, b);
        assertThat(div.quotient()).isEqualTo(BigInteger.valueOf(4));
        assertThat(div.remainder()).isEqualTo(BigInteger.valueOf(9));

        assertThat(ring.gcd(a, b)).isEqualTo(BigInteger.valueOf(3));
        assertThat(ring.lcm(a, b)).isEqualTo(BigInteger.valueOf(840));

        EuclideanDomain.ExtendedGcdResult<BigInteger> ext = ring.extendedGcd(a, b);
        assertThat(ext.gcd()).isEqualTo(BigInteger.valueOf(3));
        assertThat(a.multiply(ext.x()).add(b.multiply(ext.y()))).isEqualTo(ext.gcd());
    }
}
