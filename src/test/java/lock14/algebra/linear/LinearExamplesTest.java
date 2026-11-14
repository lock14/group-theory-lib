package lock14.algebra.linear;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import org.junit.jupiter.api.Test;

class LinearExamplesTest {

    @Test
    void luSolveAndNullspaceKernelExample() {
        // @start region="lu-solve-and-kernel"
        SquareMatrix<Rational> a = SquareMatrix.of(2, Rationals.FIELD,
                Rational.of(1), Rational.of(2),
                Rational.of(3), Rational.of(4));
        Vector<Rational> b = Vector.of(Rationals.FIELD, Rational.of(5), Rational.of(11));
        Vector<Rational> x = a.solve(b);

        Matrix<Rational> rect = Matrix.of(2, 4, Rationals.FIELD,
                Rational.of(1), Rational.of(2), Rational.ZERO, Rational.of(3),
                Rational.ZERO, Rational.ZERO, Rational.ONE, Rational.of(-1));
        List<Vector<Rational>> nullspaceBasis = rect.kernel();
        // @end

        assertThat(x).isEqualTo(Vector.of(Rationals.FIELD, Rational.ONE, Rational.TWO));
        assertThat(nullspaceBasis).hasSize(2);
        for (Vector<Rational> basisVec : nullspaceBasis) {
            assertThat(rect.multiply(basisVec)).isEqualTo(Vector.zeros(2, Rationals.FIELD));
        }
    }
}
