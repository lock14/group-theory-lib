package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.IntegralDomain;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class IntegralDomainLaws<T> extends CommutativeRingLaws<T> {

    protected abstract IntegralDomain<T> integralDomain();

    @Override
    protected CommutativeRing<T> commutativeRing() {
        return integralDomain();
    }

    @Property
    public void noZeroDivisors(@ForAll("nonZeroElements") T a, @ForAll("nonZeroElements") T b) {
        IntegralDomain<T> d = integralDomain();
        assertThat(d.isZero(d.multiply(a, b))).isFalse();
    }

    @Test
    public void unitInvariants() {
        IntegralDomain<T> d = integralDomain();
        assertThat(d.isUnit(d.one())).isTrue();
        assertThat(d.isUnit(d.negate(d.one()))).isTrue();
        assertThat(d.isUnit(d.zero())).isFalse();
    }
}
