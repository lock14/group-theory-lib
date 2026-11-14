package lock14.algebra.polynomial;

import java.util.Arrays;
import lock14.algebra.laws.CommutativeRingLaws;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
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
        assertThat(p.evaluate(Rational.of(2))).isEqualTo(Rational.of(2 + 6 + 20));
        assertThat(p.derivative()).isEqualTo(qx.of(Rational.of(3), Rational.of(10)));

        assertThat(qx.zero().degree()).isEqualTo(-1);
        assertThatThrownBy(() -> qx.of(Arrays.asList(Rational.ONE, null)))
            .isInstanceOf(NullPointerException.class);
    }
}
