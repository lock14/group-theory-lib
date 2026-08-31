package lock14.algebra.linear;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.RingLaws;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.RationalField;
import lock14.algebra.structure.Ring;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MatrixRingTest extends RingLaws<SquareMatrix<Rational>> {

    private final MatrixRing<Rational> matrixRing = new MatrixRing<>(2, RationalField.INSTANCE);

    @Override
    protected Ring<SquareMatrix<Rational>> ring() {
        return matrixRing;
    }

    @Provide
    Arbitrary<SquareMatrix<Rational>> elements() {
        Arbitrary<Rational> rationals = Arbitraries.integers().between(-10, 10).map(Rational::of);
        return rationals.array(Rational[].class).ofSize(4)
                .map(arr -> new SquareMatrix<>(2, RationalField.INSTANCE, arr));
    }

    @Test
    public void testDeterminantAndInverse() {
        // A = [1 2]
        //     [3 4]
        // det(A) = 1*4 - 2*3 = -2
        // A^-1 = -1/2 * [ 4 -2] = [-2   1 ]
        //               [-3  1]   [ 3/2 -1/2]
        Rational[][] arr = {
            {Rational.of(1), Rational.of(2)},
            {Rational.of(3), Rational.of(4)}
        };
        SquareMatrix<Rational> a = SquareMatrix.of(RationalField.INSTANCE, arr);
        assertThat(a.determinant()).isEqualTo(Rational.of(-2));

        SquareMatrix<Rational> inv = a.inverse();
        SquareMatrix<Rational> identity = SquareMatrix.identity(2, RationalField.INSTANCE);

        assertThat(a.multiply(inv)).isEqualTo(identity);
        assertThat(inv.multiply(a)).isEqualTo(identity);
    }

    @Test
    public void testSingularMatrixInverseThrows() {
        // [1 2]
        // [2 4]  -> det = 0
        Rational[][] arr = {
            {Rational.of(1), Rational.of(2)},
            {Rational.of(2), Rational.of(4)}
        };
        SquareMatrix<Rational> singular = SquareMatrix.of(RationalField.INSTANCE, arr);
        assertThat(singular.determinant()).isEqualTo(Rational.ZERO);

        assertThatThrownBy(singular::inverse)
                .isInstanceOf(NonInvertibleElementException.class);
    }
}
