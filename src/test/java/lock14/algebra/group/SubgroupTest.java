package lock14.algebra.group;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.laws.FiniteGroupLaws;
import lock14.algebra.structure.FiniteGroup;
import lock14.algebra.structure.GroupHomomorphism;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SubgroupTest extends FiniteGroupLaws<Permutation> {

    private final SymmetricGroup s4 = new SymmetricGroup(4);
    private final Subgroup<Permutation> kleinFour = Subgroup.of(s4, Set.of(
        Permutation.identity(4),
        Permutation.fromCycles(4, List.of(List.of(0, 1), List.of(2, 3))),
        Permutation.fromCycles(4, List.of(List.of(0, 2), List.of(1, 3))),
        Permutation.fromCycles(4, List.of(List.of(0, 3), List.of(1, 2)))
    ));

    @Override
    protected FiniteGroup<Permutation> finiteGroup() {
        return kleinFour;
    }

    @Provide
    @Override
    public Arbitrary<Permutation> elements() {
        return Arbitraries.of(kleinFour.elements());
    }

    @Test
    public void testSubgroupValidationAndMembershipGuards() {
        assertThatThrownBy(() -> Subgroup.of(s4, Set.of()))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Subgroup.of(s4, Set.of(Permutation.identity(3))))
            .isInstanceOf(IllegalArgumentException.class);

        CyclicGroup c4 = new CyclicGroup(4);
        assertThatThrownBy(() -> Subgroup.of(c4, Set.of(1)))
            .isInstanceOf(IllegalArgumentException.class);

        Set<Permutation> nonClosed = Set.of(
            s4.identity(),
            Permutation.transposition(4, 0, 1),
            Permutation.transposition(4, 1, 2)
        );
        assertThatThrownBy(() -> Subgroup.of(s4, nonClosed))
            .isInstanceOf(IllegalArgumentException.class);

        Permutation outsideSubgroup = Permutation.transposition(4, 0, 1);
        assertThatThrownBy(() -> kleinFour.combine(kleinFour.identity(), outsideSubgroup))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> kleinFour.combine(outsideSubgroup, kleinFour.identity()))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> kleinFour.inverse(outsideSubgroup))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> kleinFour.elementOrder(outsideSubgroup))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> kleinFour.generateClosure(List.of(outsideSubgroup)))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(kleinFour.generateClosure(List.of(kleinFour.identity())))
            .containsExactly(kleinFour.identity());
    }

    @Test
    public void testImageNormalityCosetsAndEquality() {
        assertThat(kleinFour.parentGroup()).isEqualTo(s4);
        assertThat(kleinFour.order()).isEqualTo(BigInteger.valueOf(4));
        assertThat(kleinFour.index()).isEqualTo(BigInteger.valueOf(6));
        assertThat(kleinFour.isAbelian()).isTrue();
        assertThat(kleinFour.isNormal()).isTrue();

        CyclicGroup c2 = new CyclicGroup(2);
        Subgroup<Integer> signImage = Subgroup.image(s4.signHomomorphism(), s4, c2);
        assertThat(signImage.elements()).isEqualTo(c2.elements());
        assertThat(signImage.parentGroup()).isEqualTo(c2);

        GroupHomomorphism<Integer, Permutation> embedSwap =
            x -> x == 0 ? s4.identity() : Permutation.transposition(4, 0, 1);
        Subgroup<Permutation> swapSub = Subgroup.image(embedSwap, c2, s4);
        assertThat(swapSub.order()).isEqualTo(BigInteger.TWO);
        assertThat(swapSub.index()).isEqualTo(BigInteger.valueOf(12));
        assertThat(swapSub.isNormal()).isFalse();

        for (Permutation g : s4.elements()) {
            assertThat(kleinFour.leftCoset(g)).isEqualTo(kleinFour.rightCoset(g));
        }

        Permutation cycle3 = Permutation.fromCycles(4, List.of(List.of(0, 1, 2)));
        Coset<Permutation> left = swapSub.leftCoset(cycle3);
        Coset<Permutation> right = swapSub.rightCoset(cycle3);
        assertThat(left).isNotEqualTo(right);
        assertThat(left.parentGroup()).isEqualTo(s4);
        assertThat(left.representative()).isEqualTo(cycle3);
        assertThat(left.elements()).hasSize(2);
        assertThat(left.contains(cycle3)).isTrue();
        assertThat(left.contains(s4.identity())).isFalse();
        assertThat(left.toString()).contains("H");

        Permutation otherRepSameCoset = s4.combine(cycle3, Permutation.transposition(4, 0, 1));
        Coset<Permutation> leftAlt = swapSub.leftCoset(otherRepSameCoset);
        assertThat(left).isEqualTo(leftAlt);
        assertThat(left).hasSameHashCodeAs(leftAlt);

        SymmetricGroup s3 = new SymmetricGroup(3);
        Subgroup<Permutation> trivialS3 = Subgroup.of(s3, Set.of(s3.identity()));
        Coset<Permutation> foreignCoset = trivialS3.leftCoset(s3.identity());
        assertThat(left.equals(foreignCoset)).isFalse();
        assertThat(left.equals("not a coset")).isFalse();
        assertThat(left.equals(null)).isFalse();

        assertThatThrownBy(() -> kleinFour.leftCoset(s3.identity()))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> kleinFour.rightCoset(s3.identity()))
            .isInstanceOf(IncompatibleAlgebraException.class);

        Subgroup<Permutation> kleinFourGenerated = Subgroup.generate(s4, kleinFour.elements());
        assertThat(kleinFour.equals(kleinFour)).isTrue();
        assertThat(kleinFour).isEqualTo(kleinFourGenerated);
        assertThat(kleinFour).hasSameHashCodeAs(kleinFourGenerated);
        assertThat(kleinFour).isNotEqualTo(swapSub);
        assertThat(kleinFour.equals(trivialS3)).isFalse();
        assertThat(kleinFour.equals("not a subgroup")).isFalse();
        assertThat(kleinFour.equals(null)).isFalse();
        assertThat(kleinFour.toString()).isNotBlank();
    }

    @Test
    public void testNormalityInverseConjugationBranch() {
        record CustomGeneratorView(SymmetricGroup base, Permutation leadingGenerator)
            implements FiniteGroup<Permutation> {
            @Override
            public Set<Permutation> elements() {
                return base.elements();
            }

            @Override
            public Collection<Permutation> generators() {
                List<Permutation> gens = new ArrayList<>();
                gens.add(leadingGenerator);
                gens.addAll(base.generators());
                return List.copyOf(gens);
            }

            @Override
            public BigInteger order() {
                return base.order();
            }

            @Override
            public boolean contains(Permutation element) {
                return base.contains(element);
            }

            @Override
            public Permutation identity() {
                return base.identity();
            }

            @Override
            public Permutation combine(Permutation left, Permutation right) {
                return base.combine(left, right);
            }

            @Override
            public Permutation inverse(Permutation element) {
                return base.inverse(element);
            }
        }

        SymmetricGroup s8 = new SymmetricGroup(8);
        Permutation a = Permutation.fromCycles(8, List.of(List.of(0, 1), List.of(2, 3)));
        Permutation b = Permutation.fromCycles(8, List.of(List.of(2, 3), List.of(4, 5)));
        Permutation c = Permutation.fromCycles(8, List.of(List.of(0, 1), List.of(4, 5)));
        Set<Permutation> carrier = Set.of(s8.identity(), a, b, c);

        List<Permutation> blockCycles = List.of(
            Permutation.fromCycles(8, List.of(List.of(0, 2, 4, 6), List.of(1, 3, 5, 7))),
            Permutation.fromCycles(8, List.of(List.of(2, 4, 0, 6), List.of(3, 5, 1, 7))),
            Permutation.fromCycles(8, List.of(List.of(4, 0, 2, 6), List.of(5, 1, 3, 7)))
        );
        for (Permutation g : blockCycles) {
            Subgroup<Permutation> h = Subgroup.of(new CustomGeneratorView(s8, g), carrier);
            assertThat(h.isNormal()).isFalse();
        }
    }
}
