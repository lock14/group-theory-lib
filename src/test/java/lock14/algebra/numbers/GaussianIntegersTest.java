package lock14.algebra.numbers;

import java.math.BigInteger;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.EuclideanDomainLaws;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.EuclideanDomain;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        assertThat(zi.normalize(zi.zero())).isEqualTo(zi.zero());
        assertThat(zi.normalize(zi.of(-3, 0))).isEqualTo(zi.of(3, 0));
        assertThat(zi.normalize(zi.of(0, -3))).isEqualTo(zi.of(3, 0));
        assertThat(zi.normalize(zi.of(0, 3))).isEqualTo(zi.of(3, 0));

        // All four associates of 3 + 2i normalize to 3 + 2i in the first quadrant
        Complex<BigInteger> canonical = zi.of(3, 2);
        assertThat(zi.normalize(zi.of(3, 2))).isEqualTo(canonical);
        assertThat(zi.normalize(zi.of(-2, 3))).isEqualTo(canonical);
        assertThat(zi.normalize(zi.of(-3, -2))).isEqualTo(canonical);
        assertThat(zi.normalize(zi.of(2, -3))).isEqualTo(canonical);

        // 5 = (2 + i)(2 - i) in Z[i], so gcd(5, 2 + i) = 2 + i
        assertThat(zi.gcd(zi.of(5, 0), zi.of(2, 1))).isEqualTo(zi.of(2, 1));
        assertThat(zi.lcm(zi.zero(), zi.one())).isEqualTo(zi.zero());
        assertThat(zi.lcm(zi.one(), zi.zero())).isEqualTo(zi.zero());
        assertThat(zi.lcm(zi.of(2, 0), zi.of(0, 3))).isEqualTo(zi.of(6, 0));

        assertThatThrownBy(() -> zi.divideAndRemainder(zi.one(), zi.zero()))
            .isInstanceOf(NonInvertibleElementException.class);

        record AltBigIntRing() implements CommutativeRing<BigInteger> {
            @Override
            public BigInteger zero() {
                return BigInteger.ZERO;
            }

            @Override
            public BigInteger one() {
                return BigInteger.ONE;
            }

            @Override
            public BigInteger add(BigInteger a, BigInteger b) {
                return a.add(b);
            }

            @Override
            public BigInteger negate(BigInteger a) {
                return a.negate();
            }

            @Override
            public BigInteger multiply(BigInteger a, BigInteger b) {
                return a.multiply(b);
            }
        }

        Complex<BigInteger> foreign = Complex.of(new AltBigIntRing(), BigInteger.ONE, BigInteger.ZERO);
        assertThatThrownBy(() -> zi.add(zi.one(), foreign))
            .isInstanceOf(IncompatibleAlgebraException.class);
    }
}
