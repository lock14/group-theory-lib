package lock14.algebra.numeric;

import lock14.algebra.laws.ApproximateFieldLaws;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Float64FieldTest extends ApproximateFieldLaws<Double> {

    @Override
    protected ApproximateField<Double> field() {
        return Float64Field.INSTANCE;
    }

    @Override
    protected double tolerance() {
        return 1e-8;
    }

    @Provide
    Arbitrary<Double> elements() {
        return Arbitraries.doubles().between(-1_000.0, 1_000.0);
    }

    @Provide
    Arbitrary<Double> nonZeroElements() {
        return elements().filter(d -> Math.abs(d) > 1e-4);
    }

    @Test
    public void testSignedZeroCanonicalizationAndScopedEpsilon() {
        Float64Field f = Float64Field.INSTANCE;
        assertThat(f.negate(0.0)).isEqualTo(0.0);
        assertThat(Double.doubleToRawLongBits(f.negate(0.0))).isZero();

        assertThat(f.approxEquals(1.0, 1.0 + 1e-6)).isFalse();
        boolean withinCustomScope = ScopedValue.where(NumericContext.EPSILON, 1e-5)
            .call(() -> f.approxEquals(1.0, 1.0 + 1e-6));
        assertThat(withinCustomScope).isTrue();

        assertThatThrownBy(() -> ScopedValue.where(NumericContext.EPSILON, -0.1)
            .call(NumericContext::currentEpsilon))
            .isInstanceOf(IllegalStateException.class);
    }
}
