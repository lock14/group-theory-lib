package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.Field;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class FieldLaws<T> extends CommutativeRingLaws<T> {

    protected abstract Field<T> field();

    @Override
    protected CommutativeRing<T> commutativeRing() {
        return field();
    }

    @Property
    public void nonZeroMultiplicativeInverse(@ForAll("nonZeroElements") T a) {
        Field<T> f = field();
        assertThat(f.multiply(a, f.reciprocal(a))).isEqualTo(f.one());
        assertThat(f.multiply(f.reciprocal(a), a)).isEqualTo(f.one());
    }

    @Property
    public void divisionConsistency(@ForAll("elements") T a, @ForAll("nonZeroElements") T b) {
        Field<T> f = field();
        assertThat(f.divide(a, b)).isEqualTo(f.multiply(a, f.reciprocal(b)));
    }

    @Test
    public void zeroReciprocalThrows() {
        Field<T> f = field();
        assertThatThrownBy(() -> f.reciprocal(f.zero()))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    public void divisionByZeroThrows() {
        Field<T> f = field();
        assertThatThrownBy(() -> f.divide(f.one(), f.zero()))
                .isInstanceOf(ArithmeticException.class);
    }
}
