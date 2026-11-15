package lock14.algebra.group;

import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.laws.FiniteGroupLaws;
import lock14.algebra.structure.FiniteGroup;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QuotientAndActionTest extends FiniteGroupLaws<Coset<Permutation>> {

    private final SymmetricGroup s4 = new SymmetricGroup(4);
    private final Subgroup<Permutation> kleinFour = Subgroup.of(s4, Set.of(
        Permutation.identity(4),
        Permutation.fromCycles(4, List.of(List.of(0, 1), List.of(2, 3))),
        Permutation.fromCycles(4, List.of(List.of(0, 2), List.of(1, 3))),
        Permutation.fromCycles(4, List.of(List.of(0, 3), List.of(1, 2)))
    ));
    private final QuotientGroup<Permutation> s4ModV4 = QuotientGroup.of(s4, kleinFour);

    @Override
    protected FiniteGroup<Coset<Permutation>> finiteGroup() {
        return s4ModV4;
    }

    @Provide
    @Override
    public Arbitrary<Coset<Permutation>> elements() {
        return Arbitraries.of(s4ModV4.elements());
    }

    @Test
    public void testQuotientGroupStructureAndFirstIsomorphismTheorem() {
        // S_4 / V_4 is isomorphic to S_3 (non-abelian of order 6)
        assertThat(s4ModV4.parentGroup()).isEqualTo(s4);
        assertThat(s4ModV4.normalSubgroup()).isEqualTo(kleinFour);
        assertThat(s4ModV4.order()).isEqualTo(BigInteger.valueOf(6));
        assertThat(s4ModV4.isAbelian()).isFalse();
        assertThat(s4ModV4.canonicalProjection().isValidHomomorphism(s4, s4ModV4)).isTrue();

        // First Isomorphism Theorem: S_4 / ker(sgn) has order 2 and is abelian
        CyclicGroup c2 = new CyclicGroup(2);
        Subgroup<Permutation> kerSgn = Subgroup.kernel(s4.signHomomorphism(), s4, c2);
        QuotientGroup<Permutation> quotientByAlternating = QuotientGroup.of(s4, kerSgn);
        assertThat(quotientByAlternating.order()).isEqualTo(BigInteger.TWO);
        assertThat(quotientByAlternating.isAbelian()).isTrue();

        // Quotient by the whole group S_4 / S_4 has order 1 and empty projected generators
        QuotientGroup<Permutation> trivialQuotient = QuotientGroup.of(s4, Subgroup.of(s4, s4.elements()));
        assertThat(trivialQuotient.order()).isEqualTo(BigInteger.ONE);
        assertThat(trivialQuotient.generators()).isEmpty();

        // Non-normal subgroup <(0 1)> in S_4 cannot form a quotient group
        Subgroup<Permutation> nonNormal = Subgroup.generate(s4, List.of(Permutation.transposition(4, 0, 1)));
        assertThat(nonNormal.isNormal()).isFalse();
        assertThatThrownBy(() -> QuotientGroup.of(s4, nonNormal))
            .isInstanceOf(IllegalArgumentException.class);

        // Parent group mismatch and foreign coset rejection
        SymmetricGroup s3 = new SymmetricGroup(3);
        assertThatThrownBy(() -> QuotientGroup.of(s3, kleinFour))
            .isInstanceOf(IncompatibleAlgebraException.class);
        Coset<Permutation> foreignCoset = nonNormal.leftCoset(s4.identity());
        assertThatThrownBy(() -> s4ModV4.combine(s4ModV4.identity(), foreignCoset))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> s4ModV4.inverse(foreignCoset))
            .isInstanceOf(IncompatibleAlgebraException.class);

        QuotientGroup<Permutation> s4ModV4Copy = QuotientGroup.of(s4, kleinFour);
        QuotientGroup<Permutation> s3ModS3 = QuotientGroup.of(s3, Subgroup.of(s3, s3.elements()));
        assertThat(s4ModV4.equals(s4ModV4)).isTrue();
        assertThat(s4ModV4).isEqualTo(s4ModV4Copy);
        assertThat(s4ModV4).hasSameHashCodeAs(s4ModV4Copy);
        assertThat(s4ModV4).isNotEqualTo(quotientByAlternating);
        assertThat(s4ModV4.equals(s3ModS3)).isFalse();
        assertThat(s4ModV4.equals("not a quotient group")).isFalse();
        assertThat(s4ModV4.equals(null)).isFalse();
    }

    @Test
    public void testOrbitStabilizerTheoremAndConjugacyClasses() {
        // Conjugation action of S_4 on itself partitions S_4 into 5 conjugacy classes (partitions of 4)
        GroupAction<Permutation, Permutation> conj = GroupAction.conjugation(s4);
        Set<Set<Permutation>> classes = conj.orbits(s4, s4.elements());
        assertThat(classes).hasSize(5);
        assertThat(conj.countOrbitsBurnside(s4, s4.elements())).isEqualTo(BigInteger.valueOf(5));

        for (Permutation x : s4.elements()) {
            BigInteger orbitSize = BigInteger.valueOf(conj.orbit(s4, x).size());
            BigInteger stabOrder = conj.stabilizer(s4, x).order();
            assertThat(orbitSize.multiply(stabOrder)).isEqualTo(s4.order());
        }

        // Left regular (Cayley) action is transitive and free
        GroupAction<Permutation, Permutation> leftReg = GroupAction.leftTranslation(s4);
        assertThat(leftReg.orbit(s4, s4.identity())).hasSize(24);
        assertThat(leftReg.stabilizer(s4, s4.identity()).order()).isEqualTo(BigInteger.ONE);

        // Generator BFS computes orbits on S_25 in O(|orbit| * |S|) without enumerating 25! elements
        Set<Integer> transitiveOrbit = GroupAction.permutationOnPoints().orbit(new SymmetricGroup(25), 0);
        assertThat(transitiveOrbit).hasSize(25);

        // Pseudo-action violating group action axioms is caught by Burnside divisibility check
        CyclicGroup c2 = new CyclicGroup(2);
        GroupAction<Integer, Integer> brokenAction = (g, x) -> (g == 1 && x == 0) ? 1 : x;
        assertThatThrownBy(() -> brokenAction.countOrbitsBurnside(c2, Set.of(0, 1)))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testBurnsideLemmaSquareVertexTwoColorings() {
        // Number of distinct 2-colorings of the 4 vertices of a square under D_4 symmetries = 6
        DihedralGroup d4 = new DihedralGroup(4);
        Set<List<Integer>> allColorings = new LinkedHashSet<>();
        for (int mask = 0; mask < 16; mask++) {
            allColorings.add(List.of(
                (mask >> 0) & 1,
                (mask >> 1) & 1,
                (mask >> 2) & 1,
                (mask >> 3) & 1
            ));
        }

        GroupAction<DihedralGroup.Element, List<Integer>> coloringAction = (sym, colors) -> {
            Integer[] next = new Integer[4];
            for (int v = 0; v < 4; v++) {
                int imageVertex = sym.reflected()
                    ? Math.floorMod(sym.rotation() - v, 4)
                    : Math.floorMod(v + sym.rotation(), 4);
                next[imageVertex] = colors.get(v);
            }
            return List.of(next);
        };

        assertThat(coloringAction.countOrbitsBurnside(d4, allColorings)).isEqualTo(BigInteger.valueOf(6));
        assertThat(coloringAction.orbits(d4, allColorings)).hasSize(6);
    }
}
