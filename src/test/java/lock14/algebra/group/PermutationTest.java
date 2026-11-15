package lock14.algebra.group;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.laws.GroupLaws;
import lock14.algebra.structure.Group;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PermutationTest extends GroupLaws<Permutation> {

    private final SymmetricGroup s5 = new SymmetricGroup(5);

    @Override
    protected Group<Permutation> group() {
        return s5;
    }

    @Provide
    @Override
    public Arbitrary<Permutation> elements() {
        return Arbitraries.of(0, 1, 2, 3, 4).list().ofSize(5).uniqueElements()
            .map(list -> Permutation.of(list.stream().mapToInt(Integer::intValue).toArray()));
    }

    @Test
    public void testFromCyclesOrderAndSign() {
        // (0 1)(2 3 4) in S_5: disjoint 2-cycle and 3-cycle -> order lcm(2, 3) = 6, odd * even = odd (-1)
        Permutation p = Permutation.fromCycles(5, List.of(List.of(0, 1), List.of(2, 3, 4)));
        assertThat(p.order()).isEqualTo(BigInteger.valueOf(6));
        assertThat(p.sign()).isEqualTo(-1);
        assertThat(p.isOdd()).isTrue();
        assertThat(p.isEven()).isFalse();
        assertThat(s5.power(p, 6)).isEqualTo(Permutation.identity(5));
        assertThat(p.cycles()).containsExactly(List.of(0, 1), List.of(2, 3, 4));

        // Disjoint prime-length cycles whose LCM exceeds Long.MAX_VALUE
        int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53};
        List<List<Integer>> cycles = new ArrayList<>();
        BigInteger expectedOrder = BigInteger.ONE;
        int offset = 0;
        for (int prime : primes) {
            List<Integer> cycle = new ArrayList<>(prime);
            for (int i = 0; i < prime; i++) {
                cycle.add(offset + i);
            }
            cycles.add(cycle);
            expectedOrder = expectedOrder.multiply(BigInteger.valueOf(prime));
            offset += prime;
        }
        Permutation hugeOrderPerm = Permutation.fromCycles(offset, cycles);
        assertThat(expectedOrder).isGreaterThan(BigInteger.valueOf(Long.MAX_VALUE));
        assertThat(hugeOrderPerm.order()).isEqualTo(expectedOrder);
    }

    @Test
    public void testEncapsulationAndValidation() {
        int[] raw = {1, 2, 0};
        Permutation p = Permutation.of(raw);
        raw[0] = 0;
        assertThat(p.apply(0)).isEqualTo(1);

        int[] exported = p.toArray();
        exported[0] = 2;
        assertThat(p.apply(0)).isEqualTo(1);
        assertThat(p.toList()).containsExactly(1, 2, 0);

        assertThatThrownBy(() -> Permutation.of(0, 0, 2))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Permutation.of(0, 3, 1))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> p.combine(Permutation.identity(4)))
            .isInstanceOf(IncompatibleAlgebraException.class);
    }
}
