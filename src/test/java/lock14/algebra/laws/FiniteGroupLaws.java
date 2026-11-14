package lock14.algebra.laws;

import lock14.algebra.structure.FiniteGroup;
import lock14.algebra.structure.Group;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class FiniteGroupLaws<T> extends GroupLaws<T> {

    protected abstract FiniteGroup<T> finiteGroup();

    @Override
    protected Group<T> group() {
        return finiteGroup();
    }

    @Test
    public void carrierSetContainsIdentityAndMatchesOrder() {
        FiniteGroup<T> g = finiteGroup();
        assertThat(g.elements()).isNotEmpty();
        assertThat(g.elements()).contains(g.identity());
        assertThat(g.order()).isEqualTo(g.elements().size());
    }

    @Property
    public void closureAndLagrangeElementOrder(@ForAll("elements") T a, @ForAll("elements") T b) {
        FiniteGroup<T> g = finiteGroup();
        assertThat(g.elements()).contains(g.combine(a, b));
        assertThat(g.elements()).contains(g.inverse(a));

        long elemOrder = g.elementOrder(a);
        assertThat(elemOrder).isPositive();
        assertThat(g.order() % elemOrder).isZero();
        assertThat(g.power(a, elemOrder)).isEqualTo(g.identity());
        assertThat(g.power(a, g.order())).isEqualTo(g.identity());
    }
}
