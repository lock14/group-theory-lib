package lock14.algebra.group;

import java.math.BigInteger;
import lock14.algebra.laws.FiniteGroupLaws;
import lock14.algebra.structure.FiniteGroup;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class DihedralGroupTest extends FiniteGroupLaws<DihedralGroup.Element> {

    private final DihedralGroup d6 = new DihedralGroup(6);

    @Override
    protected FiniteGroup<DihedralGroup.Element> finiteGroup() {
        return d6;
    }

    @Provide
    @Override
    public Arbitrary<DihedralGroup.Element> elements() {
        return Arbitraries.of(d6.elements());
    }

    @Test
    public void testPresentationRelationsCenterAndLazyLargePolygon() {
        assertThat(d6.order()).isEqualTo(BigInteger.valueOf(12));
        assertThat(d6.isAbelian()).isFalse();

        DihedralGroup.Element r = d6.rotationGenerator();
        DihedralGroup.Element s = d6.reflectionGenerator();

        // Presentation: r^6 = e, s^2 = e, s * r * s = r^-1
        assertThat(d6.power(r, 6)).isEqualTo(d6.identity());
        assertThat(d6.power(s, 2)).isEqualTo(d6.identity());
        assertThat(d6.combine(d6.combine(s, r), s)).isEqualTo(d6.inverse(r));

        // D_2 (Klein 4-group) is abelian
        assertThat(new DihedralGroup(2).isAbelian()).isTrue();

        DihedralGroup dHuge = new DihedralGroup(1_000_000_000);
        assertThat(dHuge.order()).isEqualTo(BigInteger.valueOf(2_000_000_000L));
        assertThat(dHuge.isAbelian()).isFalse();
        assertThat(dHuge.elements().stream().limit(3).toList()).hasSize(3);
    }
}
