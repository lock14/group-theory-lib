package lock14.algebra.group;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GroupExamplesTest {

    @Test
    void symmetricQuotientAndBurnsideExample() {
        // @start region="quotient-and-burnside"
        SymmetricGroup s4 = new SymmetricGroup(4);
        Subgroup<Permutation> kleinFour = Subgroup.of(s4, Set.of(
                Permutation.identity(4),
                Permutation.fromCycles(4, List.of(List.of(0, 1), List.of(2, 3))),
                Permutation.fromCycles(4, List.of(List.of(0, 2), List.of(1, 3))),
                Permutation.fromCycles(4, List.of(List.of(0, 3), List.of(1, 2)))
        ));

        QuotientGroup<Permutation> s4ModV4 = QuotientGroup.of(s4, kleinFour);
        BigInteger conjugacyClassCount = GroupAction.conjugation(s4)
                .countOrbitsBurnside(s4, s4.elements());
        // @end

        assertThat(kleinFour.isNormal()).isTrue();
        assertThat(s4ModV4.order()).isEqualTo(BigInteger.valueOf(6));
        assertThat(conjugacyClassCount).isEqualTo(BigInteger.valueOf(5));
    }
}
