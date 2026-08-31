package lock14.algebra.numbers;

import java.math.BigInteger;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.laws.FieldLaws;
import lock14.algebra.structure.Field;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RationalTest extends FieldLaws<Rational> {

    @Override
    protected Field<Rational> field() {
        return RationalField.INSTANCE;
    }

    @Provide
    Arbitrary<Rational> elements() {
        return Arbitraries.bigIntegers()
                .between(BigInteger.valueOf(-10000), BigInteger.valueOf(10000))
                .flatMap(num ->
                    Arbitraries.bigIntegers()
                        .between(BigInteger.valueOf(-10000), BigInteger.valueOf(10000))
                        .filter(den -> !den.equals(BigInteger.ZERO))
                        .map(den -> Rational.of(num, den))
                );
    }

    @Provide
    Arbitrary<Rational> nonZeroElements() {
        return elements().filter(r -> !r.isZero());
    }

    @Test
    public void testCanonicalReduction() {
        Rational r = Rational.of(6, 8);
        assertThat(r.numerator()).isEqualTo(BigInteger.valueOf(3));
        assertThat(r.denominator()).isEqualTo(BigInteger.valueOf(4));
    }

    @Test
    public void testNegativeDenominatorNormalization() {
        Rational r = Rational.of(3, -4);
        assertThat(r.numerator()).isEqualTo(BigInteger.valueOf(-3));
        assertThat(r.denominator()).isEqualTo(BigInteger.valueOf(4));

        Rational r2 = Rational.of(-3, -4);
        assertThat(r2.numerator()).isEqualTo(BigInteger.valueOf(3));
        assertThat(r2.denominator()).isEqualTo(BigInteger.valueOf(4));
    }

    @Test
    public void testZeroDenominatorThrows() {
        assertThatThrownBy(() -> Rational.of(5, 0))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    public void testComparison() {
        Rational r1 = Rational.of(1, 3);
        Rational r2 = Rational.of(1, 2);
        assertThat(r1).isLessThan(r2);
        assertThat(r2).isGreaterThan(r1);
        assertThat(r1).isEqualByComparingTo(Rational.of(2, 6));
    }
}
