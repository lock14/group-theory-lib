package lock14.algebra.laws;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import lock14.algebra.structure.Magma;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class MagmaLaws<T> {

    protected abstract Magma<T> magma();

    @Provide
    public abstract Arbitrary<T> elements();

    @Property
    public void closure(@ForAll("elements") T a, @ForAll("elements") T b) {
        T result = magma().combine(a, b);
        assertThat(result).isNotNull();
    }
}
