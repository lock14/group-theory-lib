package lock14.algebra.structure;

import java.math.BigInteger;
import lock14.algebra.laws.CommutativeRingLaws;
import lock14.algebra.numbers.Integers;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import lock14.algebra.quotient.Residue;
import lock14.algebra.quotient.ZMod;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class DirectProductRingTest extends CommutativeRingLaws<Pair<BigInteger, Rational>> {

    private final CommutativeRing<Pair<BigInteger, Rational>> product =
        DirectProduct.commutativeRing(Integers.RING, Rationals.FIELD);

    @Override
    protected CommutativeRing<Pair<BigInteger, Rational>> commutativeRing() {
        return product;
    }

    @Provide
    Arbitrary<Pair<BigInteger, Rational>> elements() {
        Arbitrary<BigInteger> ints = Arbitraries.bigIntegers().between(BigInteger.valueOf(-50), BigInteger.valueOf(50));
        Arbitrary<Rational> rats = Arbitraries.integers().between(-25, 25).map(Rational::of);
        return ints.flatMap(i -> rats.map(r -> new Pair<>(i, r)));
    }

    @Test
    public void testCharacteristicRingHomomorphismAndIdempotents() {
        // Product rings naturally possess orthogonal idempotents e1 = (1, 0) and e2 = (0, 1)
        Pair<BigInteger, Rational> e1 = new Pair<>(BigInteger.ONE, Rational.ZERO);
        Pair<BigInteger, Rational> e2 = new Pair<>(BigInteger.ZERO, Rational.ONE);
        assertThat(product.multiply(e1, e1)).isEqualTo(e1);
        assertThat(product.multiply(e2, e2)).isEqualTo(e2);
        assertThat(product.multiply(e1, e2)).isEqualTo(product.zero());
        assertThat(product.add(e1, e2)).isEqualTo(product.one());

        var z6 = ZMod.ring(6);
        RingHomomorphism<Long, Residue<BigInteger>> charMap = RingHomomorphism.fromIntegers(z6);
        assertThat(charMap.apply(0L)).isEqualTo(z6.zero());
        assertThat(charMap.apply(1L)).isEqualTo(z6.one());
        assertThat(charMap.apply(6L)).isEqualTo(z6.zero());
        assertThat(charMap.apply(-1L)).isEqualTo(ZMod.element(z6, 5));

        Ring<Pair<BigInteger, Rational>> nonCommView = DirectProduct.ring(Integers.RING, Rationals.FIELD);
        assertThat(nonCommView.add(e1, e2)).isEqualTo(nonCommView.one());
        assertThat(nonCommView.asAdditiveGroup().inverse(e1)).isEqualTo(nonCommView.negate(e1));
        assertThat(nonCommView.asMultiplicativeMonoid().combine(e1, e1)).isEqualTo(e1);

        var qUnits = Rationals.FIELD.asMultiplicativeGroupOfUnits();
        assertThat(qUnits.combine(Rational.TWO, qUnits.inverse(Rational.TWO))).isEqualTo(Rational.ONE);
        assertThat(Rational.of(6).divideLeft(Rational.TWO)).isEqualTo(Rational.of(3));

        var ex = new lock14.algebra.exceptions.AlgebraException("wrapped", new RuntimeException("cause"));
        assertThat(ex).hasMessage("wrapped").hasCauseInstanceOf(RuntimeException.class);
    }
}
