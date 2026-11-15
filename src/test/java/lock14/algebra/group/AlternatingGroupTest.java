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
        assertThat(a4.order()).isEqualTo(BigInteger.valueOf(12));
        assertThat(a4.isAbelian()).isFalse();
        assertThat(new AlternatingGroup(3).isAbelian()).isTrue();
        assertThat(new AlternatingGroup(2).elements()).containsExactly(Permutation.identity(2));
        assertThat(new AlternatingGroup(5).elements()).hasSize(60).allMatch(Permutation::isEven);

        Permutation transposition = Permutation.transposition(4, 0, 1);
        assertThat(a4.contains(transposition)).isFalse();
        assertThatThrownBy(() -> a4.combine(a4.identity(), transposition))
            .isInstanceOf(IncompatibleAlgebraException.class);

        SymmetricGroup s4 = new SymmetricGroup(4);
        Subgroup<Permutation> a4Sub = Subgroup.of(s4, a4.elements());
        assertThat(a4Sub.index()).isEqualTo(BigInteger.TWO);
        assertThat(a4Sub.isNormal()).isTrue();

        AlternatingGroup a10 = new AlternatingGroup(10);
        assertThat(a10.order()).isEqualTo(BigInteger.valueOf(1_814_400L));
        assertThat(a10.isAbelian()).isFalse();
        List<Permutation> sampleEven = a10.elements().stream().limit(10).toList();
        assertThat(sampleEven).hasSize(10).allMatch(Permutation::isEven);

        AlternatingGroup a25 = new AlternatingGroup(25);
        assertThat(a25.order()).isEqualTo(new BigInteger("7755605021665492992000000"));
    }
}
