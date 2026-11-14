package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import lock14.algebra.structure.Ring;
import lock14.algebra.structure.Semiring;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class RingLaws<T> extends SemiringLaws<T> {

    protected abstract Ring<T> ring();

    @Override
    protected Semiring<T> semiring() {
        return ring();
    }

    @Property
    public void additiveInverse(@ForAll("elements") T a) {
        Ring<T> r = ring();
        assertThat(r.add(a, r.negate(a))).isEqualTo(r.zero());
        assertThat(r.add(r.negate(a), a)).isEqualTo(r.zero());
    }

    @Property
    public void subtractionConsistency(@ForAll("elements") T a, @ForAll("elements") T b) {
        Ring<T> r = ring();
        assertThat(r.subtract(a, b)).isEqualTo(r.add(a, r.negate(b)));
    }

    @Property
    public void signRules(@ForAll("elements") T a, @ForAll("elements") T b) {
        Ring<T> r = ring();
        T ab = r.multiply(a, b);
        T negAb = r.negate(ab);
        assertThat(r.multiply(r.negate(a), b)).isEqualTo(negAb);
        assertThat(r.multiply(a, r.negate(b))).isEqualTo(negAb);
        assertThat(r.multiply(r.negate(a), r.negate(b))).isEqualTo(ab);
    }
}
