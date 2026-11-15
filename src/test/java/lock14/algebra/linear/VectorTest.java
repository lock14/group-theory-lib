package lock14.algebra.linear;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import lock14.algebra.quotient.QuotientField;
import lock14.algebra.quotient.Residue;
import lock14.algebra.quotient.ZMod;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class VectorTest {

    @Test
    public void testVectorFactoriesImmutabilityAndGuards() {
        CoordinateVectorSpace<Rational> r3 = new CoordinateVectorSpace<>(3, Rationals.FIELD);
        List<Vector<Rational>> basis = r3.standardBasis();
        assertThat(basis).hasSize(3);
        assertThat(basis.get(0)).isEqualTo(r3.of(Rational.ONE, Rational.ZERO, Rational.ZERO));

        Rational[] raw = {Rational.ONE, Rational.TWO};
        Vector<Rational> v = Vector.of(Rationals.FIELD, raw);
        raw[0] = Rational.TEN;
        assertThat(v.dimension()).isEqualTo(2);
        assertThat(v.ring()).isEqualTo(Rationals.FIELD);
        assertThat(v.get(0)).isEqualTo(Rational.ONE);
        assertThat(v.toList()).containsExactly(Rational.ONE, Rational.TWO);
        assertThatThrownBy(() -> v.toList().add(Rational.ONE))
            .isInstanceOf(UnsupportedOperationException.class);

        List<Rational> mutableList = new ArrayList<>(List.of(Rational.of(3), Rational.of(4)));
        Vector<Rational> fromList = Vector.of(Rationals.FIELD, mutableList);
        mutableList.set(0, Rational.TEN);
        assertThat(fromList.get(0)).isEqualTo(Rational.of(3));

        Vector<Rational> updated = v.with(1, Rational.TEN);
        assertThat(updated).isEqualTo(Vector.of(Rationals.FIELD, Rational.ONE, Rational.TEN));
        assertThat(v.get(1)).isEqualTo(Rational.TWO);

        Vector<Rational> zeros = Vector.zeros(3, Rationals.FIELD);
        assertThat(zeros).isEqualTo(Vector.of(Rationals.FIELD, Rational.ZERO, Rational.ZERO, Rational.ZERO));

        assertThatThrownBy(() -> Vector.of(null, Rational.ONE))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Vector.of(Rationals.FIELD, (Rational[]) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Vector.of(Rationals.FIELD))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Vector.of(Rationals.FIELD, Rational.ONE, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Vector.of(null, List.of(Rational.ONE)))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Vector.of(Rationals.FIELD, (List<Rational>) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Vector.of(Rationals.FIELD, List.of()))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Vector.of(Rationals.FIELD, Arrays.asList(Rational.ONE, null)))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Vector.zeros(0, Rationals.FIELD))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Vector.zeros(-2, Rationals.FIELD))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Vector.zeros(2, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> v.get(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> v.get(2)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> v.with(0, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> v.with(-1, Rational.ONE)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> v.with(2, Rational.ONE)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testArithmeticDotAndMatrixVectorProducts() {
        var f = Rationals.FIELD;
        Vector<Rational> v1 = Vector.of(f, Rational.of(1), Rational.of(2), Rational.of(3));
        Vector<Rational> v2 = Vector.of(f, Rational.of(4), Rational.of(5), Rational.of(6));

        assertThat(v1.add(v2)).isEqualTo(Vector.of(f, Rational.of(5), Rational.of(7), Rational.of(9)));
        assertThat(v1.negate()).isEqualTo(Vector.of(f, Rational.of(-1), Rational.of(-2), Rational.of(-3)));
        assertThat(v2.subtract(v1)).isEqualTo(Vector.of(f, Rational.of(3), Rational.of(3), Rational.of(3)));
        assertThat(v1.scale(Rational.of(3))).isEqualTo(Vector.of(f, Rational.of(3), Rational.of(6), Rational.of(9)));
        assertThat(v1.dot(v2)).isEqualTo(Rational.of(32));

        RectangularMatrix<Rational> rect = RectangularMatrix.of(2, 3, f,
            Rational.of(1), Rational.of(0), Rational.of(2),
            Rational.of(-1), Rational.of(3), Rational.of(1));
        assertThat(rect.multiply(v1)).isEqualTo(Vector.of(f, Rational.of(7), Rational.of(8)));

        Vector<Rational> u = Vector.of(f, Rational.of(2), Rational.of(3));
        assertThat(u.multiply(rect)).isEqualTo(Vector.of(f, Rational.of(-1), Rational.of(9), Rational.of(7)));

        // Row-vector times SquareMatrix with sparse zero vector entry and all-zero matrix column
        Vector<Rational> sparseVec = Vector.of(f, Rational.ZERO, Rational.TWO, Rational.ONE);
        SquareMatrix<Rational> sparseSq = SquareMatrix.of(3, f,
            Rational.ONE, Rational.ZERO, Rational.TWO,
            Rational.of(3), Rational.ZERO, Rational.ZERO,
            Rational.of(4), Rational.ZERO, Rational.of(5));
        assertThat(sparseVec.multiply(sparseSq))
            .isEqualTo(Vector.of(f, Rational.TEN, Rational.ZERO, Rational.of(5)));

        assertThatThrownBy(() -> v1.scale(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> v1.add(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> v1.subtract(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> v1.dot(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> v1.add(u)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> v1.subtract(u)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> v1.dot(u)).isInstanceOf(DimensionMismatchException.class);

        assertThatThrownBy(() -> rect.multiply(u)).isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> v1.multiply(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> v1.multiply(rect)).isInstanceOf(DimensionMismatchException.class);

        QuotientField<BigInteger> f5 = ZMod.field(5);
        QuotientField<BigInteger> f7 = ZMod.field(7);
        Vector<Residue<BigInteger>> vec5 = Vector.zeros(2, f5).with(0, ZMod.element(f5, 2));
        Vector<Residue<BigInteger>> vec7 = Vector.zeros(2, f7).with(0, ZMod.element(f7, 2));
        SquareMatrix<Residue<BigInteger>> sq7 = SquareMatrix.identity(2, f7);

        assertThatThrownBy(() -> vec5.add(vec7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> vec5.subtract(vec7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> vec5.dot(vec7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> vec5.multiply(sq7)).isInstanceOf(IncompatibleAlgebraException.class);
    }

    @Test
    public void testEqualsHashCodeAndToString() {
        Vector<Rational> v = Vector.of(Rationals.FIELD, Rational.ONE, Rational.TWO);
        Vector<Rational> copy = Vector.of(Rationals.FIELD, List.of(Rational.ONE, Rational.TWO));
        Vector<Rational> diffEntry = Vector.of(Rationals.FIELD, Rational.ONE, Rational.TEN);
        Vector<Rational> diffDim = Vector.of(Rationals.FIELD, Rational.ONE, Rational.TWO, Rational.ZERO);

        QuotientField<BigInteger> f5 = ZMod.field(5);
        QuotientField<BigInteger> f7 = ZMod.field(7);
        Vector<Residue<BigInteger>> vec5 = Vector.zeros(2, f5);
        Vector<Residue<BigInteger>> vec7 = Vector.zeros(2, f7);

        assertThat(v.equals(v)).isTrue();
        assertThat(v.equals(copy)).isTrue();
        assertThat(v.equals(null)).isFalse();
        assertThat(v.equals("not a vector")).isFalse();
        assertThat(v.equals(diffDim)).isFalse();
        assertThat(v.equals(diffEntry)).isFalse();
        assertThat(vec5.equals(vec7)).isFalse();

        assertThat(v.hashCode()).isEqualTo(copy.hashCode());
        assertThat(v.toString()).isEqualTo("[1, 2]");
    }
}
