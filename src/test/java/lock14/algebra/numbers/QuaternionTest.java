package lock14.algebra.numbers;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.laws.DivisionRingLaws;
import lock14.algebra.structure.DivisionRing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class QuaternionTest extends DivisionRingLaws<Quaternion> {

    @Override
    protected DivisionRing<Quaternion> divisionRing() {
        return Quaternion.ring();
    }

    @Override
    protected void assertDivisionEquals(Quaternion actual, Quaternion expected) {
        double scale = Math.max(1.0, Math.max(actual.abs(), expected.abs()));
        assertThat(actual.subtract(expected).abs() / scale).isLessThanOrEqualTo(1e-12);
    }

    @Provide
    Arbitrary<Quaternion> elements() {
        Arbitrary<Integer> coords = Arbitraries.integers().between(-10, 10);
        return coords.list().ofSize(4)
                .map(c -> Quaternion.of(c.get(0), c.get(1), c.get(2), c.get(3)));
    }

    @Provide
    Arbitrary<Quaternion> nonZeroElements() {
        return elements().filter(q -> !q.isZero());
    }

    @Test
    public void testHamiltonFundamentalFormula() {
        Quaternion i = Quaternion.I;
        Quaternion j = Quaternion.J;
        Quaternion k = Quaternion.K;

        Quaternion minusOne = Quaternion.scalar(-1.0);

        assertThat(i.multiply(i)).isEqualTo(minusOne);
        assertThat(j.multiply(j)).isEqualTo(minusOne);
        assertThat(k.multiply(k)).isEqualTo(minusOne);
        assertThat(i.multiply(j).multiply(k)).isEqualTo(minusOne);

        // Cyclic products
        assertThat(i.multiply(j)).isEqualTo(k);
        assertThat(j.multiply(i)).isEqualTo(k.negate());

        assertThat(j.multiply(k)).isEqualTo(i);
        assertThat(k.multiply(j)).isEqualTo(i.negate());

        assertThat(k.multiply(i)).isEqualTo(j);
        assertThat(i.multiply(k)).isEqualTo(j.negate());
    }

    @Test
    public void testInversionAndNorm() {
        Quaternion q = Quaternion.of(1, 2, 3, 4);
        assertThat(q.normSq()).isEqualTo(30.0);
        assertThat(q.abs()).isCloseTo(Math.sqrt(30.0), within(1e-9));

        Quaternion inv = q.reciprocal();
        Quaternion prod1 = q.multiply(inv);
        Quaternion prod2 = inv.multiply(q);

        assertThat(prod1.w()).isCloseTo(1.0, within(1e-9));
        assertThat(prod1.x()).isCloseTo(0.0, within(1e-9));
        assertThat(prod1.y()).isCloseTo(0.0, within(1e-9));
        assertThat(prod1.z()).isCloseTo(0.0, within(1e-9));

        assertThat(prod2.w()).isCloseTo(1.0, within(1e-9));
        assertThat(prod2.x()).isCloseTo(0.0, within(1e-9));
        assertThat(prod2.y()).isCloseTo(0.0, within(1e-9));
        assertThat(prod2.z()).isCloseTo(0.0, within(1e-9));
    }

    @Test
    public void testZeroReciprocalThrows() {
        assertThatThrownBy(() -> Quaternion.ZERO.reciprocal())
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    public void testExtremeScaleNormAndReciprocalNoOverflowOrUnderflow() {
        Quaternion huge = Quaternion.of(1e200, 2e200, -1e200, 2e200);
        // sqrt(1 + 4 + 1 + 4) * 1e200 = sqrt(10) * 1e200
        assertThat(huge.abs()).isCloseTo(Math.sqrt(10.0) * 1e200, within(1e186));
        Quaternion hugeInv = huge.reciprocal();
        Quaternion hugeProdRight = huge.multiply(hugeInv);
        Quaternion hugeProdLeft = hugeInv.multiply(huge);
        assertThat(hugeProdRight.w()).isCloseTo(1.0, within(1e-12));
        assertThat(hugeProdRight.x()).isCloseTo(0.0, within(1e-12));
        assertThat(hugeProdRight.y()).isCloseTo(0.0, within(1e-12));
        assertThat(hugeProdRight.z()).isCloseTo(0.0, within(1e-12));
        assertThat(hugeProdLeft.w()).isCloseTo(1.0, within(1e-12));

        Quaternion tiny = Quaternion.of(1e-200, 2e-200, -1e-200, 2e-200);
        assertThat(tiny.abs()).isCloseTo(Math.sqrt(10.0) * 1e-200, within(1e-214));
        Quaternion tinyInv = tiny.reciprocal();
        Quaternion tinyProdRight = tiny.multiply(tinyInv);
        Quaternion tinyProdLeft = tinyInv.multiply(tiny);
        assertThat(tinyProdRight.w()).isCloseTo(1.0, within(1e-12));
        assertThat(tinyProdRight.x()).isCloseTo(0.0, within(1e-12));
        assertThat(tinyProdRight.y()).isCloseTo(0.0, within(1e-12));
        assertThat(tinyProdRight.z()).isCloseTo(0.0, within(1e-12));
        assertThat(tinyProdLeft.w()).isCloseTo(1.0, within(1e-12));

        assertThat(Quaternion.scalar(Double.MIN_VALUE).abs()).isEqualTo(Double.MIN_VALUE);
        assertThat(Quaternion.scalar(Double.POSITIVE_INFINITY).abs()).isEqualTo(Double.POSITIVE_INFINITY);
        assertThat(Quaternion.scalar(Double.POSITIVE_INFINITY).reciprocal()).isEqualTo(Quaternion.ZERO);
        assertThat(Double.isNaN(Quaternion.scalar(Double.NaN).abs())).isTrue();
    }

    @Test
    public void testToStringFormatting() {
        assertThat(Quaternion.ZERO.toString()).isEqualTo("0.0");
        assertThat(Quaternion.ONE.toString()).isEqualTo("1.0");
        assertThat(Quaternion.I.toString()).isEqualTo("i");
        assertThat(Quaternion.J.negate().toString()).isEqualTo("-j");
        assertThat(Quaternion.of(1.0, -2.0, 3.0, -4.0).toString())
                .isEqualTo("1.0 - 2.0i + 3.0j - 4.0k");
        assertThat(Quaternion.of(1.0, -1.0, 0.0, 1.0).toString())
                .isEqualTo("1.0 - i + k");
        assertThat(Quaternion.of(0.0, -2.5, 1.0, 0.0).toString())
                .isEqualTo("-2.5i + j");
    }
}
