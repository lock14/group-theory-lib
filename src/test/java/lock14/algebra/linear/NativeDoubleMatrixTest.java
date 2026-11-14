package lock14.algebra.linear;

import java.lang.foreign.Arena;
import org.junit.jupiter.api.Test;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.RationalField;
import static org.assertj.core.api.Assertions.assertThat;

public class NativeDoubleMatrixTest {

    @Test
    public void testOffHeapMatrixMultiplicationAndIndependentArenaLifecycle() {
        NativeDoubleMatrix outlived;
        try (NativeDoubleMatrix a = new NativeDoubleMatrix(2, 2);
             NativeDoubleMatrix b = new NativeDoubleMatrix(2, 2);
             NativeDoubleMatrix c = a.multiply(b);
             NativeDoubleMatrix sum = a.add(b)) {

            a.set(0, 0, 1.0);
            a.set(0, 1, 2.0);
            a.set(1, 0, 3.0);
            a.set(1, 1, 4.0);

            b.set(0, 0, 2.0);
            b.set(0, 1, 0.0);
            b.set(1, 0, 1.0);
            b.set(1, 1, 2.0);

            try (NativeDoubleMatrix prod = a.multiply(b);
                 NativeDoubleMatrix added = a.add(b)) {
                assertThat(prod.get(0, 0)).isEqualTo(4.0);
                assertThat(prod.get(0, 1)).isEqualTo(4.0);
                assertThat(prod.get(1, 0)).isEqualTo(10.0);
                assertThat(prod.get(1, 1)).isEqualTo(8.0);

                assertThat(added.get(0, 0)).isEqualTo(3.0);
                assertThat(added.get(1, 1)).isEqualTo(6.0);
            }

            outlived = a.multiply(b);
            // Idempotent close check on derived matrix c
            c.close();
            sum.close();
        }

        // Parent matrices a and b are now closed, yet outlived remains valid in its own confined arena
        try (outlived) {
            assertThat(outlived.get(0, 0)).isEqualTo(4.0);
            assertThat(outlived.get(1, 1)).isEqualTo(8.0);
        }
    }

    @Test
    public void testSharedAndAutoArenaAllocationDoesNotDoubleClose() {
        try (Arena batchArena = Arena.ofConfined()) {
            try (NativeDoubleMatrix a = new NativeDoubleMatrix(2, 2, batchArena);
                 NativeDoubleMatrix b = new NativeDoubleMatrix(2, 2, batchArena);
                 NativeDoubleMatrix c = a.add(b, batchArena)) {
                a.set(0, 0, 5.0);
                b.set(0, 0, 7.0);
                try (NativeDoubleMatrix d = a.add(b, batchArena)) {
                    assertThat(d.get(0, 0)).isEqualTo(12.0);
                }
                assertThat(c.rows()).isEqualTo(2);
            }
            // batchArena is still alive until outer try-with-resources exits
            assertThat(batchArena.scope().isAlive()).isTrue();
        }

        Arena auto = Arena.ofAuto();
        try (NativeDoubleMatrix m = new NativeDoubleMatrix(2, 2, auto)) {
            m.set(1, 1, 42.0);
            assertThat(m.get(1, 1)).isEqualTo(42.0);
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
