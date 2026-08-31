package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import lock14.algebra.structure.Group;
import lock14.algebra.structure.Monoid;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class GroupLaws<T> extends MonoidLaws<T> {

    protected abstract Group<T> group();

    @Override
    protected Monoid<T> monoid() {
        return group();
    }

    @Property
    public void leftInverse(@ForAll("elements") T a) {
        Group<T> g = group();
        assertThat(g.combine(g.inverse(a), a)).isEqualTo(g.identity());
    }

    @Property
    public void rightInverse(@ForAll("elements") T a) {
        Group<T> g = group();
        assertThat(g.combine(a, g.inverse(a))).isEqualTo(g.identity());
    }

    @Property
    public void inverseInvolution(@ForAll("elements") T a) {
        Group<T> g = group();
        assertThat(g.inverse(g.inverse(a))).isEqualTo(a);
    }

    @Property
    public void inverseAntiHomomorphism(@ForAll("elements") T a, @ForAll("elements") T b) {
        Group<T> g = group();
        T invProduct = g.inverse(g.combine(a, b));
        T productInverses = g.combine(g.inverse(b), g.inverse(a));
        assertThat(invProduct).isEqualTo(productInverses);
    }

    @Property
    public void removeConsistency(@ForAll("elements") T a, @ForAll("elements") T b) {
        Group<T> g = group();
        assertThat(g.remove(a, b)).isEqualTo(g.combine(a, g.inverse(b)));
    }

    @Property
    public void powerNegativeOne(@ForAll("elements") T a) {
        Group<T> g = group();
        assertThat(g.power(a, -1)).isEqualTo(g.inverse(a));
    }
}
