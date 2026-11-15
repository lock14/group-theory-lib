package lock14.algebra.structure;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.List;
import lock14.algebra.group.CyclicGroup;
import lock14.algebra.group.Permutation;
import lock14.algebra.group.SymmetricGroup;
import org.junit.jupiter.api.Test;

class StructureExamplesTest {

    @Test
    void directProductAndScanGathererExample() {
        // @start region="direct-product-and-scan"
        CyclicGroup c3 = new CyclicGroup(3);
        SymmetricGroup s3 = new SymmetricGroup(3);
        FiniteGroup<Pair<Integer, Permutation>> product = DirectProduct.finiteGroup(c3, s3);

        Pair<Integer, Permutation> g = Pair.of(1, Permutation.transposition(3, 0, 1));
        List<Pair<Integer, Permutation>> prefixes = List.of(g, g, g).stream()
                .gather(product.scanGatherer())
                .toList();
        // @end

        assertThat(product.order()).isEqualTo(BigInteger.valueOf(18));
        assertThat(product.elementOrder(g)).isEqualTo(BigInteger.valueOf(6));
        assertThat(prefixes).hasSize(3);
        assertThat(prefixes.get(2)).isEqualTo(Pair.of(0, Permutation.transposition(3, 0, 1)));
    }

    @Test
    void groupHomomorphismCompositionExample() {
        // @start region="homomorphism-composition"
        SymmetricGroup s4 = new SymmetricGroup(4);
        CyclicGroup c2 = new CyclicGroup(2);
        GroupHomomorphism<Permutation, Integer> parity = p -> p.isEven() ? 0 : 1;

        boolean valid = parity.isValidHomomorphism(s4, c2);
        int kernelOrder = parity.kernelElements(s4, c2).size();
        // @end

        assertThat(valid).isTrue();
        assertThat(kernelOrder).isEqualTo(12);
    }
}
