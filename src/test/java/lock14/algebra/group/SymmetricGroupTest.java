package lock14.algebra.group;

import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.UndefinedOperationException;
import lock14.algebra.laws.FiniteGroupLaws;
import lock14.algebra.structure.FiniteGroup;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SymmetricGroupTest extends FiniteGroupLaws<Permutation> {

    private final SymmetricGroup s4 = new SymmetricGroup(4);

    @Override
    protected FiniteGroup<Permutation> finiteGroup() {
        return s4;
    }

    @Provide
    @Override
    public Arbitrary<Permutation> elements() {
        return Arbitraries.of(s4.elements());
    }

    @Test
    public void testSignHomomorphismAndHigherDegrees() {
        assertThat(s4.order()).isEqualTo(24L);
        assertThat(s4.isAbelian()).isFalse();

        CyclicGroup c2 = new CyclicGroup(2);
        var sgn = s4.signHomomorphism();
        assertThat(sgn.isValidHomomorphism(s4, c2)).isTrue();
        assertThat(Subgroup.kernel(sgn, s4, c2).elements()).isEqualTo(new AlternatingGroup(4).elements());

        SymmetricGroup s10 = new SymmetricGroup(10);
        assertThat(s10.order()).isEqualTo(3_628_800L);
        assertThatThrownBy(s10::elements).isInstanceOf(UndefinedOperationException.class);
        assertThatThrownBy(() -> s4.inverse(Permutation.identity(3))).isInstanceOf(IncompatibleAlgebraException.class);
    }
}
