package lock14.algebra.numbers;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.laws.ApproximateFieldLaws;
import lock14.algebra.structure.Field;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RealFieldTest extends ApproximateFieldLaws<Double> {

    @Override
    protected Field<Double> field() {
        return RealField.INSTANCE;
    }

    @Override
    protected double distance(Double a, Double b) {
        return Math.abs(a - b);
    }

    @Override
    protected double tolerance() {
        return 1e-7;
    }

    @Provide
    Arbitrary<Double> elements() {
        return Arbitraries.doubles().between(-1000.0, 1000.0);
    }

    @Provide
    Arbitrary<Double> nonZeroElements() {
        return elements().filter(d -> Math.abs(d) > 1e-4);
    }

    @Test
    public void testBasicFieldOperations() {
        RealField f = RealField.INSTANCE;
        assertThat(f.isZero(0.0)).isTrue();
        assertThat(f.isZero(-0.0)).isTrue();
        assertThat(f.isOne(1.0)).isTrue();
        assertThat(f.subtract(5.5, 2.0)).isEqualTo(3.5);
        assertThat(f.divide(9.0, 2.0)).isEqualTo(4.5);
        assertThatThrownBy(() -> f.divide(1.0, 0.0))
                .isInstanceOf(ArithmeticException.class);
    }
}
