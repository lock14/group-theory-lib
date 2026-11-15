package lock14.algebra.linear;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.numbers.Quaternion;
import lock14.algebra.numbers.QuaternionDivisionRing;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MatrixADTTest {

    @Test
    public void testExhaustiveDisjointPatternMatchingAndSubmatrix() {
        Matrix<Rational> square = Matrix.zeros(3, 3, Rationals.FIELD);
        Matrix<Rational> rect = Matrix.zeros(2, 4, Rationals.FIELD);

        assertThat(square).isInstanceOf(SquareMatrix.class);
        assertThat(rect).isInstanceOf(RectangularMatrix.class);
        assertThat(square.isSquare()).isTrue();
        assertThat(rect.isSquare()).isFalse();
        assertThat(classifyMatrix(square)).isEqualTo("Square 3x3");
        assertThat(classifyMatrix(rect)).isEqualTo("Rectangular 2x4");

        // Submatrix auto-promotes to SquareMatrix when r1 - r0 == c1 - c0
        Matrix<Rational> subSq = rect.submatrix(0, 2, 1, 3);
        Matrix<Rational> subRect = rect.submatrix(0, 2, 0, 3);
        assertThat(subSq).isInstanceOf(SquareMatrix.class);
        assertThat(subRect).isInstanceOf(RectangularMatrix.class);

        Matrix<Rational> fromSqToSq = square.submatrix(0, 2, 0, 2);
        Matrix<Rational> fromSqToRect = square.submatrix(0, 2, 0, 3);
        assertThat(fromSqToSq).isInstanceOf(SquareMatrix.class);
        assertThat(fromSqToRect).isInstanceOf(RectangularMatrix.class);

        // Square dimensions passed to RectangularMatrix.zeros are rejected
        assertThatThrownBy(() -> RectangularMatrix.zeros(2, 2, Rationals.FIELD))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static String classifyMatrix(Matrix<?> matrix) {
        return switch (matrix) {
            case SquareMatrix<?> sm -> "Square " + sm.dimension() + "x" + sm.dimension();
            case RectangularMatrix<?> rm -> "Rectangular " + rm.rows() + "x" + rm.cols();
        };
    }

    @Test
    public void testMatrixStaticFactoriesDefaultVectorsAndSubmatrixBounds() {
        var f = Rationals.FIELD;

        Matrix<Rational> sqVarargs = Matrix.of(2, 2, f,
            Rational.ONE, Rational.TWO,
            Rational.of(3), Rational.of(4));
        Matrix<Rational> rectVarargs = Matrix.of(2, 3, f,
            Rational.ONE, Rational.TWO, Rational.of(3),
            Rational.of(4), Rational.of(5), Rational.of(6));
        assertThat(sqVarargs).isInstanceOf(SquareMatrix.class);
        assertThat(rectVarargs).isInstanceOf(RectangularMatrix.class);

        assertThat(sqVarargs.rowVector(1)).isEqualTo(Vector.of(f, Rational.of(3), Rational.of(4)));
        assertThat(sqVarargs.colVector(0)).isEqualTo(Vector.of(f, Rational.ONE, Rational.of(3)));
        assertThat(rectVarargs.rowVector(0)).isEqualTo(Vector.of(f, Rational.ONE, Rational.TWO, Rational.of(3)));
        assertThat(rectVarargs.colVector(2)).isEqualTo(Vector.of(f, Rational.of(3), Rational.of(6)));

        Matrix<Rational> sq2D = Matrix.of(f, new Rational[][] {
            {Rational.ONE, Rational.TWO},
            {Rational.of(3), Rational.of(4)}
        });
        Matrix<Rational> rect2D = Matrix.of(f, new Rational[][] {
            {Rational.ONE, Rational.TWO, Rational.of(3)},
            {Rational.of(4), Rational.of(5), Rational.of(6)}
        });
        assertThat(sq2D).isEqualTo(sqVarargs);
        assertThat(rect2D).isEqualTo(rectVarargs);
        assertThat(Matrix.identity(2, f)).isEqualTo(SquareMatrix.identity(2, f));

        assertThatThrownBy(() -> Matrix.of(0, 2, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Matrix.of(-1, 2, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Matrix.of(2, 0, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Matrix.of(2, -1, f)).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Matrix.of(null, new Rational[][] {{Rational.ONE}}))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Matrix.of(f, (Rational[][]) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Matrix.of(f, new Rational[0][0]))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Matrix.of(f, new Rational[][] {null}))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Matrix.of(f, new Rational[][] {{}}))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Matrix.zeros(0, 2, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Matrix.zeros(2, 0, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Matrix.zeros(2, 2, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Matrix.identity(0, f)).isInstanceOf(IllegalArgumentException.class);

        // Exhaustive submatrix boundary conditions on a 2x3 matrix
        assertThatThrownBy(() -> rectVarargs.submatrix(-1, 1, 0, 2))
            .isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> rectVarargs.submatrix(0, 3, 0, 2))
            .isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> rectVarargs.submatrix(1, 1, 0, 2))
            .isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> rectVarargs.submatrix(2, 1, 0, 2))
            .isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> rectVarargs.submatrix(0, 2, -1, 2))
            .isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> rectVarargs.submatrix(0, 2, 0, 4))
            .isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> rectVarargs.submatrix(0, 2, 1, 1))
            .isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> rectVarargs.submatrix(0, 2, 2, 1))
            .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testCrossAdtOperationsRowStreamingAndTraceExclusivity() {
        List<String> matrixMethods = Arrays.stream(Matrix.class.getDeclaredMethods())
            .map(Method::getName)
            .toList();
        assertThat(matrixMethods).doesNotContain("trace", "asSquareMatrix");

        SquareMatrix<Rational> sq = SquareMatrix.of(2, Rationals.FIELD,
            Rational.of(2), Rational.of(1),
            Rational.of(0), Rational.of(3));
        RectangularMatrix<Rational> rect = RectangularMatrix.of(2, 3, Rationals.FIELD,
            Rational.of(1), Rational.of(2), Rational.of(3),
            Rational.of(4), Rational.of(5), Rational.of(6));

        RectangularMatrix<Rational> prod = sq.multiply(rect);
        assertThat(prod).isEqualTo(RectangularMatrix.of(2, 3, Rationals.FIELD,
            Rational.of(6), Rational.of(9), Rational.of(12),
            Rational.of(12), Rational.of(15), Rational.of(18)));

        // (2x3) * (3x2) -> SquareMatrix (2x2) narrowed via sealed pattern matching
        Matrix<Rational> gram = rect.multiply(rect.transpose());
        assertThat(gram).isInstanceOf(SquareMatrix.class);
        if (gram instanceof SquareMatrix<Rational> squareGram) {
            assertThat(squareGram.trace()).isEqualTo(Rational.of(14 + 77));
        }

        assertThat(sq.streamRows().toList()).containsExactly(
            List.of(Rational.of(2), Rational.of(1)),
            List.of(Rational.of(0), Rational.of(3))
        );
        assertThatThrownBy(() -> sq.add(rect)).isInstanceOf(DimensionMismatchException.class);
    }

    @Provide
    Arbitrary<RectangularMatrix<Rational>> rectangularMatrices3x5() {
        Arbitrary<Rational> r = Arbitraries.integers().between(-4, 4).map(Rational::of);
        return r.array(Rational[].class).ofSize(15)
            .map(arr -> RectangularMatrix.of(3, 5, Rationals.FIELD, arr));
    }

    @Property
    public void rankNullityAndKernelAnnihilationOverRationals(
            @ForAll("rectangularMatrices3x5") RectangularMatrix<Rational> a) {
        int rank = a.rank(Rationals.FIELD);
        int nullity = a.nullity(Rationals.FIELD);
        assertThat(rank + nullity).isEqualTo(5);

        List<Vector<Rational>> basis = a.kernel(Rationals.FIELD);
        assertThat(basis).hasSize(nullity);
        Vector<Rational> zero3 = Vector.zeros(3, Rationals.FIELD);
        for (Vector<Rational> v : basis) {
            assertThat(a.multiply(v)).isEqualTo(zero3);
        }
    }

    @Test
    public void testKernelOverNonCommutativeRationalQuaternions() {
        var hq = QuaternionDivisionRing.overRationals();
        RectangularMatrix<Quaternion<Rational>> qm = RectangularMatrix.of(2, 3, hq,
            hq.one(), hq.i(), hq.j(),
            hq.k(), hq.k().multiply(hq.i()), hq.k().multiply(hq.j()));

        // Row 1 is k * Row 0 -> rank 1, nullity 2!
        assertThat(qm.rank(hq)).isEqualTo(1);
        assertThat(qm.nullity(hq)).isEqualTo(2);
        Vector<Quaternion<Rational>> zero2 = Vector.zeros(2, hq);
        for (Vector<Quaternion<Rational>> v : qm.kernel(hq)) {
            assertThat(qm.multiply(v)).isEqualTo(zero2);
        }
    }
}
