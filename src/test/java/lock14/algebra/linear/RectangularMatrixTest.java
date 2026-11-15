package lock14.algebra.linear;

import java.math.BigInteger;
import java.util.List;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.laws.ModuleLaws;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import lock14.algebra.quotient.QuotientField;
import lock14.algebra.quotient.Residue;
import lock14.algebra.quotient.ZMod;
import lock14.algebra.structure.AbelianGroup;
import lock14.algebra.structure.LeftModule;
import lock14.algebra.structure.Ring;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RectangularMatrixTest extends ModuleLaws<RectangularMatrix<Rational>, Rational> {

    private static final LeftModule<RectangularMatrix<Rational>, Rational> RECT_MODULE =
        new LeftModule<>() {
            private final AbelianGroup<RectangularMatrix<Rational>> group = new AbelianGroup<>() {
                @Override
                public RectangularMatrix<Rational> identity() {
                    return RectangularMatrix.zeros(2, 3, Rationals.FIELD);
                }

                @Override
                public RectangularMatrix<Rational> combine(
                        RectangularMatrix<Rational> left,
                        RectangularMatrix<Rational> right) {
                    return left.add(right);
                }

                @Override
                public RectangularMatrix<Rational> inverse(RectangularMatrix<Rational> element) {
                    return element.negate();
                }
            };

            @Override
            public AbelianGroup<RectangularMatrix<Rational>> moduleGroup() {
                return group;
            }

            @Override
            public Ring<Rational> scalarRing() {
                return Rationals.FIELD;
            }

            @Override
            public RectangularMatrix<Rational> scale(Rational scalar, RectangularMatrix<Rational> element) {
                return element.scale(scalar);
            }
        };

    @Override
    protected LeftModule<RectangularMatrix<Rational>, Rational> module() {
        return RECT_MODULE;
    }

    @Provide
    Arbitrary<Rational> scalars() {
        return Arbitraries.oneOf(
            Arbitraries.of(Rational.ZERO, Rational.ONE, Rational.of(-1), Rational.of(2, 3)),
            Arbitraries.integers().between(-10, 10).map(Rational::of)
        );
    }

    @Provide
    Arbitrary<RectangularMatrix<Rational>> vectors() {
        Arbitrary<Rational> entries = Arbitraries.integers().between(-6, 6).map(Rational::of);
        return Arbitraries.oneOf(
            Arbitraries.of(
                RectangularMatrix.zeros(2, 3, Rationals.FIELD),
                RectangularMatrix.zeros(2, 3, Rationals.FIELD).with(0, 0, Rational.ONE)
            ),
            entries.array(Rational[].class).ofSize(6)
                .map(arr -> RectangularMatrix.of(2, 3, Rationals.FIELD, arr))
        );
    }

    @Test
    public void testFactoriesDefensiveCopyAndValidationGuards() {
        var f = Rationals.FIELD;

        Rational[] raw = {Rational.ONE, Rational.TWO, Rational.of(3)};
        RectangularMatrix<Rational> m = RectangularMatrix.of(1, 3, f, raw);
        raw[0] = Rational.TEN;
        assertThat(m.get(0, 0)).isEqualTo(Rational.ONE);
        assertThat(m.rows()).isEqualTo(1);
        assertThat(m.cols()).isEqualTo(3);
        assertThat(m.ring()).isEqualTo(f);
        assertThat(m.isSquare()).isFalse();

        Rational[][] raw2D = {
            {Rational.ONE, Rational.TWO, Rational.of(3)},
            {Rational.of(4), Rational.of(5), Rational.of(6)}
        };
        RectangularMatrix<Rational> m2D = RectangularMatrix.of(f, raw2D);
        raw2D[0][0] = Rational.TEN;
        assertThat(m2D.get(0, 0)).isEqualTo(Rational.ONE);

        assertThatThrownBy(() -> RectangularMatrix.of(0, 2, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RectangularMatrix.of(-1, 2, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RectangularMatrix.of(2, 0, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RectangularMatrix.of(2, -1, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RectangularMatrix.of(2, 2, f,
            Rational.ONE, Rational.ZERO, Rational.ZERO, Rational.ONE))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RectangularMatrix.of(1, 2, null, Rational.ONE, Rational.TWO))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> RectangularMatrix.of(1, 2, f, (Rational[]) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> RectangularMatrix.of(50_000, 50_001, f, Rational.ONE))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RectangularMatrix.of(2, 3, f, Rational.ONE, Rational.TWO))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> RectangularMatrix.of(1, 2, f, Rational.ONE, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> RectangularMatrix.of(null, raw2D)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> RectangularMatrix.of(f, (Rational[][]) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> RectangularMatrix.of(f, new Rational[][] {
            {Rational.ONE, Rational.TWO},
            {Rational.of(3), Rational.of(4)}
        })).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RectangularMatrix.of(f, new Rational[][] {
            {Rational.ONE, Rational.TWO, Rational.ZERO},
            {Rational.ONE, Rational.TWO}
        })).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> RectangularMatrix.zeros(0, 2, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RectangularMatrix.zeros(2, 0, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RectangularMatrix.zeros(2, 2, f)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RectangularMatrix.zeros(2, 3, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testElementAccessorsRowColStreamAndWith() {
        var f = Rationals.FIELD;
        RectangularMatrix<Rational> m = RectangularMatrix.of(2, 3, f,
            Rational.ONE, Rational.TWO, Rational.of(3),
            Rational.of(4), Rational.of(5), Rational.of(6));

        assertThat(m.get(0, 0)).isEqualTo(Rational.ONE);
        assertThat(m.get(1, 2)).isEqualTo(Rational.of(6));
        assertThatThrownBy(() -> m.get(-1, 0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> m.get(2, 0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> m.get(0, -1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> m.get(0, 3)).isInstanceOf(IndexOutOfBoundsException.class);

        RectangularMatrix<Rational> updated = m.with(1, 2, Rational.TEN);
        assertThat(updated.get(1, 2)).isEqualTo(Rational.TEN);
        assertThat(m.get(1, 2)).isEqualTo(Rational.of(6));
        assertThatThrownBy(() -> m.with(0, 0, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> m.with(-1, 0, Rational.ONE)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> m.with(0, 3, Rational.ONE)).isInstanceOf(IndexOutOfBoundsException.class);

        assertThat(m.row(0)).containsExactly(Rational.ONE, Rational.TWO, Rational.of(3));
        assertThat(m.row(1)).containsExactly(Rational.of(4), Rational.of(5), Rational.of(6));
        assertThatThrownBy(() -> m.row(0).add(Rational.ZERO)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> m.row(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> m.row(2)).isInstanceOf(IndexOutOfBoundsException.class);

        assertThat(m.col(0)).containsExactly(Rational.ONE, Rational.of(4));
        assertThat(m.col(2)).containsExactly(Rational.of(3), Rational.of(6));
        assertThatThrownBy(() -> m.col(0).add(Rational.ZERO)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> m.col(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> m.col(3)).isInstanceOf(IndexOutOfBoundsException.class);

        assertThat(m.streamRows().toList()).containsExactly(
            List.of(Rational.ONE, Rational.TWO, Rational.of(3)),
            List.of(Rational.of(4), Rational.of(5), Rational.of(6))
        );
    }

    @Test
    public void testArithmeticPolymorphicOverloadsAndMatrixProducts() {
        var f = Rationals.FIELD;
        RectangularMatrix<Rational> a = RectangularMatrix.of(2, 3, f,
            Rational.ONE, Rational.ZERO, Rational.TWO,
            Rational.of(3), Rational.ONE, Rational.ZERO);
        RectangularMatrix<Rational> b = RectangularMatrix.of(2, 3, f,
            Rational.TWO, Rational.of(4), Rational.of(-1),
            Rational.ZERO, Rational.of(3), Rational.of(5));
        RectangularMatrix<Rational> c3x4 = RectangularMatrix.of(3, 4, f,
            Rational.ONE, Rational.ZERO, Rational.TWO, Rational.ONE,
            Rational.ZERO, Rational.of(3), Rational.ONE, Rational.ZERO,
            Rational.TWO, Rational.ONE, Rational.ZERO, Rational.of(4));
        SquareMatrix<Rational> sq3 = SquareMatrix.of(3, f,
            Rational.TWO, Rational.ZERO, Rational.ONE,
            Rational.ZERO, Rational.of(3), Rational.ZERO,
            Rational.ONE, Rational.ZERO, Rational.TWO);
        SquareMatrix<Rational> sq2 = SquareMatrix.identity(2, f);

        Matrix<Rational> polyA = a;
        assertThat(polyA.add(b)).isEqualTo(a.add(b));
        assertThat(polyA.subtract(b)).isEqualTo(a.subtract(b));
        assertThat(a.subtract(b)).isEqualTo(RectangularMatrix.of(2, 3, f,
            Rational.of(-1), Rational.of(-4), Rational.of(3),
            Rational.of(3), Rational.of(-2), Rational.of(-5)));
        assertThat(a.transpose().transpose()).isEqualTo(a);

        // (2x3) * (3x4) -> RectangularMatrix (2x4)
        Matrix<Rational> prodRect = a.multiply(c3x4);
        assertThat(prodRect).isInstanceOf(RectangularMatrix.class);
        assertThat(prodRect).isEqualTo(RectangularMatrix.of(2, 4, f,
            Rational.of(5), Rational.TWO, Rational.TWO, Rational.of(9),
            Rational.of(3), Rational.of(3), Rational.of(7), Rational.of(3)));
        assertThat(polyA.multiply(c3x4)).isEqualTo(prodRect);

        // (2x3) * (3x3) -> RectangularMatrix (2x3)
        RectangularMatrix<Rational> prodSqRight = a.multiply(sq3);
        assertThat(prodSqRight).isEqualTo(RectangularMatrix.of(2, 3, f,
            Rational.of(4), Rational.ZERO, Rational.of(5),
            Rational.of(6), Rational.of(3), Rational.of(3)));
        assertThat(polyA.multiply(sq3)).isEqualTo(prodSqRight);

        // Validation and mismatch branches
        assertThatThrownBy(() -> a.add((RectangularMatrix<Rational>) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.add((Matrix<Rational>) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.subtract((RectangularMatrix<Rational>) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.subtract((Matrix<Rational>) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.multiply((RectangularMatrix<Rational>) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.multiply((SquareMatrix<Rational>) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.multiply((Matrix<Rational>) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.multiply((Vector<Rational>) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> a.scale(null))
            .isInstanceOf(NullPointerException.class);

        RectangularMatrix<Rational> r2x4 = RectangularMatrix.zeros(2, 4, f);
        RectangularMatrix<Rational> r3x2 = RectangularMatrix.zeros(3, 2, f);
        assertThatThrownBy(() -> a.add(r3x2)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> a.add(r2x4)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> a.subtract(r3x2)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> a.subtract(r2x4)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> a.add(sq2)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> a.subtract(sq2)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> a.multiply(r2x4)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> a.multiply(sq2)).isInstanceOf(DimensionMismatchException.class);

        QuotientField<BigInteger> f5 = ZMod.field(5);
        QuotientField<BigInteger> f7 = ZMod.field(7);
        RectangularMatrix<Residue<BigInteger>> r5 = RectangularMatrix.zeros(2, 3, f5);
        RectangularMatrix<Residue<BigInteger>> r7SameShape = RectangularMatrix.zeros(2, 3, f7);
        RectangularMatrix<Residue<BigInteger>> r7Mult = RectangularMatrix.zeros(3, 4, f7);
        SquareMatrix<Residue<BigInteger>> sq7 = SquareMatrix.identity(3, f7);
        Vector<Residue<BigInteger>> vec7 = Vector.zeros(3, f7);

        assertThatThrownBy(() -> r5.add(r7SameShape)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> r5.subtract(r7SameShape)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> r5.add(sq7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> r5.subtract(sq7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> r5.multiply(r7Mult)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> r5.multiply(sq7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> r5.multiply(vec7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> r5.rref(f7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> a.rref(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testFullColumnRankTrivialKernelEqualsHashCodeAndToString() {
        var f = Rationals.FIELD;
        RectangularMatrix<Rational> tallFullRank = RectangularMatrix.of(3, 2, f,
            Rational.ONE, Rational.TWO,
            Rational.of(3), Rational.of(5),
            Rational.ZERO, Rational.ONE);
        assertThat(tallFullRank.rank(f)).isEqualTo(2);
        assertThat(tallFullRank.nullity(f)).isEqualTo(0);
        assertThat(tallFullRank.kernel(f)).isEmpty();

        RectangularMatrix<Rational> m2x1 = RectangularMatrix.of(2, 1, f, Rational.of(5), Rational.of(6));
        RectangularMatrix<Rational> m2x3 = RectangularMatrix.of(2, 3, f,
            Rational.ONE, Rational.TWO, Rational.of(3),
            Rational.of(4), Rational.of(5), Rational.of(6));
        RectangularMatrix<Rational> m2x3Copy = RectangularMatrix.of(f, new Rational[][] {
            {Rational.ONE, Rational.TWO, Rational.of(3)},
            {Rational.of(4), Rational.of(5), Rational.of(6)}
        });
        RectangularMatrix<Rational> m2x3Diff = m2x3.with(0, 0, Rational.TEN);
        RectangularMatrix<Rational> m3x2 = m2x3.transpose();
        RectangularMatrix<Rational> m2x4 = RectangularMatrix.zeros(2, 4, f);
        SquareMatrix<Rational> sq2 = SquareMatrix.identity(2, f);

        QuotientField<BigInteger> f5 = ZMod.field(5);
        QuotientField<BigInteger> f7 = ZMod.field(7);
        RectangularMatrix<Residue<BigInteger>> r5 = RectangularMatrix.zeros(2, 3, f5);
        RectangularMatrix<Residue<BigInteger>> r7 = RectangularMatrix.zeros(2, 3, f7);

        assertThat(m2x3.equals(m2x3)).isTrue();
        assertThat(m2x3.equals(m2x3Copy)).isTrue();
        assertThat(m2x3.equals(null)).isFalse();
        assertThat(m2x3.equals(sq2)).isFalse();
        assertThat(m2x3.equals(m3x2)).isFalse();
        assertThat(m2x3.equals(m2x4)).isFalse();
        assertThat(m2x3.equals(m2x3Diff)).isFalse();
        assertThat(r5.equals(r7)).isFalse();

        assertThat(m2x3.hashCode()).isEqualTo(m2x3Copy.hashCode());
        assertThat(m2x1.toString()).isEqualTo("[2x1 RectangularMatrix]\n  [5]\n  [6]\n");
        assertThat(m2x3.toString()).isEqualTo("[2x3 RectangularMatrix]\n  [1, 2, 3]\n  [4, 5, 6]\n");
    }
}
