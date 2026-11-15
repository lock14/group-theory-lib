package lock14.algebra.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.FieldLaws;
import lock14.algebra.numeric.NumericContext;
import lock14.algebra.structure.AbelianGroup;
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
        assertThat(r.signum()).isEqualTo(-1);
        assertThat(Rational.ZERO.signum()).isZero();
        assertThat(Rational.TEN.signum()).isEqualTo(1);
        assertThat(r.abs()).isEqualTo(Rational.of(3, 4));
        assertThat(Rational.of(3, 4).abs()).isEqualTo(Rational.of(3, 4));

        assertThat(Rational.of(BigInteger.valueOf(7))).isEqualTo(Rational.of(7));
        assertThat(Rational.field()).isSameAs(Rationals.FIELD);
        assertThat(Rationals.FIELD.toString()).isEqualTo("Q");
        assertThat(Rational.of(5).toString()).isEqualTo("5");
        assertThat(r.toString()).isEqualTo("-3/4");

        assertThat(r.tryReciprocal()).contains(Rational.of(-4, 3));
        assertThat(Rational.ZERO.tryReciprocal()).isEmpty();
        assertThat(Rationals.FIELD.tryReciprocal(Rational.TWO)).contains(Rational.of(1, 2));
        assertThat(Rationals.FIELD.tryReciprocal(Rational.ZERO)).isEmpty();
        assertThat(Rationals.FIELD.divideLeft(Rational.of(6), Rational.TWO)).isEqualTo(Rational.of(3));
        assertThatThrownBy(() -> Rational.of(5, 0))
            .isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(Rational.ZERO::reciprocal)
            .isInstanceOf(NonInvertibleElementException.class);

        AbelianGroup<Rational> units = Rationals.FIELD.asMultiplicativeGroupOfUnits();
        assertThat(units.identity()).isEqualTo(Rational.ONE);
        assertThatThrownBy(() -> units.combine(Rational.ZERO, Rational.ONE))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> units.combine(Rational.ONE, Rational.ZERO))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testComparisonAndScopedMathContext() {
        Rational oneThird = Rational.of(1, 3);
        assertThat(oneThird).isLessThan(Rational.of(1, 2));
        assertThat(oneThird).isEqualByComparingTo(Rational.of(2, 6));
        assertThat(Rational.ZERO.bigDecimalValue(MathContext.DECIMAL64)).isEqualTo(BigDecimal.ZERO);

        BigDecimal dec3 = ScopedValue.where(NumericContext.MATH_CONTEXT, new MathContext(4))
            .call(oneThird::bigDecimalValue);
        assertThat(dec3).isEqualTo(new BigDecimal("0.3333"));
    }

    @Test
    public void testDoubleValueExtremeBitLengths() {
        assertThat(Rational.ZERO.doubleValue()).isEqualTo(0.0);
        assertThat(Rational.of(1, 4).doubleValue()).isEqualTo(0.25);

        Rational largeNumSmallDen = Rational.of(BigInteger.ONE.shiftLeft(60), BigInteger.valueOf(3));
        assertThat(largeNumSmallDen.doubleValue()).isCloseTo(Math.scalb(1.0, 60) / 3.0, within(1e2));

        BigInteger num = BigInteger.TEN.pow(400).add(BigInteger.ONE);
        BigInteger den = BigInteger.TEN.pow(400).multiply(BigInteger.TWO).add(BigInteger.ONE);
        Rational r = Rational.of(num, den);
        assertThat(r.doubleValue()).isCloseTo(0.5, within(1e-15));
        assertThat(r.negate().doubleValue()).isCloseTo(-0.5, within(1e-15));

        Rational subnormal = Rational.of(BigInteger.ONE, BigInteger.ONE.shiftLeft(1050));
        assertThat(subnormal.doubleValue()).isEqualTo(Math.scalb(1.0, -1050));
    }
}
