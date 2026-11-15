package lock14.algebra.linear;

import java.math.BigInteger;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.laws.ModuleLaws;
import lock14.algebra.numbers.Integers;
import lock14.algebra.quotient.QuotientRing;
import lock14.algebra.quotient.Residue;
import lock14.algebra.quotient.ZMod;
import lock14.algebra.structure.AbelianGroup;
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
        return Arbitraries.oneOf(
            Arbitraries.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE.negate(), BigInteger.TWO),
            Arbitraries.bigIntegers().between(BigInteger.valueOf(-50), BigInteger.valueOf(50))
        );
    }

    @Provide
    Arbitrary<Vector<BigInteger>> vectors() {
        return Arbitraries.oneOf(
            Arbitraries.of(
                module.zero(),
                module.unitBasis(0),
                module.unitBasis(1),
                module.unitBasis(2)
            ),
            scalars().list().ofSize(3).map(coords -> Vector.of(Integers.RING, coords))
        );
    }

    @Test
    public void testUnitBasisOfAndValidationGuards() {
        assertThat(module.dimension()).isEqualTo(3);
        assertThat(module.scalarRing()).isEqualTo(Integers.RING);
        assertThat(module.unitBasis(1)).isEqualTo(
            module.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO)
        );
        assertThat(module.standardBasis()).containsExactly(
            module.of(BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO),
            module.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO),
            module.of(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE)
        );

        Vector<BigInteger> u = module.of(BigInteger.valueOf(5), BigInteger.valueOf(-2), BigInteger.ONE);
        Vector<BigInteger> v = module.of(BigInteger.TWO, BigInteger.ONE, BigInteger.valueOf(4));
        assertThat(module.subtract(u, v)).isEqualTo(
            module.of(BigInteger.valueOf(3), BigInteger.valueOf(-3), BigInteger.valueOf(-3))
        );

        AbelianGroup<Vector<BigInteger>> grp = module.moduleGroup();
        assertThat(grp.identity()).isEqualTo(module.zero());
        assertThat(grp.combine(u, v)).isEqualTo(u.add(v));
        assertThat(grp.inverse(u)).isEqualTo(u.negate());

        assertThatThrownBy(() -> new CoordinateModule<>(0, Integers.RING))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CoordinateModule<>(-3, Integers.RING))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CoordinateModule<>(3, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> module.of((BigInteger[]) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> module.of(BigInteger.ONE, BigInteger.TWO))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> module.of(BigInteger.ONE, BigInteger.TWO, BigInteger.ZERO, BigInteger.ONE))
            .isInstanceOf(DimensionMismatchException.class);

        assertThatThrownBy(() -> module.unitBasis(-1))
            .isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> module.unitBasis(3))
            .isInstanceOf(IndexOutOfBoundsException.class);

        assertThatThrownBy(() -> module.scale(null, u)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> module.scale(BigInteger.TWO, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> module.add(null, u)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> module.add(u, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> module.negate(null)).isInstanceOf(NullPointerException.class);

        Vector<BigInteger> wrongDim = Vector.of(Integers.RING, BigInteger.ONE, BigInteger.TWO);
        assertThatThrownBy(() -> module.scale(BigInteger.TWO, wrongDim))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> module.add(wrongDim, u))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> module.add(u, wrongDim))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> module.negate(wrongDim))
            .isInstanceOf(DimensionMismatchException.class);

        QuotientRing<BigInteger> z5 = ZMod.ring(5);
        QuotientRing<BigInteger> z7 = ZMod.ring(7);
        CoordinateModule<Residue<BigInteger>> mod5 = new CoordinateModule<>(2, z5);
        Vector<Residue<BigInteger>> vec7 = Vector.zeros(2, z7);
        assertThatThrownBy(() -> mod5.scale(ZMod.element(z5, 2), vec7))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> mod5.add(vec7, mod5.zero()))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> mod5.negate(vec7))
            .isInstanceOf(IncompatibleAlgebraException.class);
    }
}
