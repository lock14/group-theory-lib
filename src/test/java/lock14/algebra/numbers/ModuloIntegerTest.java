package lock14.algebra.numbers;

import java.math.BigInteger;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.context.AlgebraicContext;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.CommutativeRingLaws;
import lock14.algebra.laws.FieldLaws;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.Field;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ModuloIntegerTest {

    public static class ModuloRingTest extends CommutativeRingLaws<ModuloInteger> {
        private final ZModnRing ring = new ZModnRing(12);

        @Override
        protected CommutativeRing<ModuloInteger> commutativeRing() {
            return ring;
        }

        @Provide
        Arbitrary<ModuloInteger> elements() {
            return Arbitraries.longs().between(0, 11)
                    .map(val -> ModuloInteger.of(val, 12));
        }
    }

    public static class ModuloPrimeFieldTest extends FieldLaws<ModuloInteger> {
        private final ZModpField field = new ZModpField(17);

        @Override
        protected Field<ModuloInteger> field() {
            return field;
        }

        @Provide
        Arbitrary<ModuloInteger> elements() {
            return Arbitraries.longs().between(0, 16)
                    .map(val -> ModuloInteger.of(val, 17));
        }

        @Provide
        Arbitrary<ModuloInteger> nonZeroElements() {
            return Arbitraries.longs().between(1, 16)
                    .map(val -> ModuloInteger.of(val, 17));
        }
    }

    @Test
    public void testScopedValueAmbientModulus() {
        ScopedValue.where(AlgebraicContext.MODULUS, BigInteger.valueOf(17)).run(() -> {
            ModuloInteger a = ModuloInteger.of(12);
            ModuloInteger b = ModuloInteger.of(10);
            ModuloInteger sum = a.add(b); // (12 + 10) % 17 = 5
            assertThat(sum.value()).isEqualTo(BigInteger.valueOf(5));
        });
    }

    @Test
    public void testNonCoprimeInversionThrows() {
        // In Z/12Z, 4 is not coprime with 12
        ModuloInteger four = ModuloInteger.of(4, 12);
        assertThat(four.isInvertible()).isFalse();
        assertThatThrownBy(four::reciprocal)
                .isInstanceOf(NonInvertibleElementException.class);
    }

    @Test
    public void testNegativeValueModularNormalization() {
        ModuloInteger m = ModuloInteger.of(-3, 7);
        assertThat(m.value()).isEqualTo(BigInteger.valueOf(4));
    }
}
