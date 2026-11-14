package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import lock14.algebra.structure.Module;
import lock14.algebra.structure.Ring;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class ModuleLaws<M, R> {

    protected abstract Module<M, R> module();

    @Property
    public void vectorAdditiveAssociativity(@ForAll("vectors") M u, @ForAll("vectors") M v, @ForAll("vectors") M w) {
        Module<M, R> m = module();
        assertThat(m.add(m.add(u, v), w)).isEqualTo(m.add(u, m.add(v, w)));
    }

    @Property
    public void vectorAdditiveIdentity(@ForAll("vectors") M u) {
        Module<M, R> m = module();
        assertThat(m.add(u, m.zero())).isEqualTo(u);
        assertThat(m.add(m.zero(), u)).isEqualTo(u);
    }

    @Property
    public void vectorAdditiveInverse(@ForAll("vectors") M u) {
        Module<M, R> m = module();
        assertThat(m.add(u, m.negate(u))).isEqualTo(m.zero());
        assertThat(m.add(m.negate(u), u)).isEqualTo(m.zero());
    }

    @Property
    public void vectorAdditiveCommutativity(@ForAll("vectors") M u, @ForAll("vectors") M v) {
        Module<M, R> m = module();
        assertThat(m.add(u, v)).isEqualTo(m.add(v, u));
    }

    @Property
    public void scalarDistributesOverVectorAddition(@ForAll("scalars") R r, @ForAll("vectors") M u, @ForAll("vectors") M v) {
        Module<M, R> m = module();
        assertThat(m.scale(r, m.add(u, v))).isEqualTo(m.add(m.scale(r, u), m.scale(r, v)));
    }

    @Property
    public void scalarAdditionDistributesOverVector(@ForAll("scalars") R r, @ForAll("scalars") R s, @ForAll("vectors") M u) {
        Module<M, R> m = module();
        Ring<R> ring = m.scalarRing();
        assertThat(m.scale(ring.add(r, s), u)).isEqualTo(m.add(m.scale(r, u), m.scale(s, u)));
    }

    @Property
    public void scalarMultiplicationCompatibility(@ForAll("scalars") R r, @ForAll("scalars") R s, @ForAll("vectors") M u) {
        Module<M, R> m = module();
        Ring<R> ring = m.scalarRing();
        assertThat(m.scale(ring.multiply(r, s), u)).isEqualTo(m.scale(r, m.scale(s, u)));
    }

    @Property
    public void scalarIdentityAndZeroAnnihilation(@ForAll("vectors") M u) {
        Module<M, R> m = module();
        Ring<R> ring = m.scalarRing();
        assertThat(m.scale(ring.one(), u)).isEqualTo(u);
        assertThat(m.scale(ring.zero(), u)).isEqualTo(m.zero());
    }
}
