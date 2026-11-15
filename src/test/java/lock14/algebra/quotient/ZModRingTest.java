package lock14.algebra.quotient;

import java.math.BigInteger;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.CommutativeRingLaws;
import lock14.algebra.numbers.GaussianIntegers;
import lock14.algebra.numbers.Integers;
import lock14.algebra.structure.CommutativeRing;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ZModRingTest extends CommutativeRingLaws<Residue<BigInteger>> {

    private final QuotientRing<BigInteger> z12 = ZMod.ring(12);

    @Override
    protected CommutativeRing<Residue<BigInteger>> commutativeRing() {
        return z12;
    }

    @Provide
    Arbitrary<Residue<BigInteger>> elements() {
        return Arbitraries.longs().between(-50, 50).map(v -> ZMod.element(z12, v));
    }

    @Test
    public void testUnitsZeroDivisorsZeroRingAndIncompatibleModuli() {
        assertThatThrownBy(() -> ZMod.ring(0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ZMod.ring(-5))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new QuotientRing<>(Integers.RING, BigInteger.ZERO))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(z12.domain()).isEqualTo(Integers.RING);
        assertThat(z12.modulus()).isEqualTo(BigInteger.valueOf(12));
        assertThat(z12.projection().apply(BigInteger.valueOf(17))).isEqualTo(ZMod.element(z12, 5));
        assertThat(z12.tryReciprocal(z12.zero())).isEmpty();

        Residue<BigInteger> five = ZMod.element(z12, 5);
        assertThat(five.isUnit()).isTrue();
        assertThat(five.tryReciprocal()).contains(five);
        assertThat(five.reciprocal()).isEqualTo(five);
        assertThat(ZMod.element(z12, 7).divide(five)).isEqualTo(ZMod.element(z12, 11));
        assertThat(five.toString()).isEqualTo("5 (mod 12)");

        // 4 is a zero divisor in Z/12Z (4 * 3 = 0)
        Residue<BigInteger> four = ZMod.element(z12, 4);
        assertThat(four.isUnit()).isFalse();
        assertThat(four.tryReciprocal()).isEmpty();
        assertThatThrownBy(four::reciprocal).isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> five.divide(four)).isInstanceOf(NonInvertibleElementException.class);

        // Zero ring Z/1Z: 0 == 1 and 0 is invertible!
        QuotientRing<BigInteger> z1 = ZMod.ring(1);
        assertThat(z1.zero()).isEqualTo(z1.one());
        assertThat(z1.tryReciprocal(z1.zero())).contains(z1.zero());

        // Cross-modulus mixing throws IncompatibleAlgebraException
        QuotientRing<BigInteger> z7 = ZMod.ring(7);
        assertThatThrownBy(() -> five.add(ZMod.element(z7, 3)))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> five.divide(ZMod.element(z7, 3)))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> z12.add(five, ZMod.element(z7, 3)))
            .isInstanceOf(IncompatibleAlgebraException.class);

        QuotientRing<BigInteger> z12Copy = ZMod.ring(12);
        var gaussianQuotient = new QuotientRing<>(GaussianIntegers.DOMAIN, GaussianIntegers.DOMAIN.of(2, 0));
        assertThat(z12.equals(z12)).isTrue();
        assertThat(z12).isEqualTo(z12Copy);
        assertThat(z12).hasSameHashCodeAs(z12Copy);
        assertThat(z12).isNotEqualTo(z7);
        assertThat(z12.equals(gaussianQuotient)).isFalse();
        assertThat(z12.equals("Z/(12)")).isFalse();
        assertThat(z12.equals(null)).isFalse();
        assertThat(z12.toString()).isEqualTo("Z/(12)");
    }
}
