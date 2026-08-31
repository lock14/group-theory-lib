package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import lock14.algebra.structure.Magma;
import lock14.algebra.structure.Semigroup;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class SemigroupLaws<T> extends MagmaLaws<T> {

    protected abstract Semigroup<T> semigroup();

    @Override
    protected Magma<T> magma() {
        return semigroup();
    }

    @Property
    public void associativity(@ForAll("elements") T a, @ForAll("elements") T b, @ForAll("elements") T c) {
        Semigroup<T> sg = semigroup();
        T leftFirst = sg.combine(sg.combine(a, b), c);
        T rightFirst = sg.combine(a, sg.combine(b, c));
        assertThat(leftFirst).isEqualTo(rightFirst);
    }

    @Property
    public void repeatConsistency(@ForAll("elements") T a, @ForAll @IntRange(min = 1, max = 5) int n) {
        Semigroup<T> sg = semigroup();
        T acc = a;
        for (int i = 1; i < n; i++) {
            acc = sg.combine(acc, a);
        }
        assertThat(sg.repeat(a, n)).isEqualTo(acc);
    }
}
