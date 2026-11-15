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
        assertThat(s4.degree()).isEqualTo(4);
        assertThat(s4.order()).isEqualTo(BigInteger.valueOf(24));
        assertThat(s4.isAbelian()).isFalse();

        SymmetricGroup s1 = new SymmetricGroup(1);
        assertThat(s1.isAbelian()).isTrue();
        assertThat(s1.generators()).containsExactly(Permutation.identity(1));

        SymmetricGroup s2 = new SymmetricGroup(2);
        assertThat(s2.isAbelian()).isTrue();
        assertThat(s2.generators()).containsExactly(Permutation.transposition(2, 0, 1));

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

    @Test
    public void testValidationMembershipIteratorAndEquality() {
        assertThatThrownBy(() -> new SymmetricGroup(0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SymmetricGroup(-2))
            .isInstanceOf(IllegalArgumentException.class);

        Permutation id3 = Permutation.identity(3);
        assertThat(s4.contains(s4.identity())).isTrue();
        assertThat(s4.contains(id3)).isFalse();
        assertThatThrownBy(() -> s4.combine(s4.identity(), id3))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> s4.signHomomorphism().apply(id3))
            .isInstanceOf(IncompatibleAlgebraException.class);

        assertThat(s4.elements().isEmpty()).isFalse();
        assertThat(s4.elements().contains(s4.identity())).isTrue();
        assertThat(s4.elements().contains(id3)).isFalse();
        assertThat(s4.elements().contains("not a permutation")).isFalse();

        Iterator<Permutation> it = new SymmetricGroup(1).elements().iterator();
        assertThat(it.next()).isEqualTo(Permutation.identity(1));
        assertThat(it.hasNext()).isFalse();
        assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);

        SymmetricGroup s4Copy = new SymmetricGroup(4);
        assertThat(s4).isEqualTo(s4Copy);
        assertThat(s4).hasSameHashCodeAs(s4Copy);
        assertThat(s4).isNotEqualTo(new SymmetricGroup(3));
        assertThat(s4.equals("S_4")).isFalse();
        assertThat(s4.equals(null)).isFalse();
        assertThat(s4.toString()).isEqualTo("S_4");
    }
}
