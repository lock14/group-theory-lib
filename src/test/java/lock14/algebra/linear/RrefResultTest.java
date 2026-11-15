package lock14.algebra.linear;

import java.util.ArrayList;
import java.util.List;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RrefResultTest {

    @Test
    public void testRrefResultValidationDefensiveCopyAndAccessors() {
        RectangularMatrix<Rational> reduced = RectangularMatrix.of(2, 4, Rationals.FIELD,
            Rational.ONE, Rational.TWO, Rational.ZERO, Rational.of(3),
            Rational.ZERO, Rational.ZERO, Rational.ONE, Rational.of(-1));

        List<Integer> mutablePivots = new ArrayList<>(List.of(0, 2));
        RrefResult<Rational> result = new RrefResult<>(reduced, mutablePivots, 2, 2);
        mutablePivots.add(3);

        assertThat(result.reduced()).isEqualTo(reduced);
        assertThat(result.pivotColumns()).containsExactly(0, 2);
        assertThat(result.rank()).isEqualTo(2);
        assertThat(result.nullity()).isEqualTo(2);
        assertThatThrownBy(() -> result.pivotColumns().add(1))
            .isInstanceOf(UnsupportedOperationException.class);

        RrefResult<Rational> same = new RrefResult<>(reduced, List.of(0, 2), 2, 2);
        assertThat(result).isEqualTo(same);
        assertThat(result.hashCode()).isEqualTo(same.hashCode());
        assertThat(result.toString()).contains("rank=2", "nullity=2");

        assertThatThrownBy(() -> new RrefResult<>(null, List.of(0, 2), 2, 2))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RrefResult<>(reduced, null, 2, 2))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RrefResult<>(reduced, List.of(), -1, 5))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RrefResult<>(reduced, List.of(0, 1, 2, 3), 5, -1))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RrefResult<>(reduced, List.of(0), 1, 2))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
