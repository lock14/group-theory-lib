package lock14.algebra.linear;

import java.math.BigInteger;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.laws.ModuleLaws;
import lock14.algebra.laws.VectorSpaceLaws;
import lock14.algebra.numbers.BigIntRing;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.RationalField;
import lock14.algebra.structure.Module;
import lock14.algebra.structure.VectorSpace;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class VectorTest {

    public static class RationalVectorSpaceTest extends VectorSpaceLaws<Vector<Rational>, Rational> {

        private final CoordinateVectorSpace<Rational> space = new CoordinateVectorSpace<>(3, RationalField.INSTANCE);

        @Override
        protected VectorSpace<Vector<Rational>, Rational> vectorSpace() {
            return space;
        }

        @Provide
        Arbitrary<Rational> scalars() {
            return Arbitraries.integers().between(-20, 20).map(Rational::of);
        }

        @Provide
        Arbitrary<Rational> nonZeroScalars() {
            return Arbitraries.integers().between(-20, 20)
                    .filter(n -> n != 0)
                    .map(Rational::of);
        }

        @Provide
        Arbitrary<Vector<Rational>> vectors() {
            return scalars().list().ofSize(3)
                    .map(coords -> Vector.of(RationalField.INSTANCE, coords));
        }
    }

    public static class IntegerCoordinateModuleTest extends ModuleLaws<Vector<BigInteger>, BigInteger> {

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
    }

    @Test
    public void testVectorImmutabilityAndBasis() {
        CoordinateVectorSpace<Rational> r3 = new CoordinateVectorSpace<>(3, Rational.field());
        List<Vector<Rational>> basis = r3.standardBasis();
        assertThat(basis).hasSize(3);
        assertThat(basis.get(0)).isEqualTo(r3.of(Rational.ONE, Rational.ZERO, Rational.ZERO));
        assertThat(basis.get(1)).isEqualTo(r3.of(Rational.ZERO, Rational.ONE, Rational.ZERO));
        assertThat(basis.get(2)).isEqualTo(r3.of(Rational.ZERO, Rational.ZERO, Rational.ONE));

        Object[] raw = new Object[] { Rational.ONE, Rational.TWO };
        Vector<Rational> v = new Vector<>(2, raw, Rational.field());
        raw[0] = Rational.TEN;
        assertThat(v.get(0)).isEqualTo(Rational.ONE);

        v.data()[0] = Rational.TEN;
        assertThat(v.get(0)).isEqualTo(Rational.ONE);

        Vector<Rational> updated = v.with(1, Rational.TEN);
        assertThat(updated).isEqualTo(Vector.of(Rational.field(), Rational.ONE, Rational.TEN));
        assertThat(v.get(1)).isEqualTo(Rational.TWO);
    }

    @Test
    public void testDotSubtractAndMatrixVectorProducts() {
        RationalField f = RationalField.INSTANCE;
        Vector<Rational> v1 = Vector.of(f, Rational.of(1), Rational.of(2), Rational.of(3));
        Vector<Rational> v2 = Vector.of(f, Rational.of(4), Rational.of(5), Rational.of(6));

        assertThat(v2.subtract(v1)).isEqualTo(Vector.of(f, Rational.of(3), Rational.of(3), Rational.of(3)));
        assertThat(v1.dot(v2)).isEqualTo(Rational.of(32));

        // 2x3 matrix A * 3-vector v1 -> 2-vector
        DenseMatrix<Rational> a = DenseMatrix.of(2, 3, f,
                Rational.of(1), Rational.of(0), Rational.of(2),
                Rational.of(-1), Rational.of(3), Rational.of(1));
        assertThat(a.multiply(v1)).isEqualTo(Vector.of(f, Rational.of(7), Rational.of(8)));

        // Row vector u^T (dim 2) * A (2x3) -> 3-vector
        Vector<Rational> u = Vector.of(f, Rational.of(2), Rational.of(3));
        assertThat(u.multiply(a)).isEqualTo(Vector.of(f, Rational.of(-1), Rational.of(9), Rational.of(7)));

        assertThatThrownBy(() -> a.multiply(u))
                .isInstanceOf(DimensionMismatchException.class);
    }
}
