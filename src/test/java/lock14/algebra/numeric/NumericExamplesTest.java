package lock14.algebra.numeric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class NumericExamplesTest {

    @Test
    void offHeapSolveAndQuaternionRotationExample() throws Exception {
        // @start region="ffm-solve-and-rotation"
        Quaternion64 rotZ90 = Quaternion64.fromAxisAngle(0.0, 0.0, 1.0, Math.PI / 2.0);
        double[] rotated = rotZ90.rotate(1.0, 0.0, 0.0);

        double[] solution;
        try (Float64Matrix m = Float64Matrix.of(new double[][]{{1e-20, 1.0}, {1.0, 1.0}})) {
            solution = ScopedValue.where(NumericContext.EPSILON, 1e-12)
                    .call(() -> m.solve(new double[]{1.0, 2.0}));
        }
        // @end

        assertThat(rotated[0]).isCloseTo(0.0, within(1e-12));
        assertThat(rotated[1]).isCloseTo(1.0, within(1e-12));
        assertThat(solution[0]).isCloseTo(1.0, within(1e-9));
        assertThat(solution[1]).isCloseTo(1.0, within(1e-9));
    }
}
