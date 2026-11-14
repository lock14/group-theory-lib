package lock14.algebra.linear;

import java.util.List;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class VectorTest {

    @Test
    public void testVectorImmutabilityAndBasis() {
        CoordinateVectorSpace<Rational> r3 = new CoordinateVectorSpace<>(3, Rationals.FIELD);
        List<Vector<Rational>> basis = r3.standardBasis();
        assertThat(basis).hasSize(3);
        assertThat(basis.get(0)).isEqualTo(r3.of(Rational.ONE, Rational.ZERO, Rational.ZERO));

        Rational[] raw = {Rational.ONE, Rational.TWO};
        Vector<Rational> v = Vector.of(Rationals.FIELD, raw);
        raw[0] = Rational.TEN;
        assertThat(v.get(0)).isEqualTo(Rational.ONE);
        assertThat(v.toList()).containsExactly(Rational.ONE, Rational.TWO);

        Vector<Rational> updated = v.with(1, Rational.TEN);
        assertThat(updated).isEqualTo(Vector.of(Rationals.FIELD, Rational.ONE, Rational.TEN));
        assertThat(v.get(1)).isEqualTo(Rational.TWO);
    }

    @Test
    public void testDotSubtractAndMatrixVectorProducts() {
        var f = Rationals.FIELD;
        Vector<Rational> v1 = Vector.of(f, Rational.of(1), Rational.of(2), Rational.of(3));
        Vector<Rational> v2 = Vector.of(f, Rational.of(4), Rational.of(5), Rational.of(6));

        assertThat(v2.subtract(v1)).isEqualTo(Vector.of(f, Rational.of(3), Rational.of(3), Rational.of(3)));
        assertThat(v1.dot(v2)).isEqualTo(Rational.of(32));

        RectangularMatrix<Rational> a = RectangularMatrix.of(2, 3, f,
            Rational.of(1), Rational.of(0), Rational.of(2),
            Rational.of(-1), Rational.of(3), Rational.of(1));
        assertThat(a.multiply(v1)).isEqualTo(Vector.of(f, Rational.of(7), Rational.of(8)));

        Vector<Rational> u = Vector.of(f, Rational.of(2), Rational.of(3));
        assertThat(u.multiply(a)).isEqualTo(Vector.of(f, Rational.of(-1), Rational.of(9), Rational.of(7)));

        assertThatThrownBy(() -> a.multiply(u))
            .isInstanceOf(DimensionMismatchException.class);
    }
}
