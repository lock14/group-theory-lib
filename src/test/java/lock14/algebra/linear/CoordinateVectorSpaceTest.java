package lock14.algebra.linear;

import java.math.BigInteger;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.laws.VectorSpaceLaws;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import lock14.algebra.quotient.QuotientField;
import lock14.algebra.quotient.Residue;
import lock14.algebra.quotient.ZMod;
import lock14.algebra.structure.VectorSpace;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CoordinateVectorSpaceTest extends VectorSpaceLaws<Vector<Rational>, Rational> {

    private final CoordinateVectorSpace<Rational> space = new CoordinateVectorSpace<>(3, Rationals.FIELD);

    @Override
    protected VectorSpace<Vector<Rational>, Rational> vectorSpace() {
        return space;
    }

    @Provide
    Arbitrary<Rational> scalars() {
        return Arbitraries.oneOf(
            Arbitraries.of(Rational.ZERO, Rational.ONE, Rational.of(-1), Rational.of(3, 5)),
            Arbitraries.integers().between(-20, 20).map(Rational::of)
        );
    }

    @Provide
    Arbitrary<Rational> nonZeroScalars() {
        return Arbitraries.oneOf(
            Arbitraries.of(Rational.ONE, Rational.of(-1), Rational.TWO, Rational.of(-3, 7)),
            Arbitraries.integers().between(-20, 20).filter(n -> n != 0).map(Rational::of)
        );
    }

    @Provide
    Arbitrary<Vector<Rational>> vectors() {
        return Arbitraries.oneOf(
            Arbitraries.of(space.zero(), space.unitBasis(0), space.unitBasis(1), space.unitBasis(2)),
            scalars().list().ofSize(3).map(coords -> Vector.of(Rationals.FIELD, coords))
        );
    }

    @Test
    public void testStandardBasisModuleDelegationAndGuards() {
        assertThat(space.dimension()).isEqualTo(3);
        assertThat(space.scalarField()).isEqualTo(Rationals.FIELD);
        assertThat(space.standardBasis()).hasSize(3);
        assertThat(space.standardBasis()).isEqualTo(space.asModule().standardBasis());
        assertThat(space.unitBasis(2)).isEqualTo(space.of(Rational.ZERO, Rational.ZERO, Rational.ONE));

        Vector<Rational> u = space.of(Rational.of(4), Rational.TWO, Rational.of(-1));
        Vector<Rational> v = space.of(Rational.ONE, Rational.of(5), Rational.TWO);
        assertThat(space.subtract(u, v)).isEqualTo(space.of(Rational.of(3), Rational.of(-3), Rational.of(-3)));

        assertThatThrownBy(() -> new CoordinateVectorSpace<>(0, Rationals.FIELD))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CoordinateVectorSpace<>(-2, Rationals.FIELD))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CoordinateVectorSpace<>(3, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> space.of(Rational.ONE, Rational.TWO))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> space.unitBasis(-1))
            .isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> space.unitBasis(3))
            .isInstanceOf(IndexOutOfBoundsException.class);

        QuotientField<BigInteger> f5 = ZMod.field(5);
        QuotientField<BigInteger> f7 = ZMod.field(7);
        CoordinateVectorSpace<Residue<BigInteger>> vs5 = new CoordinateVectorSpace<>(2, f5);
        Vector<Residue<BigInteger>> vec7 = Vector.zeros(2, f7);
        assertThatThrownBy(() -> vs5.scale(ZMod.element(f5, 3), vec7))
            .isInstanceOf(IncompatibleAlgebraException.class);
    }
}
