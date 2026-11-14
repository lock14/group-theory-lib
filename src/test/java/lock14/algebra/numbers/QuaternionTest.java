package lock14.algebra.numbers;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import lock14.algebra.laws.RingLaws;
import lock14.algebra.structure.Ring;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class QuaternionTest extends RingLaws<Quaternion> {

    @Override
    protected Ring<Quaternion> ring() {
        return Quaternion.ring();
    }

    @Provide
    Arbitrary<Quaternion> elements() {
        Arbitrary<Integer> coords = Arbitraries.integers().between(-10, 10);
        return coords.list().ofSize(4)
                .map(c -> Quaternion.of(c.get(0), c.get(1), c.get(2), c.get(3)));
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
