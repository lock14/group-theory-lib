package lock14.algebra.linear;

import java.math.BigInteger;
import java.util.List;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.numbers.Quaternion;
import lock14.algebra.numbers.QuaternionDivisionRing;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import lock14.algebra.quotient.QuotientField;
import lock14.algebra.quotient.Residue;
import lock14.algebra.quotient.ZMod;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SquareMatrixTest {

    @Test
    public void testFactoriesDefensiveCopyAndValidationGuards() {
        var f = Rationals.FIELD;

        Rational[] raw = {Rational.ONE, Rational.TWO, Rational.of(3), Rational.of(4)};
        SquareMatrix<Rational> m = SquareMatrix.of(2, f, raw);
        raw[0] = Rational.TEN;
        assertThat(m.get(0, 0)).isEqualTo(Rational.ONE);
        assertThat(m.dimension()).isEqualTo(2);
        assertThat(m.rows()).isEqualTo(2);
        assertThat(m.cols()).isEqualTo(2);
        assertThat(m.ring()).isEqualTo(f);
        assertThat(m.isSquare()).isTrue();

        Rational[][] raw2D = {
            {Rational.ONE, Rational.TWO},
            {Rational.of(3), Rational.of(4)}
        };
        SquareMatrix<Rational> m2D = SquareMatrix.of(f, raw2D);
        raw2D[0][0] = Rational.TEN;
        assertThat(m2D).isEqualTo(m);

        assertThatThrownBy(() -> SquareMatrix.of(0, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SquareMatrix.of(-2, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SquareMatrix.of(1, null, Rational.ONE)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SquareMatrix.of(1, f, (Rational[]) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SquareMatrix.of(50_000, f, Rational.ONE))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SquareMatrix.of(2, f, Rational.ONE, Rational.TWO, Rational.ZERO))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> SquareMatrix.of(2, f, Rational.ONE, null, Rational.ZERO, Rational.ONE))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> SquareMatrix.of(null, raw2D)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SquareMatrix.of(f, (Rational[][]) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SquareMatrix.of(f, new Rational[0][0]))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SquareMatrix.of(f, new Rational[][] {null}))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SquareMatrix.of(f, new Rational[][] {{}}))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SquareMatrix.of(f, new Rational[][] {
            {Rational.ONE, Rational.TWO},
            null
        })).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SquareMatrix.of(f, new Rational[][] {
            {Rational.ONE, Rational.TWO},
            {Rational.ONE}
        })).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SquareMatrix.of(f, new Rational[][] {
            {Rational.ONE, null},
            {Rational.ONE, Rational.TWO}
        })).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SquareMatrix.of(f, new Rational[][] {
            {Rational.ONE, Rational.TWO, Rational.ZERO},
            {Rational.ONE, Rational.TWO, Rational.ZERO}
        })).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> SquareMatrix.zeros(0, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SquareMatrix.zeros(-1, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SquareMatrix.zeros(2, null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> SquareMatrix.identity(0, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SquareMatrix.identity(-1, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SquareMatrix.identity(2, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testElementAccessorsRowColViewsAndWith() {
        var f = Rationals.FIELD;
        SquareMatrix<Rational> m = SquareMatrix.of(2, f,
            Rational.ONE, Rational.TWO,
            Rational.of(3), Rational.of(4));

        assertThat(m.get(0, 0)).isEqualTo(Rational.ONE);
        assertThat(m.get(1, 1)).isEqualTo(Rational.of(4));
        assertThatThrownBy(() -> m.get(-1, 0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> m.get(2, 0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> m.get(0, -1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> m.get(0, 2)).isInstanceOf(IndexOutOfBoundsException.class);

        SquareMatrix<Rational> updated = m.with(0, 1, Rational.TEN);
        assertThat(updated.get(0, 1)).isEqualTo(Rational.TEN);
        assertThat(m.get(0, 1)).isEqualTo(Rational.TWO);
        assertThatThrownBy(() -> m.with(0, 0, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> m.with(-1, 0, Rational.ONE)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> m.with(0, 2, Rational.ONE)).isInstanceOf(IndexOutOfBoundsException.class);

        assertThat(m.row(0)).containsExactly(Rational.ONE, Rational.TWO);
        assertThat(m.row(1)).containsExactly(Rational.of(3), Rational.of(4));
        assertThatThrownBy(() -> m.row(0).add(Rational.ZERO)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> m.row(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> m.row(2)).isInstanceOf(IndexOutOfBoundsException.class);

        assertThat(m.col(0)).containsExactly(Rational.ONE, Rational.of(3));
        assertThat(m.col(1)).containsExactly(Rational.TWO, Rational.of(4));
        assertThatThrownBy(() -> m.col(0).add(Rational.ZERO)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> m.col(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> m.col(2)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testArithmeticPolymorphicOverloadsAndMismatchExceptions() {
        var f = Rationals.FIELD;
        SquareMatrix<Rational> a = SquareMatrix.of(2, f,
            Rational.ONE, Rational.TWO,
            Rational.ZERO, Rational.of(3));
        SquareMatrix<Rational> b = SquareMatrix.of(2, f,
            Rational.of(4), Rational.ZERO,
            Rational.of(-1), Rational.TWO);
        RectangularMatrix<Rational> rect2x3 = RectangularMatrix.of(2, 3, f,
            Rational.ONE, Rational.ZERO, Rational.TWO,
            Rational.of(3), Rational.ONE, Rational.ZERO);
        RectangularMatrix<Rational> rect3x2 = rect2x3.transpose();
        SquareMatrix<Rational> sq3 = SquareMatrix.identity(3, f);

        Matrix<Rational> polyA = a;
        assertThat(polyA.add(b)).isEqualTo(a.add(b));
        assertThat(polyA.subtract(b)).isEqualTo(a.subtract(b));
        assertThat(a.negate()).isEqualTo(SquareMatrix.of(2, f,
            Rational.of(-1), Rational.of(-2),
            Rational.ZERO, Rational.of(-3)));
        assertThat(a.scale(Rational.of(3))).isEqualTo(SquareMatrix.of(2, f,
            Rational.of(3), Rational.of(6),
            Rational.ZERO, Rational.of(9)));
        assertThat(a.transpose()).isEqualTo(SquareMatrix.of(2, f,
            Rational.ONE, Rational.ZERO,
            Rational.TWO, Rational.of(3)));
        assertThat(a.trace()).isEqualTo(Rational.of(4));

        assertThat(polyA.multiply(b)).isEqualTo(a.multiply(b));
        assertThat(polyA.multiply(rect2x3)).isEqualTo(a.multiply(rect2x3));

        Vector<Rational> v = Vector.of(f, Rational.ZERO, Rational.TWO);
        assertThat(a.multiply(v)).isEqualTo(Vector.of(f, Rational.of(4), Rational.of(6)));

        assertThatThrownBy(() -> a.add((SquareMatrix<Rational>) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.add((Matrix<Rational>) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.subtract((SquareMatrix<Rational>) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.subtract((Matrix<Rational>) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.multiply((SquareMatrix<Rational>) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.multiply((RectangularMatrix<Rational>) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.multiply((Matrix<Rational>) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.multiply((Vector<Rational>) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.scale(null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> a.add(sq3)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> a.subtract(sq3)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> a.multiply(sq3)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> a.add(rect2x3)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> a.subtract(rect2x3)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> a.multiply(rect3x2)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> a.multiply(Vector.zeros(3, f))).isInstanceOf(DimensionMismatchException.class);

        QuotientField<BigInteger> f5 = ZMod.field(5);
        QuotientField<BigInteger> f7 = ZMod.field(7);
        SquareMatrix<Residue<BigInteger>> sq5 = SquareMatrix.identity(2, f5);
        SquareMatrix<Residue<BigInteger>> sq7 = SquareMatrix.identity(2, f7);
        RectangularMatrix<Residue<BigInteger>> r7 = RectangularMatrix.zeros(2, 3, f7);
        Vector<Residue<BigInteger>> v7 = Vector.zeros(2, f7);

        assertThatThrownBy(() -> sq5.add(sq7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> sq5.subtract(sq7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> sq5.multiply(sq7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> sq5.add(r7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> sq5.subtract(r7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> sq5.multiply(r7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> sq5.multiply(v7)).isInstanceOf(IncompatibleAlgebraException.class);
    }

    @Test
    public void testRrefKernelAndMultiRhsSolve() {
        var f = Rationals.FIELD;
        SquareMatrix<Rational> nonsingular = SquareMatrix.of(2, f,
            Rational.ONE, Rational.TWO,
            Rational.of(3), Rational.of(5));

        RrefResult<Rational> rrefNonSing = nonsingular.rref(f);
        assertThat(rrefNonSing.reduced()).isEqualTo(SquareMatrix.identity(2, f));
        assertThat(rrefNonSing.rank()).isEqualTo(2);
        assertThat(rrefNonSing.nullity()).isEqualTo(0);
        assertThat(nonsingular.kernel(f)).isEmpty();

        SquareMatrix<Rational> singular = SquareMatrix.of(2, f,
            Rational.ONE, Rational.TWO,
            Rational.TWO, Rational.of(4));
        assertThat(singular.rank(f)).isEqualTo(1);
        assertThat(singular.nullity(f)).isEqualTo(1);
        List<Vector<Rational>> ker = singular.kernel(f);
        assertThat(ker).hasSize(1);
        assertThat(singular.multiply(ker.get(0))).isEqualTo(Vector.zeros(2, f));

        // Multi-RHS solve returning SquareMatrix when rhs.cols() == dimension
        SquareMatrix<Rational> rhsSq = SquareMatrix.of(2, f,
            Rational.of(5), Rational.ONE,
            Rational.of(13), Rational.of(3));
        Matrix<Rational> solSq = nonsingular.solve(f, rhsSq);
        assertThat(solSq).isInstanceOf(SquareMatrix.class);
        assertThat(nonsingular.multiply(solSq)).isEqualTo(rhsSq);

        // Multi-RHS solve returning RectangularMatrix when rhs.cols() != dimension
        RectangularMatrix<Rational> rhsRect = RectangularMatrix.of(2, 3, f,
            Rational.ONE, Rational.TWO, Rational.ZERO,
            Rational.of(4), Rational.of(5), Rational.ONE);
        Matrix<Rational> solRect = nonsingular.solve(f, rhsRect);
        assertThat(solRect).isInstanceOf(RectangularMatrix.class);
        assertThat(nonsingular.multiply(solRect)).isEqualTo(rhsRect);

        // Non-commutative multi-RHS solve over rational quaternions
        var hq = QuaternionDivisionRing.overRationals();
        SquareMatrix<Quaternion<Rational>> qMat = SquareMatrix.identity(2, hq)
            .with(0, 1, hq.i())
            .with(1, 0, hq.j());
        RectangularMatrix<Quaternion<Rational>> qRhs = RectangularMatrix.zeros(2, 3, hq)
            .with(0, 0, hq.one())
            .with(0, 1, hq.k())
            .with(1, 1, hq.i())
            .with(1, 2, hq.j());
        Matrix<Quaternion<Rational>> qSol = qMat.solve(hq, qRhs);
        assertThat(qMat.multiply(qSol)).isEqualTo(qRhs);

        // Exception branches on rref, solve, tryInverse, inverse, determinant
        assertThatThrownBy(() -> nonsingular.rref(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> nonsingular.determinant(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> nonsingular.tryInverse(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> nonsingular.inverse(null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> nonsingular.solve(null, Vector.zeros(2, f)))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> nonsingular.solve(f, (Vector<Rational>) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> nonsingular.solve(f, Vector.zeros(3, f)))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> singular.solve(f, Vector.zeros(2, f)))
            .isInstanceOf(NonInvertibleElementException.class);

        assertThatThrownBy(() -> nonsingular.solve(null, rhsRect))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> nonsingular.solve(f, (Matrix<Rational>) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> nonsingular.solve(f, rhsRect.transpose()))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> singular.solve(f, rhsRect))
            .isInstanceOf(NonInvertibleElementException.class);

        QuotientField<BigInteger> f5 = ZMod.field(5);
        QuotientField<BigInteger> f7 = ZMod.field(7);
        SquareMatrix<Residue<BigInteger>> sq5 = SquareMatrix.identity(2, f5);
        SquareMatrix<Residue<BigInteger>> sq7 = SquareMatrix.identity(2, f7);
        Vector<Residue<BigInteger>> v5 = Vector.zeros(2, f5);
        Vector<Residue<BigInteger>> v7 = Vector.zeros(2, f7);

        assertThatThrownBy(() -> sq5.rref(f7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> sq5.tryInverse(f7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> sq5.solve(f7, v5)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> sq5.solve(f5, v7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> sq5.solve(f7, sq5)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> sq5.solve(f5, sq7)).isInstanceOf(IncompatibleAlgebraException.class);
    }

    @Test
    public void testEqualsHashCodeAndToString() {
        var f = Rationals.FIELD;
        SquareMatrix<Rational> m1x1 = SquareMatrix.of(1, f, Rational.of(7));
        SquareMatrix<Rational> m2 = SquareMatrix.of(2, f,
            Rational.ONE, Rational.TWO,
            Rational.of(3), Rational.of(4));
        SquareMatrix<Rational> m2Copy = SquareMatrix.of(f, new Rational[][] {
            {Rational.ONE, Rational.TWO},
            {Rational.of(3), Rational.of(4)}
        });
        SquareMatrix<Rational> m2Diff = m2.with(1, 1, Rational.TEN);
        SquareMatrix<Rational> m3 = SquareMatrix.identity(3, f);
        RectangularMatrix<Rational> rect = RectangularMatrix.zeros(2, 3, f);

        QuotientField<BigInteger> f5 = ZMod.field(5);
        QuotientField<BigInteger> f7 = ZMod.field(7);
        SquareMatrix<Residue<BigInteger>> sq5 = SquareMatrix.identity(2, f5);
        SquareMatrix<Residue<BigInteger>> sq7 = SquareMatrix.identity(2, f7);

        assertThat(m2.equals(m2)).isTrue();
        assertThat(m2.equals(m2Copy)).isTrue();
        assertThat(m2.equals(null)).isFalse();
        assertThat(m2.equals(rect)).isFalse();
        assertThat(m2.equals(m3)).isFalse();
        assertThat(m2.equals(m2Diff)).isFalse();
        assertThat(sq5.equals(sq7)).isFalse();

        assertThat(m2.hashCode()).isEqualTo(m2Copy.hashCode());
        assertThat(m1x1.toString()).isEqualTo("[1x1 SquareMatrix]\n  [7]\n");
        assertThat(m2.toString()).isEqualTo("[2x2 SquareMatrix]\n  [1, 2]\n  [3, 4]\n");
    }
}
