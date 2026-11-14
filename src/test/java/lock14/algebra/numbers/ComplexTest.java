package lock14.algebra.numbers;

import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.FieldLaws;
import lock14.algebra.quotient.ZMod;
import lock14.algebra.structure.Field;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ComplexTest extends FieldLaws<Complex<Rational>> {

    private final ComplexField<Rational> gaussianRationals = ComplexField.overRationals();

    @Override
    protected Field<Complex<Rational>> field() {
        return gaussianRationals;
    }

    @Provide
    Arbitrary<Complex<Rational>> elements() {
        Arbitrary<Rational> rats = Arbitraries.integers().between(-30, 30)
            .flatMap(n -> Arbitraries.integers().between(1, 15).map(d -> Rational.of(n, d)));
        return rats.flatMap(re -> rats.map(im -> gaussianRationals.of(re, im)));
    }

    @Provide
    Arbitrary<Complex<Rational>> nonZeroElements() {
        return elements().filter(c -> !c.isZero());
    }

    @Test
    public void testExactGaussianRationalDivisionAndGF9Construction() {
        Complex<Rational> a = Complex.ofRational(2, 3);
        Complex<Rational> b = Complex.ofRational(4, 5);
        assertThat(a.multiply(b)).isEqualTo(Complex.ofRational(-7, 22));

        Complex<Rational> quot = gaussianRationals.divide(a, b);
        assertThat(quot).isEqualTo(Complex.ofRational(Rational.of(23, 41), Rational.of(2, 41)));
        assertThat(quot.multiply(b)).isEqualTo(a);
        assertThatThrownBy(() -> gaussianRationals.reciprocal(gaussianRationals.zero()))
            .isInstanceOf(NonInvertibleElementException.class);

        // Over F_3 (since 3 = 3 mod 4, -1 is a quadratic non-residue), ComplexField(F_3) is GF(9)!
        var f3 = ZMod.field(3);
        var gf9 = new ComplexField<>(f3);
        var z = gf9.of(ZMod.element(f3, 1), ZMod.element(f3, 2));
        assertThat(gf9.multiply(z, gf9.reciprocal(z))).isEqualTo(gf9.one());

        var f5 = ZMod.field(5);
        var c5 = Complex.of(f5, f5.one(), f5.one());
        assertThatThrownBy(() -> z.add(c5)).isInstanceOf(IncompatibleAlgebraException.class);
    }
}
