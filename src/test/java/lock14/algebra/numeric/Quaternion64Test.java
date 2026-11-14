package lock14.algebra.numeric;

import lock14.algebra.exceptions.NonInvertibleElementException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class Quaternion64Test {

    @Test
    public void testSpatialAxisAngleRotationAndSlerp() {
        // 90-degree counterclockwise rotation around +Z maps (1, 0, 0) -> (0, 1, 0)
        Quaternion64 rotZ90 = Quaternion64.fromAxisAngle(0.0, 0.0, 1.0, Math.PI / 2.0);
        double[] rotated = rotZ90.rotateVector(1.0, 0.0, 0.0);
        assertThat(rotated[0]).isCloseTo(0.0, within(1e-12));
        assertThat(rotated[1]).isCloseTo(1.0, within(1e-12));
        assertThat(rotated[2]).isCloseTo(0.0, within(1e-12));

        // Halfway SLERP between identity (0 deg) and 90 deg around +Z is 45 deg around +Z
        Quaternion64 mid = Quaternion64.ONE.slerp(rotZ90, 0.5);
        double[] midVec = mid.rotate(1.0, 0.0, 0.0);
        double invSqrt2 = Math.sqrt(0.5);
        assertThat(midVec[0]).isCloseTo(invSqrt2, within(1e-12));
        assertThat(midVec[1]).isCloseTo(invSqrt2, within(1e-12));
        assertThat(midVec[2]).isCloseTo(0.0, within(1e-12));
    }

    @Test
    public void testExtremeScaleNormReciprocalAndFormatting() {
        Quaternion64 huge = Quaternion64.of(1e200, 2e200, -1e200, 2e200);
        assertThat(huge.abs()).isCloseTo(Math.sqrt(10.0) * 1e200, within(1e186));
        assertThat(huge.multiply(huge.reciprocal()).approxEquals(Quaternion64.ONE, 1e-12)).isTrue();

        Quaternion64 tiny = Quaternion64.of(1e-200, 2e-200, -1e-200, 2e-200);
        assertThat(tiny.abs()).isCloseTo(Math.sqrt(10.0) * 1e-200, within(1e-214));
        assertThat(tiny.multiply(tiny.reciprocal()).approxEquals(Quaternion64.ONE, 1e-12)).isTrue();

        assertThat(Quaternion64.scalar(Double.POSITIVE_INFINITY).reciprocal()).isEqualTo(Quaternion64.ZERO);
        assertThatThrownBy(Quaternion64.ZERO::reciprocal).isInstanceOf(NonInvertibleElementException.class);
        assertThat(Quaternion64.of(1.0, -2.0, 3.0, -4.0).toString()).isEqualTo("1.0 - 2.0i + 3.0j - 4.0k");
    }
}
