package lock14.algebra.structure;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import lock14.algebra.group.CyclicGroup;
import lock14.algebra.group.Permutation;
import lock14.algebra.group.SymmetricGroup;
import lock14.algebra.laws.FiniteGroupLaws;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(product.order()).isEqualTo(BigInteger.valueOf(18));
        assertThat(product.isAbelian()).isFalse();
        assertThat(DirectProduct.finiteGroup(new CyclicGroup(2), c3).isAbelian()).isTrue();
        assertThat(DirectProduct.finiteGroup(s3, new CyclicGroup(2)).isAbelian()).isFalse();

        AbelianGroup<Pair<Integer, Integer>> c2xC3 = DirectProduct.abelianGroup(new CyclicGroup(2), c3);
        assertThat(c2xC3.identity()).isEqualTo(Pair.of(0, 0));
        assertThat(c2xC3.combine(Pair.of(1, 2), Pair.of(1, 2))).isEqualTo(Pair.of(0, 1));
        assertThat(c2xC3.inverse(Pair.of(1, 2))).isEqualTo(Pair.of(1, 1));

        Monoid<Pair<Integer, Permutation>> prodMonoid = DirectProduct.monoid(c3, s3);
        assertThat(prodMonoid.identity()).isEqualTo(product.identity());
        assertThat(Stream.of(Pair.of(1, s3.identity()), Pair.of(2, s3.identity()))
            .gather(prodMonoid.scanGatherer())
            .toList()).containsExactly(Pair.of(1, s3.identity()), Pair.of(0, s3.identity()));

        Group<Pair<Integer, Permutation>> prodGroup = DirectProduct.group(c3, s3);
        Pair<Integer, Permutation> sample = Pair.of(1, Permutation.transposition(3, 0, 1));
        assertThat(prodGroup.combine(sample, prodGroup.inverse(sample))).isEqualTo(prodGroup.identity());
        assertThat(sample.second().remove(sample.second())).isEqualTo(s3.identity());

        assertThat(product.contains(Pair.of(0, s3.identity()))).isTrue();
        assertThat(product.contains(Pair.of(99, s3.identity()))).isFalse();
        assertThat(product.contains(Pair.of(0, Permutation.identity(4)))).isFalse();

        assertThat(product.elements().isEmpty()).isFalse();
        assertThat(product.elements().contains(Pair.of(0, s3.identity()))).isTrue();
        assertThat(product.elements().contains(Pair.of(99, s3.identity()))).isFalse();
        assertThat(product.elements().contains(Pair.of(0, Permutation.identity(4)))).isFalse();
        assertThat(product.elements().contains("not a pair")).isFalse();

        FiniteGroup<Pair<Integer, Integer>> trivialProd =
            DirectProduct.finiteGroup(new CyclicGroup(1), new CyclicGroup(1));
        Iterator<Pair<Integer, Integer>> it = trivialProd.elements().iterator();
        assertThat(it.next()).isEqualTo(Pair.of(0, 0));
        assertThat(it.hasNext()).isFalse();
        assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);

        GroupHomomorphism<Pair<Integer, Permutation>, Integer> proj1 = Pair::first;
        GroupHomomorphism<Integer, Integer> mod3Double = x -> (2 * x) % 3;
        GroupHomomorphism<Pair<Integer, Permutation>, Integer> composed = proj1.andThen(mod3Double);
        assertThat(proj1.isValidHomomorphism(product, c3)).isTrue();
        assertThat(composed.isValidHomomorphism(product, c3)).isTrue();

        SymmetricGroup s15 = new SymmetricGroup(15);
        CyclicGroup cMillion = new CyclicGroup(1_000_000);
        FiniteGroup<Pair<Permutation, Integer>> hugeProduct = DirectProduct.finiteGroup(s15, cMillion);
        assertThat(hugeProduct.order()).isEqualTo(s15.order().multiply(cMillion.order()));
        assertThat(hugeProduct.isAbelian()).isFalse();
        assertThat(hugeProduct.elements().stream().limit(5).toList()).hasSize(5);
    }
}
