package lock14.algebra.linear;

import java.math.BigInteger;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.laws.ModuleLaws;
import lock14.algebra.numbers.BigIntRing;
import lock14.algebra.structure.Module;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CoordinateModuleTest extends ModuleLaws<Vector<BigInteger>, BigInteger> {

    private final CoordinateModule<BigInteger> module = new CoordinateModule<>(3, BigIntRing.INSTANCE);

    @Override
    protected Module<Vector<BigInteger>, BigInteger> module() {
        return module;
    }

    @Provide
    Arbitrary<BigInteger> scalars() {
        return Arbitraries.bigIntegers().between(BigInteger.valueOf(-50), BigInteger.valueOf(50));
    }

    @Provide
    Arbitrary<Vector<BigInteger>> vectors() {
        return scalars().list().ofSize(3)
                .map(coords -> Vector.of(BigIntRing.INSTANCE, coords));
    }

    @Test
    public void testUnitBasisAndOf() {
        assertThat(module.unitBasis(1)).isEqualTo(
                module.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO)
        );
        assertThatThrownBy(() -> new CoordinateModule<>(0, BigIntRing.INSTANCE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
