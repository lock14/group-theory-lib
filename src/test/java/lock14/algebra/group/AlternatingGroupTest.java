package lock14.algebra.group;

import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.laws.FiniteGroupLaws;
import lock14.algebra.structure.FiniteGroup;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AlternatingGroupTest extends FiniteGroupLaws<Permutation> {

    private final AlternatingGroup a4 = new AlternatingGroup(4);

    @Override
    protected FiniteGroup<Permutation> finiteGroup() {
        return a4;
    }

    @Provide
    @Override
    public Arbitrary<Permutation> elements() {
        return Arbitraries.of(a4.elements());
    }

    @Test
    public void testOrderNormalityAndOddPermutationRejection() {
        assertThat(a4.order()).isEqualTo(12L);
        assertThat(a4.isAbelian()).isFalse();

        Permutation transposition = Permutation.transposition(4, 0, 1);
        assertThat(a4.contains(transposition)).isFalse();
        assertThatThrownBy(() -> a4.combine(a4.identity(), transposition))
            .isInstanceOf(IncompatibleAlgebraException.class);

        SymmetricGroup s4 = new SymmetricGroup(4);
        Subgroup<Permutation> a4Sub = Subgroup.of(s4, a4.elements());
        assertThat(a4Sub.index()).isEqualTo(2L);
        assertThat(a4Sub.isNormal()).isTrue();
    }
}
