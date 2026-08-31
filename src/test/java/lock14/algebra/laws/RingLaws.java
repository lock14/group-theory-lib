package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import lock14.algebra.structure.Ring;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class RingLaws<T> {

    protected abstract Ring<T> ring();

    @Property
    public void additiveAssociativity(@ForAll("elements") T a, @ForAll("elements") T b, @ForAll("elements") T c) {
        Ring<T> r = ring();
        assertThat(r.add(r.add(a, b), c)).isEqualTo(r.add(a, r.add(b, c)));
    }

    @Property
    public void additiveIdentity(@ForAll("elements") T a) {
        Ring<T> r = ring();
        assertThat(r.add(a, r.zero())).isEqualTo(a);
        assertThat(r.add(r.zero(), a)).isEqualTo(a);
    }

    @Property
    public void additiveInverse(@ForAll("elements") T a) {
        Ring<T> r = ring();
        assertThat(r.add(a, r.negate(a))).isEqualTo(r.zero());
        assertThat(r.add(r.negate(a), a)).isEqualTo(r.zero());
    }

    @Property
    public void additiveCommutativity(@ForAll("elements") T a, @ForAll("elements") T b) {
        Ring<T> r = ring();
        assertThat(r.add(a, b)).isEqualTo(r.add(b, a));
    }

    @Property
    public void multiplicativeAssociativity(@ForAll("elements") T a, @ForAll("elements") T b, @ForAll("elements") T c) {
        Ring<T> r = ring();
        assertThat(r.multiply(r.multiply(a, b), c)).isEqualTo(r.multiply(a, r.multiply(b, c)));
    }

    @Property
    public void multiplicativeIdentity(@ForAll("elements") T a) {
        Ring<T> r = ring();
        assertThat(r.multiply(a, r.one())).isEqualTo(a);
        assertThat(r.multiply(r.one(), a)).isEqualTo(a);
    }

    @Property
    public void leftDistributivity(@ForAll("elements") T a, @ForAll("elements") T b, @ForAll("elements") T c) {
        Ring<T> r = ring();
        T left = r.multiply(a, r.add(b, c));
        T right = r.add(r.multiply(a, b), r.multiply(a, c));
        assertThat(left).isEqualTo(right);
    }

    @Property
    public void rightDistributivity(@ForAll("elements") T a, @ForAll("elements") T b, @ForAll("elements") T c) {
        Ring<T> r = ring();
        T left = r.multiply(r.add(a, b), c);
        T right = r.add(r.multiply(a, c), r.multiply(b, c));
        assertThat(left).isEqualTo(right);
    }

    @Property
    public void zeroAnnihilation(@ForAll("elements") T a) {
        Ring<T> r = ring();
        assertThat(r.multiply(a, r.zero())).isEqualTo(r.zero());
        assertThat(r.multiply(r.zero(), a)).isEqualTo(r.zero());
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
