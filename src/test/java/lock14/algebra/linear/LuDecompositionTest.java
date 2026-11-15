package lock14.algebra.linear;

import java.math.BigInteger;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.group.Permutation;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import lock14.algebra.quotient.QuotientField;
import lock14.algebra.quotient.Residue;
import lock14.algebra.quotient.ZMod;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LuDecompositionTest {

    @Test
    public void testConstructorValidationAndRecordSemantics() {
        var f = Rationals.FIELD;
        Permutation id2 = Permutation.identity(2);
        SquareMatrix<Rational> lower = SquareMatrix.identity(2, f);
        SquareMatrix<Rational> upper = SquareMatrix.of(2, f,
            Rational.TWO, Rational.ONE,
            Rational.ZERO, Rational.of(3));

        LuDecomposition<Rational> luPos = new LuDecomposition<>(id2, lower, upper, 1, f);
        LuDecomposition<Rational> luNeg = new LuDecomposition<>(Permutation.of(1, 0), lower, upper, -1, f);

        assertThat(luPos.rowPermutation()).isEqualTo(id2);
        assertThat(luPos.lower()).isEqualTo(lower);
        assertThat(luPos.upper()).isEqualTo(upper);
        assertThat(luPos.swapSign()).isEqualTo(1);
        assertThat(luPos.field()).isEqualTo(f);
        assertThat(luNeg.swapSign()).isEqualTo(-1);

        LuDecomposition<Rational> luPosCopy = new LuDecomposition<>(id2, lower, upper, 1, f);
        assertThat(luPos).isEqualTo(luPosCopy);
        assertThat(luPos.hashCode()).isEqualTo(luPosCopy.hashCode());
        assertThat(luPos.toString()).contains("swapSign=1");

        assertThatThrownBy(() -> new LuDecomposition<>(null, lower, upper, 1, f))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LuDecomposition<>(id2, null, upper, 1, f))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LuDecomposition<>(id2, lower, null, 1, f))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LuDecomposition<>(id2, lower, upper, 1, null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LuDecomposition<>(id2, lower, upper, 0, f))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LuDecomposition<>(id2, lower, upper, 2, f))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LuDecomposition<>(id2, lower, upper, -2, f))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testIsNonsingularDeterminantAndSolveBranches() {
        var f = Rationals.FIELD;

        // Nonsingular without row swaps (swapSign == +1)
        SquareMatrix<Rational> noSwap = SquareMatrix.of(2, f,
            Rational.TWO, Rational.ONE,
            Rational.of(4), Rational.of(5));
        LuDecomposition<Rational> luNoSwap = noSwap.lu(f);
        assertThat(luNoSwap.isNonsingular()).isTrue();
        assertThat(luNoSwap.swapSign()).isEqualTo(1);
        assertThat(luNoSwap.determinant()).isEqualTo(Rational.of(6));
        assertThat(luNoSwap.permutationMatrix().multiply(noSwap))
            .isEqualTo(luNoSwap.lower().multiply(luNoSwap.upper()));

        // Nonsingular with single row swap (swapSign == -1)
        SquareMatrix<Rational> withSwap = SquareMatrix.of(3, f,
            Rational.ZERO, Rational.TWO, Rational.ONE,
            Rational.ONE, Rational.of(-2), Rational.of(-3),
            Rational.ZERO, Rational.ZERO, Rational.of(4));
        LuDecomposition<Rational> luSwap = withSwap.lu(f);
        assertThat(luSwap.isNonsingular()).isTrue();
        assertThat(luSwap.swapSign()).isEqualTo(-1);
        assertThat(luSwap.determinant()).isEqualTo(Rational.of(-8));
        Vector<Rational> rhs = Vector.of(f, Rational.of(5), Rational.of(-4), Rational.of(8));
        Vector<Rational> sol = luSwap.solve(rhs);
        assertThat(withSwap.multiply(sol)).isEqualTo(rhs);

        // Singular matrix (zero diagonal on U)
        SquareMatrix<Rational> singular = SquareMatrix.of(2, f,
            Rational.ONE, Rational.TWO,
            Rational.TWO, Rational.of(4));
        LuDecomposition<Rational> luSingular = singular.lu(f);
        assertThat(luSingular.isNonsingular()).isFalse();
        assertThat(luSingular.determinant()).isEqualTo(Rational.ZERO);
        assertThatThrownBy(() -> luSingular.solve(Vector.of(f, Rational.ONE, Rational.TWO)))
            .isInstanceOf(NonInvertibleElementException.class);

        // Singular matrix with zero first column
        SquareMatrix<Rational> zeroCol = SquareMatrix.of(2, f,
            Rational.ZERO, Rational.ONE,
            Rational.ZERO, Rational.TWO);
        LuDecomposition<Rational> luZeroCol = zeroCol.lu(f);
        assertThat(luZeroCol.isNonsingular()).isFalse();
        assertThat(luZeroCol.determinant()).isEqualTo(Rational.ZERO);

        // Validation guards on lu() and solve()
        assertThatThrownBy(() -> noSwap.lu(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> luNoSwap.solve(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> luNoSwap.solve(Vector.of(f, Rational.ONE, Rational.TWO, Rational.ZERO)))
            .isInstanceOf(DimensionMismatchException.class);

        QuotientField<BigInteger> f5 = ZMod.field(5);
        QuotientField<BigInteger> f7 = ZMod.field(7);
        SquareMatrix<Residue<BigInteger>> sq5 = SquareMatrix.identity(2, f5);
        LuDecomposition<Residue<BigInteger>> lu5 = sq5.lu(f5);
        Vector<Residue<BigInteger>> vec7 = Vector.zeros(2, f7);

        assertThatThrownBy(() -> sq5.lu(f7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> lu5.solve(vec7)).isInstanceOf(IncompatibleAlgebraException.class);
    }
}
