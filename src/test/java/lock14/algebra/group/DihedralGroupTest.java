package lock14.algebra.group;

import java.math.BigInteger;
import java.util.Iterator;
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
        assertThat(d6.polygonSides()).isEqualTo(6);
        assertThat(d6.order()).isEqualTo(BigInteger.valueOf(12));
        assertThat(d6.isAbelian()).isFalse();

        DihedralGroup.Element r = d6.rotationGenerator();
        DihedralGroup.Element s = d6.reflectionGenerator();

        // Presentation: r^6 = e, s^2 = e, s * r * s = r^-1
        assertThat(d6.power(r, 6)).isEqualTo(d6.identity());
        assertThat(d6.power(s, 2)).isEqualTo(d6.identity());
        assertThat(d6.combine(d6.combine(s, r), s)).isEqualTo(d6.inverse(r));

        // D_1 and D_2 (Klein 4-group) are abelian
        DihedralGroup d1 = new DihedralGroup(1);
        assertThat(d1.polygonSides()).isEqualTo(1);
        assertThat(d1.order()).isEqualTo(BigInteger.TWO);
        assertThat(d1.isAbelian()).isTrue();
        assertThat(d1.generators()).containsExactly(d1.reflectionGenerator());

        assertThat(new DihedralGroup(2).isAbelian()).isTrue();

        DihedralGroup dHuge = new DihedralGroup(1_000_000_000);
        assertThat(dHuge.order()).isEqualTo(BigInteger.valueOf(2_000_000_000L));
        assertThat(dHuge.isAbelian()).isFalse();
        assertThat(dHuge.elements().stream().limit(3).toList()).hasSize(3);
    }

    @Test
    public void testValidationFormattingMembershipAndEquality() {
        assertThatThrownBy(() -> new DihedralGroup(0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DihedralGroup(-3))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DihedralGroup.Element(-1, false))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(new DihedralGroup.Element(0, false).toString()).isEqualTo("e");
        assertThat(new DihedralGroup.Element(0, true).toString()).isEqualTo("s");
        assertThat(new DihedralGroup.Element(1, false).toString()).isEqualTo("r");
        assertThat(new DihedralGroup.Element(1, true).toString()).isEqualTo("rs");
        assertThat(new DihedralGroup.Element(2, false).toString()).isEqualTo("r^2");
        assertThat(new DihedralGroup.Element(2, true).toString()).isEqualTo("r^2s");

        DihedralGroup.Element outside = new DihedralGroup.Element(6, false);
        assertThat(d6.contains(d6.identity())).isTrue();
        assertThat(d6.contains(outside)).isFalse();
        assertThatThrownBy(() -> d6.combine(d6.identity(), outside))
            .isInstanceOf(IncompatibleAlgebraException.class);
        assertThatThrownBy(() -> d6.inverse(outside))
            .isInstanceOf(IncompatibleAlgebraException.class);

        assertThat(d6.elements().isEmpty()).isFalse();
        assertThat(d6.elements().contains(d6.identity())).isTrue();
        assertThat(d6.elements().contains(outside)).isFalse();
        assertThat(d6.elements().contains("not an element")).isFalse();

        Iterator<DihedralGroup.Element> it = new DihedralGroup(1).elements().iterator();
        assertThat(it.next()).isEqualTo(new DihedralGroup.Element(0, false));
        assertThat(it.next()).isEqualTo(new DihedralGroup.Element(0, true));
        assertThat(it.hasNext()).isFalse();
        assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);

        DihedralGroup d6Copy = new DihedralGroup(6);
        assertThat(d6).isEqualTo(d6Copy);
        assertThat(d6).hasSameHashCodeAs(d6Copy);
        assertThat(d6).isNotEqualTo(new DihedralGroup(4));
        assertThat(d6.equals("D_6")).isFalse();
        assertThat(d6.equals(null)).isFalse();
        assertThat(d6.toString()).isEqualTo("D_6");
    }
}
