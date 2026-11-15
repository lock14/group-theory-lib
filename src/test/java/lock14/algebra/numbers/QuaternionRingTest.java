package lock14.algebra.numbers;

import java.math.BigInteger;
import java.util.List;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.laws.RingLaws;
import lock14.algebra.quotient.QuotientField;
import lock14.algebra.quotient.Residue;
import lock14.algebra.quotient.ZMod;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.Group;
import lock14.algebra.structure.Ring;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import net.jqwik.api.Tuple;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QuaternionRingTest extends RingLaws<Quaternion<BigInteger>> {

    private final QuaternionRing<BigInteger> lipschitz = new QuaternionRing<>(Integers.RING);

    @Override
    protected Ring<Quaternion<BigInteger>> ring() {
        return lipschitz;
    }

    @Provide
    Arbitrary<Quaternion<BigInteger>> elements() {
        Arbitrary<BigInteger> coords = Arbitraries.bigIntegers()
            .between(BigInteger.valueOf(-20), BigInteger.valueOf(20));
        Arbitrary<Quaternion<BigInteger>> randomQuats = coords.list().ofSize(4)
            .map(c -> lipschitz.of(c.get(0), c.get(1), c.get(2), c.get(3)));
        return Arbitraries.frequencyOf(
            Tuple.of(1, Arbitraries.of(List.of(
                lipschitz.zero(),
                lipschitz.one(),
                lipschitz.negate(lipschitz.one()),
                lipschitz.i(),
                lipschitz.j(),
                lipschitz.k()
            ))),
            Tuple.of(5, randomQuats)
        );
    }

    @Test
    public void testBasisUnitsPredicatesArithmeticAndFormatting() {
        Quaternion<BigInteger> zero = lipschitz.zero();
        Quaternion<BigInteger> one = lipschitz.one();
        Quaternion<BigInteger> i = lipschitz.i();
        Quaternion<BigInteger> j = lipschitz.j();
        Quaternion<BigInteger> k = lipschitz.k();

        assertThat(lipschitz.scalarRing()).isEqualTo(Integers.RING);
        assertThat(Quaternion.scalar(Integers.RING, BigInteger.ONE)).isEqualTo(one);
        assertThat(Quaternion.scalar(Integers.RING, BigInteger.ZERO)).isEqualTo(zero);

        assertThat(lipschitz.isZero(zero)).isTrue();
        assertThat(lipschitz.isZero(one)).isFalse();
        assertThat(lipschitz.isZero(i)).isFalse();

        assertThat(lipschitz.isOne(one)).isTrue();
        assertThat(lipschitz.isOne(zero)).isFalse();
        assertThat(lipschitz.isOne(lipschitz.of(BigInteger.ONE, BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO)))
            .isFalse();
        assertThat(lipschitz.isOne(lipschitz.of(BigInteger.ONE, BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO)))
            .isFalse();
        assertThat(lipschitz.isOne(lipschitz.of(BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE)))
            .isFalse();

        assertThat(lipschitz.multiply(i, j)).isEqualTo(k);
        assertThat(lipschitz.multiply(j, k)).isEqualTo(i);
        assertThat(lipschitz.multiply(k, i)).isEqualTo(j);
        assertThat(lipschitz.subtract(i, j)).isEqualTo(lipschitz.add(i, lipschitz.negate(j)));

        Quaternion<BigInteger> sample = lipschitz.of(
            BigInteger.ONE, BigInteger.TWO, BigInteger.valueOf(3), BigInteger.valueOf(4)
        );
        assertThat(sample.scale(BigInteger.TWO)).isEqualTo(lipschitz.of(
            BigInteger.TWO, BigInteger.valueOf(4), BigInteger.valueOf(6), BigInteger.valueOf(8)
        ));
        assertThat(sample.toString()).isEqualTo("(1 + 2i + 3j + 4k)");

        QuaternionRing<BigInteger> lipschitzCopy = new QuaternionRing<>(Integers.RING);
        assertThat(lipschitz).isEqualTo(lipschitzCopy);
        assertThat(lipschitz).hasSameHashCodeAs(lipschitzCopy);
        assertThat(lipschitz.toString()).contains("QuaternionRing");
    }

    @Test
    public void testIncompatibleRingGuardsAndRationalDivisionUnits() {
        record AltIntegersRing() implements CommutativeRing<BigInteger> {
            @Override
            public BigInteger zero() {
                return BigInteger.ZERO;
            }

            @Override
            public BigInteger one() {
                return BigInteger.ONE;
            }

            @Override
            public BigInteger add(BigInteger a, BigInteger b) {
                return a.add(b);
            }

            @Override
            public BigInteger negate(BigInteger a) {
                return a.negate();
            }

            @Override
            public BigInteger multiply(BigInteger a, BigInteger b) {
                return a.multiply(b);
            }
        }

        Quaternion<BigInteger> foreignIntQuat = Quaternion.scalar(new AltIntegersRing(), BigInteger.ONE);
        assertThatThrownBy(() -> lipschitz.add(lipschitz.one(), foreignIntQuat))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> lipschitz.negate(foreignIntQuat))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> lipschitz.subtract(lipschitz.one(), foreignIntQuat))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> lipschitz.multiply(lipschitz.one(), foreignIntQuat))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> lipschitz.isZero(foreignIntQuat))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> lipschitz.isOne(foreignIntQuat))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> lipschitz.one().add(foreignIntQuat))
            .isInstanceOf(IncompatibleAlgebraException.class);

        QuotientField<BigInteger> f3 = ZMod.field(3);
        QuotientField<BigInteger> f7 = ZMod.field(7);
        QuaternionDivisionRing<Residue<BigInteger>> hF3 = new QuaternionDivisionRing<>(f3);
        Quaternion<Residue<BigInteger>> qF7 = Quaternion.scalar(f7, f7.one());
        assertThatThrownBy(() -> hF3.add(hF3.one(), qF7))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> hF3.one().tryReciprocal(f7))
            .isInstanceOf(IncompatibleAlgebraException.class);

        QuaternionDivisionRing<Rational> hq = QuaternionDivisionRing.overRationals();
        Quaternion<Rational> a = Quaternion.ofRational(1, 2, 0, 0);
        Quaternion<Rational> b = Quaternion.ofRational(0, 0, 1, 3);
        assertThat(hq.divide(a, b).multiply(b)).isEqualTo(a);
        assertThat(b.multiply(hq.divideLeft(a, b))).isEqualTo(a);
        assertThatThrownBy(() -> hq.zero().reciprocal(Rationals.FIELD))
            .isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> hq.divide(a, hq.zero()))
            .isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> hq.divideLeft(a, hq.zero()))
            .isInstanceOf(NonInvertibleElementException.class);

        Group<Quaternion<Rational>> units = hq.asMultiplicativeGroupOfUnits();
        assertThat(units.identity()).isEqualTo(hq.one());
        assertThat(units.combine(hq.i(), hq.j())).isEqualTo(hq.k());
        assertThat(units.inverse(hq.i())).isEqualTo(hq.i().negate());
        assertThatThrownBy(() -> units.combine(hq.zero(), hq.one()))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> units.combine(hq.one(), hq.zero()))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
