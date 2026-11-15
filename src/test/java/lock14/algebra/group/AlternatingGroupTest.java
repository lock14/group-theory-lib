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
        assertThat(a4.degree()).isEqualTo(4);
        assertThat(a4.order()).isEqualTo(BigInteger.valueOf(12));
        assertThat(a4.isAbelian()).isFalse();
        assertThat(new AlternatingGroup(3).isAbelian()).isTrue();
        assertThat(new AlternatingGroup(1).generators()).containsExactly(Permutation.identity(1));
        assertThat(new AlternatingGroup(2).generators()).containsExactly(Permutation.identity(2));
        assertThat(new AlternatingGroup(2).elements()).containsExactly(Permutation.identity(2));
        assertThat(new AlternatingGroup(5).elements()).hasSize(60).allMatch(Permutation::isEven);

        Permutation transposition = Permutation.transposition(4, 0, 1);
        assertThat(a4.contains(a4.identity())).isTrue();
        assertThat(a4.contains(transposition)).isFalse();
        assertThat(a4.contains(Permutation.identity(3))).isFalse();
        assertThatThrownBy(() -> a4.combine(a4.identity(), transposition))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> a4.inverse(Permutation.identity(3)))
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

    @Test
    public void testValidationElementsSetIteratorAndEquality() {
        assertThatThrownBy(() -> new AlternatingGroup(0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AlternatingGroup(-1))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(a4.elements().isEmpty()).isFalse();
        assertThat(a4.elements().contains(a4.identity())).isTrue();
        assertThat(a4.elements().contains(Permutation.transposition(4, 0, 1))).isFalse();
        assertThat(a4.elements().contains(Permutation.identity(3))).isFalse();
        assertThat(a4.elements().contains("not a permutation")).isFalse();

        Iterator<Permutation> it = new AlternatingGroup(2).elements().iterator();
        assertThat(it.next()).isEqualTo(Permutation.identity(2));
        assertThat(it.hasNext()).isFalse();
        assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);

        AlternatingGroup a4Copy = new AlternatingGroup(4);
        assertThat(a4).isEqualTo(a4Copy);
        assertThat(a4).hasSameHashCodeAs(a4Copy);
        assertThat(a4).isNotEqualTo(new AlternatingGroup(5));
        assertThat(a4.equals("A_4")).isFalse();
        assertThat(a4.equals(null)).isFalse();
        assertThat(a4.toString()).isEqualTo("A_4");
    }
}
