package lock14.algebra.linear;

import java.math.BigInteger;
import lock14.algebra.laws.ModuleLaws;
import lock14.algebra.numbers.Integers;
import lock14.algebra.structure.LeftModule;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CoordinateModuleTest extends ModuleLaws<Vector<BigInteger>, BigInteger> {

    private final CoordinateModule<BigInteger> module = new CoordinateModule<>(3, Integers.RING);

    @Override
    protected LeftModule<Vector<BigInteger>, BigInteger> module() {
        return module;
    }

    @Provide
    Arbitrary<BigInteger> scalars() {
        return Arbitraries.bigIntegers().between(BigInteger.valueOf(-50), BigInteger.valueOf(50));
    }

    @Provide
    Arbitrary<Vector<BigInteger>> vectors() {
        return scalars().list().ofSize(3)
            .map(coords -> Vector.of(Integers.RING, coords));
    }

    @Test
    public void testUnitBasisAndOf() {
        assertThat(module.unitBasis(1)).isEqualTo(
            module.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO)
        );
        assertThat(module.standardBasis()).containsExactly(
            module.of(BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO),
            module.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO),
            module.of(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE)
        );
        assertThatThrownBy(() -> new CoordinateModule<>(0, Integers.RING))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
