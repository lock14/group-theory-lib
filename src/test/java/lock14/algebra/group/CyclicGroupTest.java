package lock14.algebra.group;

import java.math.BigInteger;
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
    public void testGeneratorCyclicSubgroupOrdersAndLazyLargeModulus() {
        assertThat(c12.isAbelian()).isTrue();
        assertThat(c12.elementOrder(c12.generator())).isEqualTo(BigInteger.valueOf(12));
        assertThat(Subgroup.generate(c12, List.of(4)).order()).isEqualTo(BigInteger.valueOf(3));
        assertThat(Subgroup.generate(c12, List.of(6)).order()).isEqualTo(BigInteger.TWO);
        assertThat(Subgroup.generate(c12, List.of(5)).order()).isEqualTo(BigInteger.valueOf(12));

        CyclicGroup cMax = new CyclicGroup(Integer.MAX_VALUE);
        assertThat(cMax.order()).isEqualTo(BigInteger.valueOf(Integer.MAX_VALUE));
        assertThat(cMax.combine(Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1))
            .isEqualTo(Integer.MAX_VALUE - 2);
        assertThat(cMax.elementOrder(1)).isEqualTo(BigInteger.valueOf(Integer.MAX_VALUE));
        assertThat(cMax.elements().stream().limit(4).toList()).containsExactly(0, 1, 2, 3);
    }
}
