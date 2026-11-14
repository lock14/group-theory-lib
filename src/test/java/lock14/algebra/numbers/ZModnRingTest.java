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
}
