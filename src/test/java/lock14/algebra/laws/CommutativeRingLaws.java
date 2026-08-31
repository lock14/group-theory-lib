package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.Ring;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class CommutativeRingLaws<T> extends RingLaws<T> {

    protected abstract CommutativeRing<T> commutativeRing();

    @Override
    protected Ring<T> ring() {
        return commutativeRing();
    }

    @Property
    public void multiplicativeCommutativity(@ForAll("elements") T a, @ForAll("elements") T b) {
        CommutativeRing<T> r = commutativeRing();
        assertThat(r.multiply(a, b)).isEqualTo(r.multiply(b, a));
    }
}
