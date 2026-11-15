package lock14.algebra.group;

import java.math.BigInteger;
import java.util.List;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
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
    public void testSignHomomorphismAndLazyHigherDegrees() {
        assertThat(s4.order()).isEqualTo(BigInteger.valueOf(24));
        assertThat(s4.isAbelian()).isFalse();
        assertThat(new SymmetricGroup(1).isAbelian()).isTrue();
        assertThat(new SymmetricGroup(2).isAbelian()).isTrue();

        CyclicGroup c2 = new CyclicGroup(2);
        var sgn = s4.signHomomorphism();
        assertThat(sgn.isValidHomomorphism(s4, c2)).isTrue();
        assertThat(Subgroup.kernel(sgn, s4, c2).elements()).isEqualTo(new AlternatingGroup(4).elements());

        SymmetricGroup s10 = new SymmetricGroup(10);
        assertThat(s10.order()).isEqualTo(BigInteger.valueOf(3_628_800L));
        assertThat(s10.isAbelian()).isFalse();
        List<Permutation> firstFive = s10.elements().stream().limit(5).toList();
        assertThat(firstFive).hasSize(5);
        assertThat(firstFive.getFirst()).isEqualTo(Permutation.identity(10));
        assertThat(s10.elements()).contains(Permutation.transposition(10, 0, 9));

        SymmetricGroup s25 = new SymmetricGroup(25);
        assertThat(s25.order()).isEqualTo(new BigInteger("15511210043330985984000000"));
        assertThat(s25.elements().iterator().next()).isEqualTo(Permutation.identity(25));
        assertThatThrownBy(() -> s4.inverse(Permutation.identity(3))).isInstanceOf(IncompatibleAlgebraException.class);
    }
}
