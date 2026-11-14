package lock14.algebra.quotient;

import java.math.BigInteger;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.CommutativeRingLaws;
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
        Residue<BigInteger> five = ZMod.element(z12, 5);
        assertThat(five.tryReciprocal()).contains(five);
        assertThat(five.reciprocal()).isEqualTo(five);

        // 4 is a zero divisor in Z/12Z (4 * 3 = 0)
        Residue<BigInteger> four = ZMod.element(z12, 4);
        assertThat(four.tryReciprocal()).isEmpty();
        assertThatThrownBy(four::reciprocal).isInstanceOf(NonInvertibleElementException.class);

        // Zero ring Z/1Z: 0 == 1 and 0 is invertible!
        QuotientRing<BigInteger> z1 = ZMod.ring(1);
        assertThat(z1.zero()).isEqualTo(z1.one());
        assertThat(z1.tryReciprocal(z1.zero())).contains(z1.zero());

        // Cross-modulus mixing throws IncompatibleAlgebraException
        QuotientRing<BigInteger> z7 = ZMod.ring(7);
        assertThatThrownBy(() -> five.add(ZMod.element(z7, 3)))
            .isInstanceOf(IncompatibleAlgebraException.class);
    }
}
