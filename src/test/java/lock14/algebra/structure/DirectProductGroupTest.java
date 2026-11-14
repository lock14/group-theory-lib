package lock14.algebra.structure;

import lock14.algebra.group.CyclicGroup;
import lock14.algebra.group.Permutation;
import lock14.algebra.group.SymmetricGroup;
import lock14.algebra.laws.FiniteGroupLaws;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class DirectProductGroupTest extends FiniteGroupLaws<Pair<Integer, Permutation>> {

    private final CyclicGroup c3 = new CyclicGroup(3);
    private final SymmetricGroup s3 = new SymmetricGroup(3);
    private final FiniteGroup<Pair<Integer, Permutation>> product = DirectProduct.finiteGroup(c3, s3);

    @Override
    protected FiniteGroup<Pair<Integer, Permutation>> finiteGroup() {
        return product;
    }

    @Provide
    @Override
    public Arbitrary<Pair<Integer, Permutation>> elements() {
        return Arbitraries.of(product.elements());
    }

    @Test
    public void testOrderAbelianStatusAndProjections() {
        assertThat(product.order()).isEqualTo(18L);
        assertThat(product.isAbelian()).isFalse();

        AbelianGroup<Pair<Integer, Integer>> c2xC3 = DirectProduct.abelianGroup(new CyclicGroup(2), c3);
        assertThat(c2xC3.combine(new Pair<>(1, 2), new Pair<>(1, 2))).isEqualTo(new Pair<>(0, 1));

        Monoid<Pair<Integer, Permutation>> prodMonoid = DirectProduct.monoid(c3, s3);
        assertThat(prodMonoid.identity()).isEqualTo(product.identity());
        assertThat(java.util.stream.Stream.of(new Pair<>(1, s3.identity()), new Pair<>(2, s3.identity()))
            .gather(prodMonoid.scanGatherer())
            .toList()).containsExactly(new Pair<>(1, s3.identity()), new Pair<>(0, s3.identity()));

        Group<Pair<Integer, Permutation>> prodGroup = DirectProduct.group(c3, s3);
        Pair<Integer, Permutation> sample = new Pair<>(1, Permutation.transposition(3, 0, 1));
        assertThat(prodGroup.combine(sample, prodGroup.inverse(sample))).isEqualTo(prodGroup.identity());
        assertThat(sample.second().remove(sample.second())).isEqualTo(s3.identity());

        GroupHomomorphism<Pair<Integer, Permutation>, Integer> proj1 = Pair::first;
        GroupHomomorphism<Integer, Integer> mod3Double = x -> (2 * x) % 3;
        GroupHomomorphism<Pair<Integer, Permutation>, Integer> composed = proj1.andThen(mod3Double);
        assertThat(proj1.isValidHomomorphism(product, c3)).isTrue();
        assertThat(composed.isValidHomomorphism(product, c3)).isTrue();
    }
}
