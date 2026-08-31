package lock14.algebra.discrete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.laws.GroupLaws;
import lock14.algebra.structure.Group;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PermutationTest extends GroupLaws<Permutation> {

    private final SymmetricGroup s4 = new SymmetricGroup(4);

    @Override
    protected Group<Permutation> group() {
        return s4;
    }

    @Override
    @Provide
    public Arbitrary<Permutation> elements() {
        return Arbitraries.randomValue(random -> {
            List<Integer> list = new ArrayList<>(List.of(0, 1, 2, 3));
            Collections.shuffle(list, random);
            int[] map = list.stream().mapToInt(Integer::intValue).toArray();
            return Permutation.of(map);
        });
    }

    @Property
    public void signHomomorphism(@ForAll("elements") Permutation a, @ForAll("elements") Permutation b) {
        int signProd = a.combine(b).sign();
        int prodSigns = a.sign() * b.sign();
        assertThat(signProd).isEqualTo(prodSigns);
    }

    @Test
    public void testCyclesAndParity() {
        // Transposition (0 1) in S_4 is odd (sign = -1)
        Permutation t = Permutation.transposition(4, 0, 1);
        assertThat(t.sign()).isEqualTo(-1);
        assertThat(t.isOdd()).isTrue();
        assertThat(t.isEven()).isFalse();

        // 3-cycle (0 1 2) is even (sign = 1)
        Permutation c3 = Permutation.of(1, 2, 0, 3);
        assertThat(c3.sign()).isEqualTo(1);
        assertThat(c3.isEven()).isTrue();
    }

    @Test
    public void testInvalidPermutationThrows() {
        // Duplicate element
        assertThatThrownBy(() -> Permutation.of(0, 1, 1, 3))
                .isInstanceOf(IllegalArgumentException.class);

        // Out of bounds element
        assertThatThrownBy(() -> Permutation.of(0, 1, 2, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
