package lock14.algebra.numbers;

import java.math.BigInteger;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.EuclideanDomainLaws;
import lock14.algebra.structure.EuclideanDomain;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IntegersTest extends EuclideanDomainLaws<BigInteger> {

    @Override
    protected EuclideanDomain<BigInteger> euclideanDomain() {
        return Integers.RING;
    }

    @Provide
    Arbitrary<BigInteger> elements() {
        return Arbitraries.bigIntegers().between(BigInteger.valueOf(-100_000), BigInteger.valueOf(100_000));
    }

    @Provide
    Arbitrary<BigInteger> nonZeroElements() {
        return elements().filter(b -> b.signum() != 0);
    }

    @Property
    public void leastNonNegativeCanonicalRemainder(
            @ForAll("elements") BigInteger a,
            @ForAll("nonZeroElements") BigInteger b) {
        var qr = Integers.RING.divideAndRemainder(a, b);
        assertThat(qr.remainder()).isGreaterThanOrEqualTo(BigInteger.ZERO);
        assertThat(qr.remainder()).isLessThan(b.abs());
    }

    @Test
    public void testNegativeDividendAndDivisorEuclideanDivision() {
        // -7 = (-2)*5 + 3
        var qr1 = Integers.RING.divideAndRemainder(BigInteger.valueOf(-7), BigInteger.valueOf(5));
        assertThat(qr1.quotient()).isEqualTo(BigInteger.valueOf(-2));
        assertThat(qr1.remainder()).isEqualTo(BigInteger.valueOf(3));

        // -7 = 2*(-5) + 3
        var qr2 = Integers.RING.divideAndRemainder(BigInteger.valueOf(-7), BigInteger.valueOf(-5));
        assertThat(qr2.quotient()).isEqualTo(BigInteger.valueOf(2));
        assertThat(qr2.remainder()).isEqualTo(BigInteger.valueOf(3));

        assertThat(Integers.RING.lcm(BigInteger.ZERO, BigInteger.valueOf(5))).isEqualTo(BigInteger.ZERO);
        assertThat(Integers.RING.lcm(BigInteger.valueOf(5), BigInteger.ZERO)).isEqualTo(BigInteger.ZERO);
        assertThat(Integers.RING.lcm(BigInteger.valueOf(-6), BigInteger.valueOf(4)))
            .isEqualTo(BigInteger.valueOf(12));
        assertThatThrownBy(() -> Integers.RING.divideAndRemainder(BigInteger.ONE, BigInteger.ZERO))
            .isInstanceOf(NonInvertibleElementException.class);
        assertThat(Integers.RING.toString()).isEqualTo("Z");
    }
}
