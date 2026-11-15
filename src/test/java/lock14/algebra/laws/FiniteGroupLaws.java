package lock14.algebra.laws;

import java.math.BigInteger;
import lock14.algebra.group.Subgroup;
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
    public void carrierSetContainsIdentityMatchesOrderAndIsGeneratedByGenerators() {
        FiniteGroup<T> g = finiteGroup();
        assertThat(g.elements()).isNotEmpty();
        assertThat(g.elements()).contains(g.identity());
        assertThat(g.order()).isEqualTo(BigInteger.valueOf(g.elements().size()));
        assertThat(Subgroup.generate(g, g.generators()).elements()).isEqualTo(g.elements());
    }

    @Property
    public void closureAndLagrangeElementOrder(@ForAll("elements") T a, @ForAll("elements") T b) {
        FiniteGroup<T> g = finiteGroup();
        assertThat(g.elements()).contains(g.combine(a, b));
        assertThat(g.elements()).contains(g.inverse(a));

        BigInteger elemOrder = g.elementOrder(a);
        assertThat(elemOrder).isPositive();
        assertThat(g.order().mod(elemOrder)).isZero();
        assertThat(g.power(a, elemOrder)).isEqualTo(g.identity());
        assertThat(g.power(a, g.order())).isEqualTo(g.identity());
    }
}
