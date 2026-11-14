package lock14.algebra.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.FieldLaws;
import lock14.algebra.numeric.NumericContext;
import lock14.algebra.structure.Field;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class RationalTest extends FieldLaws<Rational> {

    @Override
    protected Field<Rational> field() {
        return Rationals.FIELD;
    }

    @Provide
    Arbitrary<Rational> elements() {
        return Arbitraries.bigIntegers()
            .between(BigInteger.valueOf(-10_000), BigInteger.valueOf(10_000))
            .flatMap(num ->
                Arbitraries.bigIntegers()
                    .between(BigInteger.valueOf(-10_000), BigInteger.valueOf(10_000))
                    .filter(den -> den.signum() != 0)
                    .map(den -> Rational.of(num, den))
            );
    }

    @Provide
    Arbitrary<Rational> nonZeroElements() {
        return elements().filter(r -> !r.isZero());
    }

    @Test
    public void testCanonicalReductionAndTryReciprocal() {
        Rational r = Rational.of(6, -8);
        assertThat(r.numerator()).isEqualTo(BigInteger.valueOf(-3));
        assertThat(r.denominator()).isEqualTo(BigInteger.valueOf(4));
        assertThat(r.tryReciprocal()).contains(Rational.of(-4, 3));
        assertThat(Rational.ZERO.tryReciprocal()).isEmpty();
        assertThatThrownBy(() -> Rational.of(5, 0))
            .isInstanceOf(NonInvertibleElementException.class);
    }

    @Test
    public void testComparisonAndScopedMathContext() {
        Rational oneThird = Rational.of(1, 3);
        assertThat(oneThird).isLessThan(Rational.of(1, 2));
        assertThat(oneThird).isEqualByComparingTo(Rational.of(2, 6));

        BigDecimal dec3 = ScopedValue.where(NumericContext.MATH_CONTEXT, new MathContext(4))
            .call(oneThird::bigDecimalValue);
        assertThat(dec3).isEqualTo(new BigDecimal("0.3333"));
    }

    @Test
    public void testDoubleValueExtremeBitLengths() {
        BigInteger num = BigInteger.TEN.pow(400).add(BigInteger.ONE);
        BigInteger den = BigInteger.TEN.pow(400).multiply(BigInteger.TWO).add(BigInteger.ONE);
        Rational r = Rational.of(num, den);
        assertThat(r.doubleValue()).isCloseTo(0.5, within(1e-15));
        assertThat(r.negate().doubleValue()).isCloseTo(-0.5, within(1e-15));

        Rational subnormal = Rational.of(BigInteger.ONE, BigInteger.ONE.shiftLeft(1050));
        assertThat(subnormal.doubleValue()).isEqualTo(Math.scalb(1.0, -1050));
    }
}
