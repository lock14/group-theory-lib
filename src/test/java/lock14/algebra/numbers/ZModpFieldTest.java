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

public class ZModpFieldTest extends FieldLaws<ModuloInteger> {

    private final ZModpField field = new ZModpField(17);

    @Override
    protected Field<ModuloInteger> field() {
        return field;
    }

    @Provide
    Arbitrary<ModuloInteger> elements() {
        return Arbitraries.longs().between(0, 16)
                .map(field::element);
    }

    @Provide
    Arbitrary<ModuloInteger> nonZeroElements() {
        return Arbitraries.longs().between(1, 16)
                .map(field::element);
    }

    @Test
    public void testPrimeValidationAndElementFactory() {
        assertThat(field.element(BigInteger.valueOf(20))).isEqualTo(ModuloInteger.of(3, 17));
        assertThatThrownBy(() -> new ZModpField(12))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ZModpField(1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ZModpField(-7))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testForeignModulusOperandsRejected() {
        ZModpField f5 = new ZModpField(5);
        ModuloInteger sevenA = ModuloInteger.of(3, 7);
        ModuloInteger sevenB = ModuloInteger.of(4, 7);

        assertThatThrownBy(() -> f5.add(sevenA, sevenB))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> f5.reciprocal(sevenA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> f5.divide(sevenA, sevenB))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> f5.isOne(sevenA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> f5.normalize(sevenA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> f5.gcd(sevenA, f5.zero()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> f5.extendedGcd(sevenA, f5.zero()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
