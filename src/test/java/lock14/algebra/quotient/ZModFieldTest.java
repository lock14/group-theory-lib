package lock14.algebra.quotient;

import java.math.BigInteger;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.FieldLaws;
import lock14.algebra.numbers.Integers;
import lock14.algebra.structure.Field;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ZModFieldTest extends FieldLaws<Residue<BigInteger>> {

    private final QuotientField<BigInteger> f17 = ZMod.field(17);

    @Override
    protected Field<Residue<BigInteger>> field() {
        return f17;
    }

    @Provide
    Arbitrary<Residue<BigInteger>> elements() {
        return Arbitraries.longs().between(0, 16).map(v -> ZMod.element(f17, v));
    }

    @Provide
    Arbitrary<Residue<BigInteger>> nonZeroElements() {
        return Arbitraries.longs().between(1, 16).map(v -> ZMod.element(f17, v));
    }

    @Test
    public void testCompositeModulusRejectedAndFermatLittleTheorem() {
        assertThatThrownBy(() -> ZMod.field(0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ZMod.field(-7))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ZMod.field(15))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new QuotientField<>(Integers.RING, BigInteger.ONE))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new QuotientField<>(Integers.RING, BigInteger.ONE.negate()))
            .isInstanceOf(IllegalArgumentException.class);

        Residue<BigInteger> three = ZMod.element(f17, 3);
        assertThat(f17.power(three, 16)).isEqualTo(f17.one());
        assertThatThrownBy(() -> f17.reciprocal(f17.zero()))
            .isInstanceOf(NonInvertibleElementException.class);

        QuotientField<BigInteger> uncheckedComposite =
            new QuotientField<>(Integers.RING, BigInteger.valueOf(6));
        assertThatThrownBy(() -> uncheckedComposite.reciprocal(ZMod.element(uncheckedComposite, 2)))
            .isInstanceOf(NonInvertibleElementException.class);
    }
}
