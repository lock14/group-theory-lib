package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;
import lock14.algebra.structure.DivisionRing;
import lock14.algebra.structure.Group;
import lock14.algebra.structure.Ring;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class DivisionRingLaws<T> extends RingLaws<T> {

    protected abstract DivisionRing<T> divisionRing();

    @Override
    protected Ring<T> ring() {
        return divisionRing();
    }

    /**
     * Asserts equality for multiplicative inverse and division identities.
     * Exact division rings use exact {@code equals}; IEEE-754 floating-point division rings
     * may override with bounded norm tolerance.
     */
    protected void assertDivisionEquals(T actual, T expected) {
        assertThat(actual).isEqualTo(expected);
    }

    @Property
    public void nonZeroTwoSidedMultiplicativeInverse(@ForAll("nonZeroElements") T a) {
        DivisionRing<T> d = divisionRing();
        assertThat(d.isUnit(a)).isTrue();
        assertThat(d.isUnit(d.zero())).isFalse();
        assertDivisionEquals(d.multiply(a, d.reciprocal(a)), d.one());
        assertDivisionEquals(d.multiply(d.reciprocal(a), a), d.one());
    }

    @Property
    public void rightAndLeftDivisionIdentities(@ForAll("elements") T a, @ForAll("nonZeroElements") T b) {
        DivisionRing<T> d = divisionRing();
        T rightQuot = d.divide(a, b);
        T leftQuot = d.divideLeft(a, b);
        assertDivisionEquals(d.multiply(rightQuot, b), a);
        assertDivisionEquals(d.multiply(b, leftQuot), a);
    }

    @Property
    public void multiplicativeGroupOfUnitsView(@ForAll("nonZeroElements") T a, @ForAll("nonZeroElements") T b) {
        DivisionRing<T> d = divisionRing();
        Group<T> units = d.asMultiplicativeGroupOfUnits();
        assertThat(units.identity()).isEqualTo(d.one());
        assertDivisionEquals(units.combine(a, units.inverse(a)), d.one());
        assertDivisionEquals(units.combine(units.inverse(a), a), d.one());
        assertDivisionEquals(units.combine(a, b), d.multiply(a, b));
    }

    @Test
    public void zeroReciprocalAndDivisionByZeroThrow() {
        DivisionRing<T> d = divisionRing();
        assertThatThrownBy(() -> d.reciprocal(d.zero()))
                .isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> d.divide(d.one(), d.zero()))
                .isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> d.divideLeft(d.one(), d.zero()))
                .isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> d.asMultiplicativeGroupOfUnits().combine(d.one(), d.zero()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
