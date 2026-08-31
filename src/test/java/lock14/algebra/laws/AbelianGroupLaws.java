package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import lock14.algebra.structure.AbelianGroup;
import lock14.algebra.structure.Group;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbelianGroupLaws<T> extends GroupLaws<T> {

    protected abstract AbelianGroup<T> abelianGroup();

    @Override
    protected Group<T> group() {
        return abelianGroup();
    }

    @Property
    public void commutativity(@ForAll("elements") T a, @ForAll("elements") T b) {
        AbelianGroup<T> g = abelianGroup();
        assertThat(g.combine(a, b)).isEqualTo(g.combine(b, a));
    }
}
