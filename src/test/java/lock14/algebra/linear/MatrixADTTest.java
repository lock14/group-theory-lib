package lock14.algebra.linear;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.numbers.Rational;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatrixADTTest {

    @Test
    @DisplayName("Exhaustive Pattern Matching on Disjoint Matrix ADT")
    void testExhaustivePatternMatching() {
        Matrix<Rational> square = Matrix.zeros(3, 3, Rational.field());
        Matrix<Rational> dense = Matrix.zeros(2, 4, Rational.field());

        assertThat(square).isInstanceOf(SquareMatrix.class);
        assertThat(dense).isInstanceOf(DenseMatrix.class);
        assertThat(classifyMatrix(square)).isEqualTo("Square 3x3");
        assertThat(classifyMatrix(dense)).isEqualTo("Rectangular 2x4");
    }

    private String classifyMatrix(Matrix<?> matrix) {
        return switch (matrix) {
            case SquareMatrix<?> sm -> "Square " + sm.dimension() + "x" + sm.dimension();
            case DenseMatrix<?> dm -> "Rectangular " + dm.rows() + "x" + dm.cols();
        };
    }

    @Test
    @DisplayName("Canonical Matrix.of and Matrix.identity factories")
    void testCanonicalMatrixFactories() {
        Matrix<Rational> mSq = Matrix.of(2, 2, Rational.field(),
                Rational.ONE, Rational.TWO,
                Rational.ZERO, Rational.TEN);
        Matrix<Rational> mRect = Matrix.of(1, 3, Rational.field(),
                Rational.ONE, Rational.TWO, Rational.TEN);

        assertThat(mSq).isInstanceOf(SquareMatrix.class);
        assertThat(mRect).isInstanceOf(DenseMatrix.class);
        assertThat(Matrix.identity(2, Rational.field())).isEqualTo(SquareMatrix.identity(2, Rational.field()));
    }

    @Test
    @DisplayName("Polymorphic add, subtract, negate, and scale on Matrix<T>")
    void testPolymorphicModuleOperationsOnMatrix() {
        Matrix<Rational> a = Matrix.of(2, 3, Rational.field(),
                Rational.of(1), Rational.of(2), Rational.of(3),
                Rational.of(4), Rational.of(5), Rational.of(6));
        Matrix<Rational> b = Matrix.of(2, 3, Rational.field(),
                Rational.of(6), Rational.of(5), Rational.of(4),
                Rational.of(3), Rational.of(2), Rational.of(1));

        assertThat(a.add(b)).isEqualTo(Matrix.of(2, 3, Rational.field(),
                Rational.of(7), Rational.of(7), Rational.of(7),
                Rational.of(7), Rational.of(7), Rational.of(7)));
        assertThat(a.subtract(b)).isEqualTo(a.add(b.negate()));
        assertThat(a.scale(Rational.of(2))).isEqualTo(a.add(a));

        Matrix<Rational> sq = Matrix.identity(2, Rational.field());
        assertThat(sq.add(sq).scale(Rational.of(3))).isEqualTo(
                SquareMatrix.identity(2, Rational.field()).scale(Rational.of(6))
        );
        assertThatThrownBy(() -> a.add(sq))
                .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> sq.subtract(a))
                .isInstanceOf(DimensionMismatchException.class);
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
    @DisplayName("SquareMatrix.with and Matrix.with immutable entry updates")
    void testSquareMatrixWith() {
        SquareMatrix<Rational> id = SquareMatrix.identity(2, Rational.field());
        SquareMatrix<Rational> updated = id.with(0, 1, Rational.of(5));

        assertThat(id.get(0, 1)).isEqualTo(Rational.ZERO);
        assertThat(updated.get(0, 1)).isEqualTo(Rational.of(5));
        assertThat(updated.determinant()).isEqualTo(Rational.ONE);

        Matrix<Rational> polyRef = id;
        Matrix<Rational> polyUpdated = polyRef.with(1, 0, Rational.of(-3));
        assertThat(polyUpdated).isInstanceOf(SquareMatrix.class);
        assertThat(polyUpdated.get(1, 0)).isEqualTo(Rational.of(-3));
    }

    @Test
    @DisplayName("Cross-ADT Matrix multiplication and Matrix-Vector multiplication")
    void testCrossAdtMultiplication() {
        SquareMatrix<Rational> sq = SquareMatrix.of(2, Rational.field(),
                Rational.of(2), Rational.of(1),
                Rational.of(0), Rational.of(3));
        DenseMatrix<Rational> rect = DenseMatrix.of(2, 3, Rational.field(),
                Rational.of(1), Rational.of(2), Rational.of(3),
                Rational.of(4), Rational.of(5), Rational.of(6));

        // SquareMatrix (2x2) * DenseMatrix (2x3) -> DenseMatrix (2x3)
        DenseMatrix<Rational> prod1 = sq.multiply(rect);
        assertThat(prod1).isEqualTo(DenseMatrix.of(2, 3, Rational.field(),
                Rational.of(6), Rational.of(9), Rational.of(12),
                Rational.of(12), Rational.of(15), Rational.of(18)));

        // Polymorphic Matrix * Matrix
        Matrix<Rational> mSq = sq;
        Matrix<Rational> mRect = rect;
        assertThat(mSq.multiply(mRect)).isEqualTo(prod1);
        assertThat(mSq.multiply(mSq)).isInstanceOf(SquareMatrix.class);

        // DenseMatrix (3x2) * SquareMatrix (2x2) -> DenseMatrix (3x2)
        DenseMatrix<Rational> rectT = rect.transpose();
        DenseMatrix<Rational> prod2 = rectT.multiply(sq);
        assertThat(prod2).isEqualTo(DenseMatrix.of(3, 2, Rational.field(),
                Rational.of(2), Rational.of(13),
                Rational.of(4), Rational.of(17),
                Rational.of(6), Rational.of(21)));

        // DenseMatrix (2x3) * DenseMatrix (3x2) -> SquareMatrix (2x2)
        Matrix<Rational> gram = rect.multiply(rectT);
        assertThat(gram).isInstanceOf(SquareMatrix.class);
        assertThat(gram.asSquareMatrix().dimension()).isEqualTo(2);

        // SquareMatrix * Vector
        Vector<Rational> v = Vector.of(Rational.field(), Rational.of(5), Rational.of(2));
        assertThat(mSq.multiply(v)).isEqualTo(Vector.of(Rational.field(), Rational.of(12), Rational.of(6)));
    }

    @Test
    @DisplayName("trace() is exclusively defined on SquareMatrix, not general Matrix")
    void testTraceExclusiveToSquareMatrix() {
        boolean matrixHasTrace = Arrays.stream(Matrix.class.getDeclaredMethods())
                .map(Method::getName)
                .anyMatch("trace"::equals);
        assertThat(matrixHasTrace).isFalse();

        SquareMatrix<Rational> sq = SquareMatrix.identity(3, Rational.field());
        assertThat(sq.trace()).isEqualTo(Rational.of(3));
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
        Matrix<Rational> polySquare = Matrix.identity(3, Rational.field());
        SquareMatrix<Rational> sm = polySquare.asSquareMatrix();
        assertThat(sm.dimension()).isEqualTo(3);
        assertThat(sm.determinant()).isEqualTo(Rational.ONE);

        DenseMatrix<Rational> rect = DenseMatrix.zeros(2, 3, Rational.field());
        assertThatThrownBy(rect::asSquareMatrix)
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Row streaming on Matrix interface")
    void testRowStreaming() {
        Matrix<Rational> matrix = Matrix.identity(3, Rational.field());
        List<List<Rational>> rows = matrix.streamRows().toList();
        assertThat(rows).hasSize(3);
        assertThat(rows.get(0)).containsExactly(Rational.ONE, Rational.ZERO, Rational.ZERO);
        assertThat(rows.get(1)).containsExactly(Rational.ZERO, Rational.ONE, Rational.ZERO);
        assertThat(rows.get(2)).containsExactly(Rational.ZERO, Rational.ZERO, Rational.ONE);
    }
}
