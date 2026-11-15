package lock14.algebra.linear;

import java.math.BigInteger;
import java.util.List;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.RingLaws;
import lock14.algebra.numbers.Complex;
import lock14.algebra.numbers.GaussianIntegers;
import lock14.algebra.numbers.Integers;
import lock14.algebra.numbers.Quaternion;
import lock14.algebra.numbers.QuaternionDivisionRing;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import lock14.algebra.polynomial.Polynomial;
import lock14.algebra.polynomial.PolynomialRing;
import lock14.algebra.quotient.GaloisFields;
import lock14.algebra.quotient.QuotientRing;
import lock14.algebra.quotient.Residue;
import lock14.algebra.quotient.ZMod;
import lock14.algebra.structure.Ring;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MatrixRingTest extends RingLaws<SquareMatrix<Rational>> {

    private final MatrixRing<Rational> matrixRing = new MatrixRing<>(2, Rationals.FIELD);
    private final QuotientRing<BigInteger> z12 = ZMod.ring(12);

    @Override
    protected Ring<SquareMatrix<Rational>> ring() {
        return matrixRing;
    }

    @Provide
    Arbitrary<SquareMatrix<Rational>> elements() {
        Arbitrary<Rational> rationals = Arbitraries.integers().between(-8, 8).map(Rational::of);
        return Arbitraries.oneOf(
            Arbitraries.of(
                matrixRing.zero(),
                matrixRing.one(),
                matrixRing.of(Rational.ONE, Rational.TWO, Rational.TWO, Rational.of(4))
            ),
            rationals.array(Rational[].class).ofSize(4)
                .map(arr -> SquareMatrix.of(2, Rationals.FIELD, arr))
        );
    }

    @Provide
    Arbitrary<SquareMatrix<BigInteger>> intMatrices3x3() {
        Arbitrary<BigInteger> ints = Arbitraries.bigIntegers().between(BigInteger.valueOf(-8), BigInteger.valueOf(8));
        return ints.array(BigInteger[].class).ofSize(9)
            .map(arr -> SquareMatrix.of(3, Integers.RING, arr));
    }

    @Provide
    Arbitrary<SquareMatrix<Residue<BigInteger>>> mod12Matrices3x3() {
        Arbitrary<Residue<BigInteger>> mods = Arbitraries.longs().between(0, 11).map(v -> ZMod.element(z12, v));
        return mods.array(Residue[].class).ofSize(9)
            .map(arr -> {
                @SuppressWarnings("unchecked")
                Residue<BigInteger>[] typed = (Residue<BigInteger>[]) arr;
                return SquareMatrix.of(3, z12, typed);
            });
    }

    @Provide
    Arbitrary<SquareMatrix<Residue<BigInteger>>> mod12Matrices4x4() {
        Arbitrary<Residue<BigInteger>> mods = Arbitraries.longs().between(0, 11).map(v -> ZMod.element(z12, v));
        return mods.array(Residue[].class).ofSize(16)
            .map(arr -> {
                @SuppressWarnings("unchecked")
                Residue<BigInteger>[] typed = (Residue<BigInteger>[]) arr;
                return SquareMatrix.of(4, z12, typed);
            });
    }

    @Provide
    Arbitrary<SquareMatrix<Rational>> rationalMatrices3x3() {
        Arbitrary<Rational> r = Arbitraries.integers().between(-5, 5).map(Rational::of);
        return r.array(Rational[].class).ofSize(9)
            .map(arr -> SquareMatrix.of(3, Rationals.FIELD, arr));
    }

    @Property
    public void bareissDeterminantMultiplicativityOverIntegers(
            @ForAll("intMatrices3x3") SquareMatrix<BigInteger> a,
            @ForAll("intMatrices3x3") SquareMatrix<BigInteger> b) {
        assertThat(a.multiply(b).determinant(Integers.RING))
            .isEqualTo(a.determinant(Integers.RING).multiply(b.determinant(Integers.RING)));
    }

    @Property
    public void birdDivisionFreeDeterminantMultiplicativityOverZMod12(
            @ForAll("mod12Matrices3x3") SquareMatrix<Residue<BigInteger>> a,
            @ForAll("mod12Matrices3x3") SquareMatrix<Residue<BigInteger>> b) {
        assertThat(a.multiply(b).determinant(z12))
            .isEqualTo(a.determinant(z12).multiply(b.determinant(z12)));
    }

    @Property
    public void birdDivisionFreeDeterminantMultiplicativity4x4OverZMod12(
            @ForAll("mod12Matrices4x4") SquareMatrix<Residue<BigInteger>> a,
            @ForAll("mod12Matrices4x4") SquareMatrix<Residue<BigInteger>> b) {
        assertThat(a.multiply(b).determinant(z12))
            .isEqualTo(a.determinant(z12).multiply(b.determinant(z12)));
    }

    @Property
    public void luFactorizationIdentityOverRationals(
            @ForAll("rationalMatrices3x3") SquareMatrix<Rational> a) {
        LuDecomposition<Rational> lu = a.lu(Rationals.FIELD);
        SquareMatrix<Rational> pa = lu.permutationMatrix().multiply(a);
        SquareMatrix<Rational> luProd = lu.lower().multiply(lu.upper());
        assertThat(pa).isEqualTo(luProd);
        assertThat(lu.determinant()).isEqualTo(a.determinant(Rationals.FIELD));
    }

    @Test
    public void testMatrixRingConstructionPredicatesAndMembershipGuards() {
        assertThat(matrixRing.dimension()).isEqualTo(2);
        assertThat(matrixRing.entryRing()).isEqualTo(Rationals.FIELD);

        SquareMatrix<Rational> m = matrixRing.of(Rational.ONE, Rational.TWO, Rational.of(3), Rational.of(4));
        assertThat(m).isEqualTo(SquareMatrix.of(2, Rationals.FIELD,
            Rational.ONE, Rational.TWO, Rational.of(3), Rational.of(4)));
        assertThat(matrixRing.isZero(matrixRing.zero())).isTrue();
        assertThat(matrixRing.isZero(matrixRing.one())).isFalse();
        assertThat(matrixRing.isOne(matrixRing.one())).isTrue();
        assertThat(matrixRing.isOne(matrixRing.zero())).isFalse();

        assertThatThrownBy(() -> new MatrixRing<>(0, Rationals.FIELD))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MatrixRing<>(-2, Rationals.FIELD))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MatrixRing<>(2, null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> matrixRing.of(Rational.ONE, Rational.TWO))
            .isInstanceOf(DimensionMismatchException.class);

        assertThatThrownBy(() -> matrixRing.negate(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> matrixRing.isZero(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> matrixRing.isOne(null)).isInstanceOf(NullPointerException.class);

        SquareMatrix<Rational> m3 = SquareMatrix.identity(3, Rationals.FIELD);
        assertThatThrownBy(() -> matrixRing.add(m3, matrixRing.one()))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> matrixRing.add(matrixRing.one(), m3))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> matrixRing.subtract(m3, matrixRing.one()))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> matrixRing.multiply(m3, matrixRing.one()))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> matrixRing.negate(m3))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> matrixRing.isZero(m3))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> matrixRing.isOne(m3))
            .isInstanceOf(DimensionMismatchException.class);
    }

    @Test
    public void testDeterminantSmallSizesBareissSwapsAndEvenDimensionBird() {
        // 1x1 and 2x2 fast paths in MatrixOps.determinant
        SquareMatrix<BigInteger> m1 = SquareMatrix.of(1, Integers.RING, BigInteger.valueOf(-7));
        assertThat(m1.determinant(Integers.RING)).isEqualTo(BigInteger.valueOf(-7));

        SquareMatrix<BigInteger> m2 = SquareMatrix.of(2, Integers.RING,
            BigInteger.valueOf(3), BigInteger.valueOf(5),
            BigInteger.TWO, BigInteger.valueOf(7));
        assertThat(m2.determinant(Integers.RING)).isEqualTo(BigInteger.valueOf(11));

        // 4x4 Bareiss determinant requiring row swap (odd sign) and exact multi-step division
        SquareMatrix<BigInteger> bareiss4 = SquareMatrix.of(4, Integers.RING,
            BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO,
            BigInteger.TWO, BigInteger.valueOf(3), BigInteger.ZERO, BigInteger.ZERO,
            BigInteger.ZERO, BigInteger.ZERO, BigInteger.valueOf(4), BigInteger.ONE,
            BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(3));
        assertThat(bareiss4.determinant(Integers.RING)).isEqualTo(BigInteger.valueOf(-22));

        // 3x3 Bareiss singular matrix with all-zero column
        SquareMatrix<BigInteger> bareissSingular = SquareMatrix.of(3, Integers.RING,
            BigInteger.ZERO, BigInteger.ONE, BigInteger.TWO,
            BigInteger.ZERO, BigInteger.valueOf(3), BigInteger.valueOf(4),
            BigInteger.ZERO, BigInteger.valueOf(5), BigInteger.valueOf(6));
        assertThat(bareissSingular.determinant(Integers.RING)).isEqualTo(BigInteger.ZERO);

        // 4x4 Bird division-free determinant over non-domain Z/12Z (even n >= 4 sign branch)
        SquareMatrix<Residue<BigInteger>> bird4 = SquareMatrix.zeros(4, z12)
            .with(0, 1, ZMod.element(z12, 1))
            .with(0, 3, ZMod.element(z12, 2))
            .with(1, 0, ZMod.element(z12, 3))
            .with(1, 1, ZMod.element(z12, 1))
            .with(2, 2, ZMod.element(z12, 2))
            .with(2, 3, ZMod.element(z12, 5))
            .with(3, 2, ZMod.element(z12, 1))
            .with(3, 3, ZMod.element(z12, 4));
        assertThat(bird4.determinant(z12)).isEqualTo(ZMod.element(z12, 3));
    }

    @Test
    public void testLuSolveAndSquareSolveOverRationalsAndGF16() {
        // Requires row swap at pivot 0!
        SquareMatrix<Rational> a = SquareMatrix.of(3, Rationals.FIELD,
            Rational.ZERO, Rational.of(2), Rational.of(1),
            Rational.of(1), Rational.of(-2), Rational.of(-3),
            Rational.of(-1), Rational.of(1), Rational.of(2));
        Vector<Rational> b = Vector.of(Rationals.FIELD, Rational.of(-8), Rational.ZERO, Rational.of(3));

        Vector<Rational> xDirect = a.solve(Rationals.FIELD, b);
        Vector<Rational> xLu = a.lu(Rationals.FIELD).solve(b);
        assertThat(xDirect).isEqualTo(xLu);
        assertThat(a.multiply(xDirect)).isEqualTo(b);
        assertThat(a.tryInverse(Rationals.FIELD)).contains(a.inverse(Rationals.FIELD));

        // Solve over finite extension field GF(2^4)
        var gf16 = GaloisFields.extension(2, 1, 1, 0, 0, 1);
        var f2 = ZMod.field(2);
        var alpha = gf16.of(Polynomial.x(f2));
        var alphaPlus1 = alpha.add(gf16.one());
        SquareMatrix<Residue<Polynomial<Residue<BigInteger>>>> gfMat = SquareMatrix.zeros(2, gf16)
            .with(0, 0, alpha)
            .with(0, 1, gf16.one())
            .with(1, 0, gf16.one())
            .with(1, 1, alphaPlus1);
        Vector<Residue<Polynomial<Residue<BigInteger>>>> gfRhs = Vector.of(gf16, List.of(gf16.one(), alpha));
        var gfSol = gfMat.solve(gf16, gfRhs);
        assertThat(gfMat.multiply(gfSol)).isEqualTo(gfRhs);
    }

    @Test
    public void testExactNonCommutativeQuaternionSolveAndInverse() {
        var hq = QuaternionDivisionRing.overRationals();
        SquareMatrix<Quaternion<Rational>> qm = SquareMatrix.identity(2, hq)
            .with(0, 0, Quaternion.ofRational(1, 1, 0, 0))
            .with(0, 1, hq.j())
            .with(1, 0, hq.k())
            .with(1, 1, Quaternion.ofRational(2, 0, 1, -1));

        SquareMatrix<Quaternion<Rational>> inv = qm.inverse(hq);
        SquareMatrix<Quaternion<Rational>> id = SquareMatrix.identity(2, hq);
        assertThat(qm.multiply(inv)).isEqualTo(id);
        assertThat(inv.multiply(qm)).isEqualTo(id);

        Vector<Quaternion<Rational>> rhs = Vector.of(hq, List.of(hq.i(), hq.j().add(hq.k())));
        Vector<Quaternion<Rational>> x = qm.solve(hq, rhs);
        assertThat(qm.multiply(x)).isEqualTo(rhs);
    }

    @Test
    public void testBareissOverGaussianIntegersAndCharacteristicPolynomial() {
        var zi = GaussianIntegers.DOMAIN;
        SquareMatrix<Complex<BigInteger>> gm = SquareMatrix.zeros(3, zi)
            .with(0, 0, zi.of(2, 1))
            .with(0, 1, zi.of(1, 0))
            .with(0, 2, zi.of(0, -1))
            .with(1, 0, zi.of(0, 1))
            .with(1, 1, zi.of(3, -1))
            .with(1, 2, zi.of(1, 1))
            .with(2, 0, zi.of(1, -1))
            .with(2, 1, zi.of(0, 2))
            .with(2, 2, zi.of(2, 0));
        Complex<BigInteger> detGm = gm.determinant(zi);
        assertThat(gm.transpose().determinant(zi)).isEqualTo(detGm);

        PolynomialRing<Rational> qx = new PolynomialRing<>(Rationals.FIELD);
        Polynomial<Rational> x = qx.x();
        Polynomial<Rational> c1 = qx.constant(Rational.of(1));
        Polynomial<Rational> c2 = qx.constant(Rational.of(2));
        Polynomial<Rational> c3 = qx.constant(Rational.of(3));
        SquareMatrix<Polynomial<Rational>> charMat = SquareMatrix.zeros(3, qx)
            .with(0, 0, x.subtract(c1))
            .with(1, 1, x.subtract(c2))
            .with(2, 2, x.subtract(c3));
        assertThat(charMat.determinant(qx)).isEqualTo(
            x.subtract(c1).multiply(x.subtract(c2)).multiply(x.subtract(c3))
        );
    }

    @Test
    public void testSingularAndForeignRingRejections() {
        SquareMatrix<Rational> singular = SquareMatrix.of(2, Rationals.FIELD,
            Rational.of(1), Rational.of(2),
            Rational.of(2), Rational.of(4));
        assertThat(singular.tryInverse(Rationals.FIELD)).isEmpty();
        assertThatThrownBy(() -> singular.inverse(Rationals.FIELD)).isInstanceOf(NonInvertibleElementException.class);

        var f5 = ZMod.field(5);
        var f7 = ZMod.field(7);
        MatrixRing<Residue<BigInteger>> m2z5 = new MatrixRing<>(2, f5);
        SquareMatrix<Residue<BigInteger>> a7 = SquareMatrix.identity(2, f7);
        assertThatThrownBy(() -> m2z5.isOne(a7)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> a7.determinant(f5)).isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> a7.inverse(f5)).isInstanceOf(IncompatibleAlgebraException.class);
    }
}
