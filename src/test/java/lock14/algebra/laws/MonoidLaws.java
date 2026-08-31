package lock14.algebra.laws;

import java.util.List;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import lock14.algebra.structure.Monoid;
import lock14.algebra.structure.Semigroup;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class MonoidLaws<T> extends SemigroupLaws<T> {

    protected abstract Monoid<T> monoid();

    @Override
    protected Semigroup<T> semigroup() {
        return monoid();
    }

    @Property
    public void leftIdentity(@ForAll("elements") T a) {
        Monoid<T> m = monoid();
        assertThat(m.combine(m.identity(), a)).isEqualTo(a);
    }

    @Property
    public void rightIdentity(@ForAll("elements") T a) {
        Monoid<T> m = monoid();
        assertThat(m.combine(a, m.identity())).isEqualTo(a);
    }

    @Property
    public void powerZero(@ForAll("elements") T a) {
        Monoid<T> m = monoid();
        assertThat(m.power(a, 0)).isEqualTo(m.identity());
    }

    @Provide
    public Arbitrary<List<T>> elementLists() {
        return elements().list().ofMinSize(0).ofMaxSize(10);
    }

    @Property
    public void collectorConsistency(@ForAll("elementLists") List<T> list) {
        Monoid<T> m = monoid();
        T folded = m.fold(list);
        T collected = list.stream().collect(m.collector());
        assertThat(collected).isEqualTo(folded);
    }

    @Property
    public void gathererScanConsistency(@ForAll("elementLists") List<T> list) {
        Monoid<T> m = monoid();
        List<T> scanned = list.stream().gather(m.scanGatherer()).toList();
        if (list.isEmpty()) {
            assertThat(scanned).isEmpty();
        } else {
            assertThat(scanned.getLast()).isEqualTo(m.fold(list));
        }
    }
}
