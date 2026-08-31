package lock14.algebra.linear;

import org.junit.jupiter.api.Test;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.RationalField;
import static org.assertj.core.api.Assertions.assertThat;

public class NativeDoubleMatrixTest {

    @Test
    public void testOffHeapMatrixMultiplication() {
        try (NativeDoubleMatrix a = new NativeDoubleMatrix(2, 2);
             NativeDoubleMatrix b = new NativeDoubleMatrix(2, 2)) {

            a.set(0, 0, 1.0);
            a.set(0, 1, 2.0);
            a.set(1, 0, 3.0);
            a.set(1, 1, 4.0);

            b.set(0, 0, 2.0);
            b.set(0, 1, 0.0);
            b.set(1, 0, 1.0);
            b.set(1, 1, 2.0);

            NativeDoubleMatrix c = a.multiply(b);

            // [1 2] * [2 0] = [ 4  4]
            // [3 4]   [1 2]   [10  8]
            assertThat(c.get(0, 0)).isEqualTo(4.0);
            assertThat(c.get(0, 1)).isEqualTo(4.0);
            assertThat(c.get(1, 0)).isEqualTo(10.0);
            assertThat(c.get(1, 1)).isEqualTo(8.0);
        }
    }

    @Test
    public void testVectorOperations() {
        RationalField f = RationalField.INSTANCE;
        Vector<Rational> v1 = Vector.of(f, Rational.of(1), Rational.of(2), Rational.of(3));
        Vector<Rational> v2 = Vector.of(f, Rational.of(4), Rational.of(5), Rational.of(6));

        Vector<Rational> sum = v1.add(v2);
        assertThat(sum).isEqualTo(Vector.of(f, Rational.of(5), Rational.of(7), Rational.of(9)));

        // Dot product: 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        Rational dot = v1.dot(v2);
        assertThat(dot).isEqualTo(Rational.of(32));

        Vector<Rational> scaled = v1.scale(Rational.of(3));
        assertThat(scaled).isEqualTo(Vector.of(f, Rational.of(3), Rational.of(6), Rational.of(9)));
    }
}
