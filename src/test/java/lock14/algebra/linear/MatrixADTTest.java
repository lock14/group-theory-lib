package lock14.algebra.linear;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import lock14.algebra.numbers.Rational;
import net.jqwik.api.Example;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatrixADTTest {

    @Test
    @DisplayName("Exhaustive Pattern Matching on Matrix ADT")
    void testExhaustivePatternMatching() {
        Matrix<Rational> square = SquareMatrix.identity(3, Rational.field());
        Matrix<Rational> dense = DenseMatrix.zeros(2, 4, Rational.field());

        assertThat(classifyMatrix(square)).isEqualTo("Square 3x3");
        assertThat(classifyMatrix(dense)).isEqualTo("Dense 2x4");
    }

    private String classifyMatrix(Matrix<?> matrix) {
        return switch (matrix) {
            case SquareMatrix<?> sm -> "Square " + sm.dimension() + "x" + sm.dimension();
            case DenseMatrix<?> dm -> "Dense " + dm.rows() + "x" + dm.cols();
        };
    }

    @Test
    @DisplayName("Record Deconstruction on Matrix ADT")
    void testRecordDeconstruction() {
        Matrix<Rational> square = SquareMatrix.identity(2, Rational.field());

        if (square instanceof SquareMatrix<Rational>(int dim, var ring, var data)) {
            assertThat(dim).isEqualTo(2);
            assertThat(ring).isEqualTo(Rational.field());
            assertThat(data).hasSize(4);
        } else {
            throw new AssertionError("Failed to match SquareMatrix record pattern");
        }
    }

    @Test
    @DisplayName("Matrix equality and hashCode contracts")
    void testMatrixEquality() {
        SquareMatrix<Rational> a = SquareMatrix.identity(2, Rational.field());
        SquareMatrix<Rational> b = SquareMatrix.identity(2, Rational.field());
        SquareMatrix<Rational> c = SquareMatrix.zeros(2, Rational.field());

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);

        DenseMatrix<Rational> da = DenseMatrix.zeros(2, 3, Rational.field());
        DenseMatrix<Rational> db = DenseMatrix.zeros(2, 3, Rational.field());
        DenseMatrix<Rational> dc = DenseMatrix.zeros(3, 2, Rational.field());

        assertThat(da).isEqualTo(db);
        assertThat(da.hashCode()).isEqualTo(db.hashCode());
        assertThat(da).isNotEqualTo(dc);
    }

    @Test
    @DisplayName("asSquareMatrix conversion")
    void testAsSquareMatrix() {
        DenseMatrix<Rational> squareDense = DenseMatrix.identity(3, Rational.field());
        SquareMatrix<Rational> sm = squareDense.asSquareMatrix();
        assertThat(sm.dimension()).isEqualTo(3);
        assertThat(sm.determinant()).isEqualTo(Rational.ONE);

        DenseMatrix<Rational> rect = DenseMatrix.zeros(2, 3, Rational.field());
        assertThatThrownBy(rect::asSquareMatrix)
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Row streaming on Matrix interface")
    void testRowStreaming() {
        Matrix<Rational> matrix = DenseMatrix.identity(3, Rational.field());
        List<List<Rational>> rows = matrix.streamRows().toList();
        assertThat(rows).hasSize(3);
        assertThat(rows.get(0)).containsExactly(Rational.ONE, Rational.ZERO, Rational.ZERO);
        assertThat(rows.get(1)).containsExactly(Rational.ZERO, Rational.ONE, Rational.ZERO);
        assertThat(rows.get(2)).containsExactly(Rational.ZERO, Rational.ZERO, Rational.ONE);
    }
}
