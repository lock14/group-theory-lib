package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import lock14.algebra.structure.Field;
import lock14.algebra.structure.Module;
import lock14.algebra.structure.VectorSpace;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class VectorSpaceLaws<V, F> extends ModuleLaws<V, F> {

    protected abstract VectorSpace<V, F> vectorSpace();

    @Override
    protected Module<V, F> module() {
        return vectorSpace();
    }

    @Property
    public void inverseScalarScalingRestoresVector(@ForAll("nonZeroScalars") F c, @ForAll("vectors") V v) {
        VectorSpace<V, F> vs = vectorSpace();
        Field<F> f = vs.scalarField();
        assertThat(vs.scale(f.reciprocal(c), vs.scale(c, v))).isEqualTo(v);
    }
}
