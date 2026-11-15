package lock14.algebra.polynomial;

import java.math.BigInteger;
import java.util.Arrays;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.laws.CommutativeRingLaws;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import lock14.algebra.quotient.QuotientRing;
import lock14.algebra.quotient.Residue;
import lock14.algebra.quotient.ZMod;
import lock14.algebra.structure.CommutativeRing;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PolynomialTest extends CommutativeRingLaws<Polynomial<Rational>> {

    private final PolynomialRing<Rational> qx = new PolynomialRing<>(Rationals.FIELD);

    @Override
    protected CommutativeRing<Polynomial<Rational>> commutativeRing() {
        return qx;
    }

    @Provide
    Arbitrary<Polynomial<Rational>> elements() {
        Arbitrary<Rational> coeffs = Arbitraries.integers().between(-15, 15).map(Rational::of);
        return coeffs.list().ofMinSize(1).ofMaxSize(5).map(qx::of);
    }

    @Test
    public void testCanonicalTrimmingEvaluationAndDerivative() {
        // 2 + 3x + 5x^2 + 0x^3 -> degree 2
        Polynomial<Rational> p = qx.of(Rational.of(2), Rational.of(3), Rational.of(5), Rational.ZERO);
        assertThat(p.degree()).isEqualTo(2);
        assertThat(p.coefficients()).containsExactly(Rational.of(2), Rational.of(3), Rational.of(5));
        assertThat(p.ring()).isEqualTo(Rationals.FIELD);
        assertThat(p.coefficient(-1)).isEqualTo(Rational.ZERO);
        assertThat(p.coefficient(99)).isEqualTo(Rational.ZERO);
        assertThat(p.isOne()).isFalse();
        assertThat(p.evaluate(Rational.of(2))).isEqualTo(Rational.of(2 + 6 + 20));
        assertThat(p.derivative()).isEqualTo(qx.of(Rational.of(3), Rational.of(10)));

        assertThat(qx.zero().degree()).isEqualTo(-1);
        assertThat(qx.zero().evaluate(Rational.of(5))).isEqualTo(Rational.ZERO);
        assertThat(qx.zero().scale(Rational.TWO)).isEqualTo(qx.zero());
        assertThat(p.scale(Rational.ZERO)).isEqualTo(qx.zero());
        assertThat(qx.constant(Rational.of(7)).derivative()).isEqualTo(qx.zero());
        assertThat(qx.zero().derivative()).isEqualTo(qx.zero());

        assertThatThrownBy(() -> qx.of(Arrays.asList(Rational.ONE, null)))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testMonomialZeroRingFormattingAndEquality() {
        assertThatThrownBy(() -> Polynomial.monomial(Rationals.FIELD, -1, Rational.ONE))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(Polynomial.monomial(Rationals.FIELD, 3, Rational.ZERO)).isEqualTo(qx.zero());
        assertThat(Polynomial.monomial(Rationals.FIELD, 2, Rational.of(5)))
            .isEqualTo(qx.of(Rational.ZERO, Rational.ZERO, Rational.of(5)));

        QuotientRing<BigInteger> z1 = ZMod.ring(1);
        assertThat(Polynomial.one(z1)).isEqualTo(Polynomial.zero(z1));
        assertThat(Polynomial.x(z1)).isEqualTo(Polynomial.zero(z1));

        QuotientRing<BigInteger> z6 = ZMod.ring(6);
        QuotientRing<BigInteger> z7 = ZMod.ring(7);
        PolynomialRing<Residue<BigInteger>> z6x = new PolynomialRing<>(z6);
        Polynomial<Residue<BigInteger>> z7Poly = Polynomial.x(z7);
        assertThatThrownBy(() -> z6x.x().add(z7Poly))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> z6x.add(z6x.x(), z7Poly))
            .isInstanceOf(IncompatibleAlgebraException.class);

        assertThat(qx.zero().toString()).isEqualTo("0");
        assertThat(qx.of(Rational.of(2), Rational.ZERO, Rational.of(5)).toString()).isEqualTo("2 + 5x^2");
        assertThat(qx.of(Rational.ONE, Rational.of(3), Rational.of(4)).toString()).isEqualTo("1 + 3x + 4x^2");

        Polynomial<Rational> p1 = qx.of(Rational.ONE, Rational.TWO);
        Polynomial<Rational> p2 = qx.of(Rational.ONE, Rational.TWO, Rational.ZERO);
        assertThat(p1.equals(p1)).isTrue();
        assertThat(p1).isEqualTo(p2);
        assertThat(p1).hasSameHashCodeAs(p2);
        assertThat(p1).isNotEqualTo(qx.x());
        assertThat(z6x.x().equals(z7Poly)).isFalse();
        assertThat(p1.equals("not a polynomial")).isFalse();
        assertThat(p1.equals(null)).isFalse();
    }
}
