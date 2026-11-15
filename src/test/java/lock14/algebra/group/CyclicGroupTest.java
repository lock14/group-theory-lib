package lock14.algebra.group;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.laws.FiniteGroupLaws;
import lock14.algebra.structure.FiniteGroup;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        CyclicGroup c1 = new CyclicGroup(1);
        assertThat(c1.generator()).isZero();
        assertThat(c1.generators()).containsExactly(0);
        assertThat(c1.order()).isEqualTo(BigInteger.ONE);

        CyclicGroup cMax = new CyclicGroup(Integer.MAX_VALUE);
        assertThat(cMax.order()).isEqualTo(BigInteger.valueOf(Integer.MAX_VALUE));
        assertThat(cMax.combine(Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1))
            .isEqualTo(Integer.MAX_VALUE - 2);
        assertThat(cMax.elementOrder(1)).isEqualTo(BigInteger.valueOf(Integer.MAX_VALUE));
        assertThat(cMax.elements().stream().limit(4).toList()).containsExactly(0, 1, 2, 3);
    }

    @Test
    public void testValidationMembershipIteratorAndEquality() {
        assertThatThrownBy(() -> new CyclicGroup(0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CyclicGroup(-5))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(c12.contains(5)).isTrue();
        assertThat(c12.contains(-1)).isFalse();
        assertThat(c12.contains(12)).isFalse();
        assertThatThrownBy(() -> c12.combine(-1, 0))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> c12.combine(12, 0))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> c12.inverse(-1))
            .isInstanceOf(IncompatibleAlgebraException.class);

        assertThat(c12.elements().isEmpty()).isFalse();
        assertThat(c12.elements().contains(0)).isTrue();
        assertThat(c12.elements().contains(-1)).isFalse();
        assertThat(c12.elements().contains(12)).isFalse();
        assertThat(c12.elements().contains("not an integer")).isFalse();

        Iterator<Integer> it = new CyclicGroup(1).elements().iterator();
        assertThat(it.next()).isZero();
        assertThat(it.hasNext()).isFalse();
        assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);

        CyclicGroup c12Copy = new CyclicGroup(12);
        assertThat(c12).isEqualTo(c12Copy);
        assertThat(c12).hasSameHashCodeAs(c12Copy);
        assertThat(c12).isNotEqualTo(new CyclicGroup(6));
        assertThat(c12.equals("C_12")).isFalse();
        assertThat(c12.equals(null)).isFalse();
        assertThat(c12.toString()).isEqualTo("C_12");
    }
}
