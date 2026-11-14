package lock14.algebra.laws;

import lock14.algebra.structure.Field;
import lock14.algebra.structure.LeftModule;
import lock14.algebra.structure.VectorSpace;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class VectorSpaceLaws<V, F> extends ModuleLaws<V, F> {

    protected abstract VectorSpace<V, F> vectorSpace();

    @Override
    protected LeftModule<V, F> module() {
        return vectorSpace();
    }

    @Property
    public void inverseScalarScalingRestoresVector(@ForAll("nonZeroScalars") F c, @ForAll("vectors") V v) {
        VectorSpace<V, F> vs = vectorSpace();
        Field<F> f = vs.scalarField();
        assertThat(vs.scale(f.reciprocal(c), vs.scale(c, v))).isEqualTo(v);
    }
}
