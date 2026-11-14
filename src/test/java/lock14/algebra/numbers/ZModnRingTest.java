package lock14.algebra.numbers;

import java.math.BigInteger;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.laws.CommutativeRingLaws;
import lock14.algebra.structure.CommutativeRing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ZModnRingTest extends CommutativeRingLaws<ModuloInteger> {

    private final ZModnRing ring = new ZModnRing(12);

    @Override
    protected CommutativeRing<ModuloInteger> commutativeRing() {
        return ring;
    }

    @Provide
    Arbitrary<ModuloInteger> elements() {
        return Arbitraries.longs().between(0, 11)
                .map(ring::element);
    }

    @Test
    public void testFactoryAndInvalidModulus() {
        assertThat(ring.element(BigInteger.valueOf(15))).isEqualTo(ModuloInteger.of(3, 12));
        assertThatThrownBy(() -> new ZModnRing(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ZModnRing(-5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testTrivialZeroRingModulusOne() {
        ZModnRing trivial = new ZModnRing(1);
        assertThat(trivial.zero()).isEqualTo(trivial.one());
        assertThat(trivial.isZero(trivial.zero())).isTrue();
        assertThat(trivial.isOne(trivial.one())).isTrue();
        assertThat(trivial.one().isInvertible()).isTrue();
        assertThat(trivial.one().reciprocal()).isEqualTo(trivial.one());
    }

    @Test
    public void testForeignModulusOperandsRejected() {
        ZModnRing mod5 = new ZModnRing(5);
        ModuloInteger sevenA = ModuloInteger.of(3, 7);
        ModuloInteger sevenB = ModuloInteger.of(4, 7);

        assertThatThrownBy(() -> mod5.add(sevenA, sevenB))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mod5.negate(sevenA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mod5.isZero(sevenA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mod5.isOne(sevenA))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
