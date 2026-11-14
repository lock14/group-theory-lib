package lock14.algebra.laws;

import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.structure.DivisionRing;
import lock14.algebra.structure.Group;
import lock14.algebra.structure.Ring;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class DivisionRingLaws<T> extends RingLaws<T> {

    protected abstract DivisionRing<T> divisionRing();

    @Override
    protected Ring<T> ring() {
        return divisionRing();
    }

    @Property
    public void nonZeroTwoSidedMultiplicativeInverse(@ForAll("nonZeroElements") T a) {
        DivisionRing<T> d = divisionRing();
        assertThat(d.isUnit(a)).isTrue();
        assertThat(d.isUnit(d.zero())).isFalse();
        assertThat(d.tryReciprocal(a)).contains(d.reciprocal(a));
        assertThat(d.multiply(a, d.reciprocal(a))).isEqualTo(d.one());
        assertThat(d.multiply(d.reciprocal(a), a)).isEqualTo(d.one());
    }

    @Property
    public void rightAndLeftDivisionIdentities(@ForAll("elements") T a, @ForAll("nonZeroElements") T b) {
        DivisionRing<T> d = divisionRing();
        T rightQuot = d.divide(a, b);
        T leftQuot = d.divideLeft(a, b);
        assertThat(d.multiply(rightQuot, b)).isEqualTo(a);
        assertThat(d.multiply(b, leftQuot)).isEqualTo(a);
    }

    @Property
    public void multiplicativeGroupOfUnitsView(@ForAll("nonZeroElements") T a, @ForAll("nonZeroElements") T b) {
        DivisionRing<T> d = divisionRing();
        Group<T> units = d.asMultiplicativeGroupOfUnits();
        assertThat(units.identity()).isEqualTo(d.one());
        assertThat(units.combine(a, units.inverse(a))).isEqualTo(d.one());
        assertThat(units.combine(units.inverse(a), a)).isEqualTo(d.one());
        assertThat(units.combine(a, b)).isEqualTo(d.multiply(a, b));
    }

    @Test
    public void zeroReciprocalAndDivisionByZeroThrow() {
        DivisionRing<T> d = divisionRing();
        assertThat(d.tryReciprocal(d.zero())).isEmpty();
        assertThatThrownBy(() -> d.reciprocal(d.zero()))
            .isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> d.divide(d.one(), d.zero()))
            .isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> d.divideLeft(d.one(), d.zero()))
            .isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> d.asMultiplicativeGroupOfUnits().combine(d.one(), d.zero()))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
