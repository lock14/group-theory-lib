package lock14.algebra.numbers;

import java.math.BigInteger;
import lock14.algebra.laws.EuclideanDomainLaws;
import lock14.algebra.structure.EuclideanDomain;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class GaussianIntegersTest extends EuclideanDomainLaws<Complex<BigInteger>> {

    private final GaussianIntegers zi = GaussianIntegers.DOMAIN;

    @Override
    protected EuclideanDomain<Complex<BigInteger>> euclideanDomain() {
        return zi;
    }

    @Provide
    Arbitrary<Complex<BigInteger>> elements() {
        Arbitrary<BigInteger> coords = Arbitraries.bigIntegers().between(BigInteger.valueOf(-80), BigInteger.valueOf(80));
        return coords.flatMap(re -> coords.map(im -> zi.of(re, im)));
    }

    @Provide
    Arbitrary<Complex<BigInteger>> nonZeroElements() {
        return elements().filter(z -> !z.isZero());
    }

    @Property
    public void remainderHasStrictlySmallerNormThanDivisor(
            @ForAll("elements") Complex<BigInteger> a,
            @ForAll("nonZeroElements") Complex<BigInteger> b) {
        var qr = zi.divideAndRemainder(a, b);
        assertThat(qr.remainder().normSq()).isLessThan(b.normSq());
    }

    @Test
    public void testUnitsFirstQuadrantAssociateAndGaussianPrimeFactorization() {
        assertThat(zi.isUnit(zi.of(1, 0))).isTrue();
        assertThat(zi.isUnit(zi.of(-1, 0))).isTrue();
        assertThat(zi.isUnit(zi.of(0, 1))).isTrue();
        assertThat(zi.isUnit(zi.of(0, -1))).isTrue();
        assertThat(zi.isUnit(zi.of(1, 1))).isFalse();

        // All four associates of 3 + 2i normalize to 3 + 2i in the first quadrant
        Complex<BigInteger> canonical = zi.of(3, 2);
        assertThat(zi.normalize(zi.of(3, 2))).isEqualTo(canonical);
        assertThat(zi.normalize(zi.of(-2, 3))).isEqualTo(canonical);
        assertThat(zi.normalize(zi.of(-3, -2))).isEqualTo(canonical);
        assertThat(zi.normalize(zi.of(2, -3))).isEqualTo(canonical);

        // 5 = (2 + i)(2 - i) in Z[i], so gcd(5, 2 + i) = 2 + i
        assertThat(zi.gcd(zi.of(5, 0), zi.of(2, 1))).isEqualTo(zi.of(2, 1));
    }
}
