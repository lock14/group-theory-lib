package lock14.algebra.linear;

import java.math.BigInteger;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.discrete.Polynomial;
import lock14.algebra.discrete.PolynomialRing;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.RingLaws;
import lock14.algebra.numbers.BigIntRing;
import lock14.algebra.numbers.Complex;
import lock14.algebra.numbers.ComplexField;
import lock14.algebra.numbers.ModuloInteger;
import lock14.algebra.numbers.Quaternion;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.RationalField;
import lock14.algebra.numbers.RealField;
import lock14.algebra.numbers.ZModnRing;
import lock14.algebra.structure.Ring;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

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

    @Provide
    Arbitrary<SquareMatrix<BigInteger>> intMatrices3x3() {
        Arbitrary<BigInteger> ints = Arbitraries.bigIntegers().between(BigInteger.valueOf(-8), BigInteger.valueOf(8));
        return ints.array(BigInteger[].class).ofSize(9)
                .map(arr -> new SquareMatrix<>(3, BigIntRing.INSTANCE, arr));
    }

    @Provide
    Arbitrary<SquareMatrix<ModuloInteger>> mod12Matrices3x3() {
        ZModnRing z12 = new ZModnRing(12);
        Arbitrary<ModuloInteger> mods = Arbitraries.longs().between(0, 11).map(z12::element);
        return mods.array(ModuloInteger[].class).ofSize(9)
                .map(arr -> new SquareMatrix<>(3, z12, arr));
    }

    @Property
    public void bareissDeterminantMultiplicativityOverIntegers(
            @ForAll("intMatrices3x3") SquareMatrix<BigInteger> a,
            @ForAll("intMatrices3x3") SquareMatrix<BigInteger> b) {
        BigInteger detProduct = a.multiply(b).determinant();
        BigInteger productDets = a.determinant().multiply(b.determinant());
        assertThat(detProduct).isEqualTo(productDets);
    }

    @Property
    public void birdDivisionFreeDeterminantMultiplicativityOverZMod12(
            @ForAll("mod12Matrices3x3") SquareMatrix<ModuloInteger> a,
            @ForAll("mod12Matrices3x3") SquareMatrix<ModuloInteger> b) {
        ModuloInteger detProduct = a.multiply(b).determinant();
        ModuloInteger productDets = a.determinant().multiply(b.determinant());
        assertThat(detProduct).isEqualTo(productDets);
    }

    @Test
    public void testDeterminantAndInverse() {
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
    public void testPartialPivotingPreventsCatastrophicCancellation() {
        // Classic Wilkinson/Forsythe matrix:
        // [ 1e-20  1 ]
        // [ 1      1 ]
        // Without partial pivoting, Gauss-Jordan computes inv[0,0] = 0 instead of -1!
        Double[][] realArr = {
            {1e-20, 1.0},
            {1.0, 1.0}
        };
        SquareMatrix<Double> m = SquareMatrix.of(RealField.INSTANCE, realArr);
        assertThat(m.determinant()).isCloseTo(-1.0, within(1e-12));

        SquareMatrix<Double> inv = m.inverse();
        assertThat(inv.get(0, 0)).isCloseTo(-1.0, within(1e-12));
        assertThat(inv.get(0, 1)).isCloseTo(1.0, within(1e-12));
        assertThat(inv.get(1, 0)).isCloseTo(1.0, within(1e-12));
        assertThat(inv.get(1, 1)).isCloseTo(0.0, within(1e-12));

        // Same over ComplexField
        Complex[][] complexArr = {
            {Complex.of(0.0, 1e-20), Complex.ONE},
            {Complex.ONE, Complex.ONE}
        };
        SquareMatrix<Complex> cm = SquareMatrix.of(ComplexField.INSTANCE, complexArr);
        SquareMatrix<Complex> cInv = cm.inverse();
        SquareMatrix<Complex> prod = cm.multiply(cInv);
        assertThat(prod.get(0, 0).subtract(Complex.ONE).abs()).isLessThan(1e-12);
        assertThat(prod.get(0, 1).abs()).isLessThan(1e-12);
        assertThat(prod.get(1, 0).abs()).isLessThan(1e-12);
        assertThat(prod.get(1, 1).subtract(Complex.ONE).abs()).isLessThan(1e-12);
    }

    @Test
    public void testNonCommutativeDeterminantThrowsUnsupportedOperationException() {
        SquareMatrix<Quaternion> qm = SquareMatrix.of(2, Quaternion.ring(),
                Quaternion.I, Quaternion.J,
                Quaternion.K, Quaternion.ONE);
        assertThatThrownBy(qm::determinant)
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void testLargeCommutativeRingDeterminantRunsInPolynomialTime() {
        // 12x12 upper-bidiagonal + perturbation matrix over Z (would require 12! = 479,001,600 calls in Laplace)
        int n = 12;
        BigInteger[][] data = new BigInteger[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    data[i][j] = BigInteger.valueOf(2);
                } else if (Math.abs(i - j) == 1) {
                    data[i][j] = BigInteger.ONE;
                } else {
                    data[i][j] = BigInteger.ZERO;
                }
            }
        }
        // Tridiagonal Toeplitz T_n(1, 2, 1) has exact determinant n + 1 = 13!
        SquareMatrix<BigInteger> tridiag = SquareMatrix.of(BigIntRing.INSTANCE, data);
        assertThat(tridiag.determinant()).isEqualTo(BigInteger.valueOf(n + 1));

        // Characteristic polynomial det(xI - A) for 3x3 matrix over Q[x] via Bird's division-free algorithm
        PolynomialRing<Rational> qx = new PolynomialRing<>(RationalField.INSTANCE);
        Polynomial<Rational> x = Polynomial.x(RationalField.INSTANCE);
        Polynomial<Rational> c1 = Polynomial.constant(RationalField.INSTANCE, Rational.of(1));
        Polynomial<Rational> c2 = Polynomial.constant(RationalField.INSTANCE, Rational.of(2));
        Polynomial<Rational> c3 = Polynomial.constant(RationalField.INSTANCE, Rational.of(3));
        Polynomial<Rational> zero = qx.zero();

        // Diagonal matrix diag(x - 1, x - 2, x - 3) has det = (x - 1)(x - 2)(x - 3)
        SquareMatrix<Polynomial<Rational>> charMat = SquareMatrix.of(3, qx,
                x.subtract(c1), zero, zero,
                zero, x.subtract(c2), zero,
                zero, zero, x.subtract(c3));
        Polynomial<Rational> expected = x.subtract(c1).multiply(x.subtract(c2)).multiply(x.subtract(c3));
        assertThat(charMat.determinant()).isEqualTo(expected);
    }

    @Test
    public void testSingularMatrixInverseThrows() {
        Rational[][] arr = {
            {Rational.of(1), Rational.of(2)},
            {Rational.of(2), Rational.of(4)}
        };
        SquareMatrix<Rational> singular = SquareMatrix.of(RationalField.INSTANCE, arr);
        assertThat(singular.determinant()).isEqualTo(Rational.ZERO);

        assertThatThrownBy(singular::inverse)
                .isInstanceOf(NonInvertibleElementException.class);
    }

    @Test
    public void testForeignEntryRingMatrixRejected() {
        MatrixRing<ModuloInteger> m2z5 = new MatrixRing<>(2, new ZModnRing(5));
        ZModnRing z7 = new ZModnRing(7);
        SquareMatrix<ModuloInteger> a7 = SquareMatrix.identity(2, z7);
        SquareMatrix<ModuloInteger> b7 = SquareMatrix.zeros(2, z7);

        assertThatThrownBy(() -> m2z5.add(a7, b7))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> m2z5.multiply(a7, b7))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> m2z5.isOne(a7))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
