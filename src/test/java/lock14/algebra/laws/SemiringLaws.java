package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;
import lock14.algebra.structure.Semiring;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class SemiringLaws<T> {

    protected abstract Semiring<T> semiring();

    @Property
    public void additiveAssociativity(@ForAll("elements") T a, @ForAll("elements") T b, @ForAll("elements") T c) {
        Semiring<T> s = semiring();
        assertThat(s.add(s.add(a, b), c)).isEqualTo(s.add(a, s.add(b, c)));
    }

    @Property
    public void additiveIdentity(@ForAll("elements") T a) {
        Semiring<T> s = semiring();
        assertThat(s.add(a, s.zero())).isEqualTo(a);
        assertThat(s.add(s.zero(), a)).isEqualTo(a);
    }

    @Property
    public void additiveCommutativity(@ForAll("elements") T a, @ForAll("elements") T b) {
        Semiring<T> s = semiring();
        assertThat(s.add(a, b)).isEqualTo(s.add(b, a));
    }

    @Property
    public void multiplicativeAssociativity(@ForAll("elements") T a, @ForAll("elements") T b, @ForAll("elements") T c) {
        Semiring<T> s = semiring();
        assertThat(s.multiply(s.multiply(a, b), c)).isEqualTo(s.multiply(a, s.multiply(b, c)));
    }

    @Property
    public void multiplicativeIdentity(@ForAll("elements") T a) {
        Semiring<T> s = semiring();
        assertThat(s.multiply(a, s.one())).isEqualTo(a);
        assertThat(s.multiply(s.one(), a)).isEqualTo(a);
    }

    @Property
    public void leftDistributivity(@ForAll("elements") T a, @ForAll("elements") T b, @ForAll("elements") T c) {
        Semiring<T> s = semiring();
        T left = s.multiply(a, s.add(b, c));
        T right = s.add(s.multiply(a, b), s.multiply(a, c));
        assertThat(left).isEqualTo(right);
    }

    @Property
    public void rightDistributivity(@ForAll("elements") T a, @ForAll("elements") T b, @ForAll("elements") T c) {
        Semiring<T> s = semiring();
        T left = s.multiply(s.add(a, b), c);
        T right = s.add(s.multiply(a, c), s.multiply(b, c));
        assertThat(left).isEqualTo(right);
    }

    @Property
    public void zeroAnnihilation(@ForAll("elements") T a) {
        Semiring<T> s = semiring();
        assertThat(s.multiply(a, s.zero())).isEqualTo(s.zero());
        assertThat(s.multiply(s.zero(), a)).isEqualTo(s.zero());
    }

    @Test
    public void zeroAndOnePredicates() {
        Semiring<T> s = semiring();
        assertThat(s.isZero(s.zero())).isTrue();
        assertThat(s.isOne(s.one())).isTrue();
    }
}
