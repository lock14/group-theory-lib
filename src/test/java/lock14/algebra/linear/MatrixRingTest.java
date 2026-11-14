package lock14.algebra.linear;

import java.math.BigInteger;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.exceptions.UndefinedOperationException;
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

    @Override
    protected Ring<SquareMatrix<Rational>> ring() {
        return matrixRing;
    }

    @Provide
    Arbitrary<SquareMatrix<Rational>> elements() {
        Arbitrary<Rational> rationals = Arbitraries.integers().between(-8, 8).map(Rational::of);
        return rationals.array(Rational[].class).ofSize(4)
            .map(arr -> SquareMatrix.of(2, Rationals.FIELD, arr));
    }

    @Provide
    Arbitrary<SquareMatrix<BigInteger>> intMatrices3x3() {
        Arbitrary<BigInteger> ints = Arbitraries.bigIntegers().between(BigInteger.valueOf(-8), BigInteger.valueOf(8));
        return ints.array(BigInteger[].class).ofSize(9)
            .map(arr -> SquareMatrix.of(3, Integers.RING, arr));
    }

    @Provide
    Arbitrary<SquareMatrix<Residue<BigInteger>>> mod12Matrices3x3() {
        var z12 = ZMod.ring(12);
        Arbitrary<Residue<BigInteger>> mods = Arbitraries.longs().between(0, 11).map(v -> ZMod.element(z12, v));
        return mods.array(Residue[].class).ofSize(9)
            .map(arr -> {
                @SuppressWarnings("unchecked")
                Residue<BigInteger>[] typed = (Residue<BigInteger>[]) arr;
                return SquareMatrix.of(3, z12, typed);
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
        assertThat(a.multiply(b).determinant()).isEqualTo(a.determinant().multiply(b.determinant()));
    }

    @Property
    public void birdDivisionFreeDeterminantMultiplicativityOverZMod12(
            @ForAll("mod12Matrices3x3") SquareMatrix<Residue<BigInteger>> a,
            @ForAll("mod12Matrices3x3") SquareMatrix<Residue<BigInteger>> b) {
        assertThat(a.multiply(b).determinant()).isEqualTo(a.determinant().multiply(b.determinant()));
    }

    @Property
    public void luFactorizationIdentityOverRationals(
            @ForAll("rationalMatrices3x3") SquareMatrix<Rational> a) {
        LuDecomposition<Rational> lu = a.lu();
        SquareMatrix<Rational> pa = lu.permutationMatrix().multiply(a);
        SquareMatrix<Rational> luProd = lu.lower().multiply(lu.upper());
        assertThat(pa).isEqualTo(luProd);
        assertThat(lu.determinant()).isEqualTo(a.determinant());
    }

    @Test
    public void testLuSolveAndSquareSolveOverRationalsAndGF16() {
        // Requires row swap at pivot 0!
        SquareMatrix<Rational> a = SquareMatrix.of(3, Rationals.FIELD,
            Rational.ZERO, Rational.of(2), Rational.of(1),
            Rational.of(1), Rational.of(-2), Rational.of(-3),
            Rational.of(-1), Rational.of(1), Rational.of(2));
        Vector<Rational> b = Vector.of(Rationals.FIELD, Rational.of(-8), Rational.ZERO, Rational.of(3));

        Vector<Rational> xDirect = a.solve(b);
        Vector<Rational> xLu = a.lu().solve(b);
        assertThat(xDirect).isEqualTo(xLu);
        assertThat(a.multiply(xDirect)).isEqualTo(b);
        assertThat(a.tryInverse()).contains(a.inverse());

        // Solve over finite extension field GF(2^4)
        var gf16 = GaloisFields.extension(2, 1, 1, 0, 0, 1);
        var f2 = ZMod.field(2);
        var alpha = gf16.of(Polynomial.x(f2));
        var alphaPlus1 = alpha.add(gf16.one());
        SquareMatrix<Residue<Polynomial<Residue<BigInteger>>>> gfMat = SquareMatrix.of(2, gf16,
            alpha, gf16.one(),
            gf16.one(), alphaPlus1);
        Vector<Residue<Polynomial<Residue<BigInteger>>>> gfRhs = Vector.of(gf16, gf16.one(), alpha);
        var gfSol = gfMat.solve(gfRhs);
        assertThat(gfMat.multiply(gfSol)).isEqualTo(gfRhs);
    }

    @Test
    public void testExactNonCommutativeQuaternionSolveAndInverse() {
        var hq = QuaternionDivisionRing.overRationals();
        SquareMatrix<Quaternion<Rational>> qm = SquareMatrix.of(2, hq,
            Quaternion.ofRational(1, 1, 0, 0), hq.j(),
            hq.k(), Quaternion.ofRational(2, 0, 1, -1));

        SquareMatrix<Quaternion<Rational>> inv = qm.inverse();
        SquareMatrix<Quaternion<Rational>> id = SquareMatrix.identity(2, hq);
        assertThat(qm.multiply(inv)).isEqualTo(id);
        assertThat(inv.multiply(qm)).isEqualTo(id);

        Vector<Quaternion<Rational>> rhs = Vector.of(hq, hq.i(), hq.j().add(hq.k()));
        Vector<Quaternion<Rational>> x = qm.solve(rhs);
        assertThat(qm.multiply(x)).isEqualTo(rhs);

        assertThatThrownBy(qm::determinant).isInstanceOf(UndefinedOperationException.class);
    }

    @Test
    public void testBareissOverGaussianIntegersAndCharacteristicPolynomial() {
        var zi = GaussianIntegers.DOMAIN;
        SquareMatrix<Complex<BigInteger>> gm = SquareMatrix.of(3, zi,
            zi.of(2, 1), zi.of(1, 0), zi.of(0, -1),
            zi.of(0, 1), zi.of(3, -1), zi.of(1, 1),
            zi.of(1, -1), zi.of(0, 2), zi.of(2, 0));
        Complex<BigInteger> detGm = gm.determinant();
        assertThat(gm.transpose().determinant()).isEqualTo(detGm);

        PolynomialRing<Rational> qx = new PolynomialRing<>(Rationals.FIELD);
        Polynomial<Rational> x = qx.x();
        Polynomial<Rational> c1 = qx.constant(Rational.of(1));
        Polynomial<Rational> c2 = qx.constant(Rational.of(2));
        Polynomial<Rational> c3 = qx.constant(Rational.of(3));
        Polynomial<Rational> zero = qx.zero();
        SquareMatrix<Polynomial<Rational>> charMat = SquareMatrix.of(3, qx,
            x.subtract(c1), zero, zero,
            zero, x.subtract(c2), zero,
            zero, zero, x.subtract(c3));
        assertThat(charMat.determinant()).isEqualTo(
            x.subtract(c1).multiply(x.subtract(c2)).multiply(x.subtract(c3))
        );
    }

    @Test
    public void testSingularAndForeignRingRejections() {
        SquareMatrix<Rational> singular = SquareMatrix.of(2, Rationals.FIELD,
            Rational.of(1), Rational.of(2),
            Rational.of(2), Rational.of(4));
        assertThat(singular.tryInverse()).isEmpty();
        assertThatThrownBy(singular::inverse).isInstanceOf(NonInvertibleElementException.class);

        MatrixRing<Residue<BigInteger>> m2z5 = new MatrixRing<>(2, ZMod.ring(5));
        SquareMatrix<Residue<BigInteger>> a7 = SquareMatrix.identity(2, ZMod.ring(7));
        assertThatThrownBy(() -> m2z5.isOne(a7)).isInstanceOf(IncompatibleAlgebraException.class);
    }
}
