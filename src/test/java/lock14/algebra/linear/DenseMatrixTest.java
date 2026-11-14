package lock14.algebra.linear;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Test;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.numbers.BigIntRing;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.RationalField;
import lock14.algebra.structure.Ring;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DenseMatrixTest {

    private final RationalField field = RationalField.INSTANCE;

    @Test
    public void testMatrixMultiplicationPromotesSquareAndPreservesRectangular() {
        // [3 -2 5]      [ 2  3]     [24 29]
        // [3  0 4]  *   [-9  0]  =  [ 6 25]  (2x3 * 3x2 -> canonical 2x2 SquareMatrix)
        //               [ 0  4]
        Rational[][] m1 = {
            {Rational.of(3), Rational.of(-2), Rational.of(5)},
            {Rational.of(3), Rational.of(0), Rational.of(4)}
        };
        Rational[][] m2 = {
            {Rational.of(2), Rational.of(3)},
            {Rational.of(-9), Rational.of(0)},
            {Rational.of(0), Rational.of(4)}
        };
        Rational[][] expectedSquare = {
            {Rational.of(24), Rational.of(29)},
            {Rational.of(6), Rational.of(25)}
        };

        DenseMatrix<Rational> a = DenseMatrix.of(field, m1);
        DenseMatrix<Rational> b = DenseMatrix.of(field, m2);
        Matrix<Rational> product = a.multiply(b);

        assertThat(product).isInstanceOf(SquareMatrix.class);
        assertThat(product).isEqualTo(SquareMatrix.of(field, expectedSquare));

        // 2x3 * 3x4 -> rectangular 2x4 DenseMatrix
        DenseMatrix<Rational> c = DenseMatrix.zeros(3, 4, field);
        Matrix<Rational> rectProduct = a.multiply(c);
        assertThat(rectProduct).isInstanceOf(DenseMatrix.class);
        assertThat(rectProduct).isEqualTo(DenseMatrix.zeros(2, 4, field));
    }

    @Test
    public void testSquareShapeRejectedByDenseMatrix() {
        assertThatThrownBy(() -> DenseMatrix.zeros(2, 2, field))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DenseMatrix.of(2, 2, field,
                Rational.ONE, Rational.ZERO, Rational.ZERO, Rational.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DenseMatrix.zeros(0, 3, field))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDimensionMismatchThrows() {
        DenseMatrix<Rational> a = DenseMatrix.zeros(2, 3, field);
        DenseMatrix<Rational> b = DenseMatrix.zeros(2, 3, field);
        DenseMatrix<Rational> c = DenseMatrix.zeros(4, 2, field);

        // a * b (2x3 * 2x3 -> inner dimension mismatch 3 != 2)
        assertThatThrownBy(() -> a.multiply(b))
                .isInstanceOf(DimensionMismatchException.class);

        // a + c (2x3 + 4x2 -> dimension mismatch)
        assertThatThrownBy(() -> a.add(c))
                .isInstanceOf(DimensionMismatchException.class);
    }

    @Test
    public void testTransposeAndSquareTrace() {
        Rational[][] sqArr = {
            {Rational.of(1), Rational.of(2), Rational.of(3)},
            {Rational.of(4), Rational.of(5), Rational.of(6)},
            {Rational.of(7), Rational.of(8), Rational.of(9)}
        };
        Matrix<Rational> polySq = Matrix.of(field, sqArr);
        SquareMatrix<Rational> sq = polySq.asSquareMatrix();

        // Trace of sq: 1 + 5 + 9 = 15
        assertThat(sq.trace()).isEqualTo(Rational.of(15));
        assertThat(sq.scale(Rational.of(2)).trace()).isEqualTo(Rational.of(30));

        // Rectangular (A^T)^T = A
        DenseMatrix<Rational> rect = DenseMatrix.of(2, 3, field,
                Rational.of(1), Rational.of(2), Rational.of(3),
                Rational.of(4), Rational.of(5), Rational.of(6));
        assertThat(rect.transpose().transpose()).isEqualTo(rect);
    }

    @Test
    public void testDefensiveArrayCloningInConstructorsAndAccessors() {
        Object[] rectInput = new Object[] {
            Rational.ONE, Rational.ZERO, Rational.TWO,
            Rational.ZERO, Rational.ONE, Rational.TEN
        };
        Object[] sqInput = new Object[] {
            Rational.ONE, Rational.ZERO,
            Rational.ZERO, Rational.ONE
        };
        DenseMatrix<Rational> dm = new DenseMatrix<>(2, 3, field, rectInput);
        SquareMatrix<Rational> sm = new SquareMatrix<>(2, field, sqInput);

        // External mutation of input array must not leak into matrices
        rectInput[0] = Rational.of(99);
        sqInput[0] = Rational.of(99);
        assertThat(dm.get(0, 0)).isEqualTo(Rational.ONE);
        assertThat(sm.get(0, 0)).isEqualTo(Rational.ONE);

        // Mutating array returned by record data() accessor must not leak either
        dm.data()[0] = Rational.of(77);
        sm.data()[0] = Rational.of(88);
        assertThat(dm.get(0, 0)).isEqualTo(Rational.ONE);
        assertThat(sm.get(0, 0)).isEqualTo(Rational.ONE);
    }

    @Test
    public void testRingAwareEquality() {
        Ring<BigInteger> altBigIntRing = new Ring<>() {
            @Override public BigInteger zero() { return BigIntRing.INSTANCE.zero(); }
            @Override public BigInteger one() { return BigIntRing.INSTANCE.one(); }
            @Override public BigInteger add(BigInteger a, BigInteger b) { return a.add(b); }
            @Override public BigInteger negate(BigInteger a) { return a.negate(); }
            @Override public BigInteger multiply(BigInteger a, BigInteger b) { return a.multiply(b); }
        };

        DenseMatrix<BigInteger> dm1 = DenseMatrix.zeros(2, 3, BigIntRing.INSTANCE);
        DenseMatrix<BigInteger> dm2 = DenseMatrix.zeros(2, 3, altBigIntRing);
        assertThat(dm1).isNotEqualTo(dm2);

        SquareMatrix<BigInteger> sm1 = SquareMatrix.identity(2, BigIntRing.INSTANCE);
        SquareMatrix<BigInteger> sm2 = SquareMatrix.identity(2, altBigIntRing);
        assertThat(sm1).isNotEqualTo(sm2);
    }

    @Test
    public void testStreamRowsGatherer() {
        Rational[][] arr = {
            {Rational.of(1), Rational.of(2)},
            {Rational.of(3), Rational.of(4)},
            {Rational.of(5), Rational.of(6)}
        };
        DenseMatrix<Rational> a = DenseMatrix.of(field, arr);
        List<List<Rational>> rows = a.streamRows().toList();

        assertThat(rows).hasSize(3);
        assertThat(rows.get(0)).containsExactly(Rational.of(1), Rational.of(2));
        assertThat(rows.get(1)).containsExactly(Rational.of(3), Rational.of(4));
        assertThat(rows.get(2)).containsExactly(Rational.of(5), Rational.of(6));
    }
}
