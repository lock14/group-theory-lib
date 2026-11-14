package lock14.algebra.linear;

import java.util.List;
import org.junit.jupiter.api.Test;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.RationalField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DenseMatrixTest {

    private final RationalField field = RationalField.INSTANCE;

    @Test
    public void testMatrixMultiplication() {
        // [3 -2 5]      [ 2  3]     [24 29]
        // [3  0 4]  *   [-9  0]  =  [ 6 25]
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
        Rational[][] expected = {
            {Rational.of(24), Rational.of(29)},
            {Rational.of(6), Rational.of(25)}
        };

        DenseMatrix<Rational> a = DenseMatrix.of(field, m1);
        DenseMatrix<Rational> b = DenseMatrix.of(field, m2);
        DenseMatrix<Rational> product = a.multiply(b);

        assertThat(product).isEqualTo(DenseMatrix.of(field, expected));
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
        Rational[][] arr = {
            {Rational.of(1), Rational.of(2), Rational.of(3)},
            {Rational.of(4), Rational.of(5), Rational.of(6)},
            {Rational.of(7), Rational.of(8), Rational.of(9)}
        };
        DenseMatrix<Rational> a = DenseMatrix.of(field, arr);
        SquareMatrix<Rational> sq = a.asSquareMatrix();

        // Trace of sq: 1 + 5 + 9 = 15
        assertThat(sq.trace()).isEqualTo(Rational.of(15));

        // (A^T)^T = A
        assertThat(a.transpose().transpose()).isEqualTo(a);

        // Trace linearity: tr(2A) = 2 tr(A)
        assertThat(sq.scale(Rational.of(2)).trace()).isEqualTo(Rational.of(30));
    }

    @Test
    public void testDefensiveArrayCloningInConstructorsAndAccessors() {
        Object[] input = new Object[] {
            Rational.ONE, Rational.ZERO,
            Rational.ZERO, Rational.ONE
        };
        DenseMatrix<Rational> dm = new DenseMatrix<>(2, 2, field, input);
        SquareMatrix<Rational> sm = new SquareMatrix<>(2, field, input);

        // External mutation of input array must not leak into matrices
        input[0] = Rational.of(99);
        assertThat(dm.get(0, 0)).isEqualTo(Rational.ONE);
        assertThat(sm.get(0, 0)).isEqualTo(Rational.ONE);

        // Mutating array returned by record data() accessor must not leak either
        dm.data()[0] = Rational.of(77);
        sm.data()[0] = Rational.of(88);
        assertThat(dm.get(0, 0)).isEqualTo(Rational.ONE);
        assertThat(sm.get(0, 0)).isEqualTo(Rational.ONE);
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
