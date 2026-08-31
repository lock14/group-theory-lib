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
    public void testTransposeAndTrace() {
        Rational[][] arr = {
            {Rational.of(1), Rational.of(2), Rational.of(3)},
            {Rational.of(4), Rational.of(5), Rational.of(6)},
            {Rational.of(7), Rational.of(8), Rational.of(9)}
        };
        DenseMatrix<Rational> a = DenseMatrix.of(field, arr);

        // Trace of a: 1 + 5 + 9 = 15
        assertThat(a.trace()).isEqualTo(Rational.of(15));

        // (A^T)^T = A
        assertThat(a.transpose().transpose()).isEqualTo(a);

        // Trace linearity: tr(2A) = 2 tr(A)
        assertThat(a.scale(Rational.of(2)).trace()).isEqualTo(Rational.of(30));
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
