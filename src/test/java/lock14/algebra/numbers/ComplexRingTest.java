package lock14.algebra.numbers;

import java.math.BigInteger;
import lock14.algebra.laws.CommutativeRingLaws;
import lock14.algebra.quotient.QuotientRing;
import lock14.algebra.quotient.Residue;
import lock14.algebra.quotient.ZMod;
import lock14.algebra.structure.CommutativeRing;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ComplexRingTest extends CommutativeRingLaws<Complex<Residue<BigInteger>>> {

    private final QuotientRing<BigInteger> z6 = ZMod.ring(6);
    private final ComplexRing<Residue<BigInteger>> z6i = new ComplexRing<>(z6);

    @Override
    protected CommutativeRing<Complex<Residue<BigInteger>>> commutativeRing() {
        return z6i;
    }

    @Provide
    Arbitrary<Complex<Residue<BigInteger>>> elements() {
        Arbitrary<Residue<BigInteger>> r = Arbitraries.longs().between(0, 5).map(v -> ZMod.element(z6, v));
        return r.flatMap(re -> r.map(im -> z6i.of(re, im)));
    }

    @Test
    public void testImaginaryUnitSquaredIsMinusOne() {
        assertThat(z6i.multiply(z6i.i(), z6i.i())).isEqualTo(z6i.negate(z6i.one()));
        assertThat(z6i.isOne(z6i.one())).isTrue();
        assertThat(z6i.isZero(z6i.zero())).isTrue();
    }
}
