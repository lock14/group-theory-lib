package lock14.algebra.group;

import java.util.List;
import lock14.algebra.laws.FiniteGroupLaws;
import lock14.algebra.structure.FiniteGroup;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CyclicGroupTest extends FiniteGroupLaws<Integer> {

    private final CyclicGroup c12 = new CyclicGroup(12);

    @Override
    protected FiniteGroup<Integer> finiteGroup() {
        return c12;
    }

    @Provide
    @Override
    public Arbitrary<Integer> elements() {
        return Arbitraries.of(c12.elements());
    }

    @Test
    public void testGeneratorAndCyclicSubgroupOrders() {
        assertThat(c12.isAbelian()).isTrue();
        assertThat(c12.elementOrder(c12.generator())).isEqualTo(12L);
        assertThat(Subgroup.generate(c12, List.of(4)).order()).isEqualTo(3L);
        assertThat(Subgroup.generate(c12, List.of(6)).order()).isEqualTo(2L);
        assertThat(Subgroup.generate(c12, List.of(5)).order()).isEqualTo(12L);
    }
}
