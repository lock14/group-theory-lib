package lock14.algebra.numeric;

import java.lang.foreign.Arena;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

/**
 * JMH microbenchmarks for off-heap Foreign Function &amp; Memory {@link Float64Matrix} linear algebra
 * and {@link Quaternion64} spatial rotations / SLERP in {@code lock14.algebra.numeric}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 4, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class NumericBenchmarks {

    private static final int MATRIX_DIM = 64;

    private Arena arena;
    private Float64SquareMatrix mat64A;
    private Float64SquareMatrix mat64B;
    private double[] rhs64;
    private Quaternion64 rotStart;
    private Quaternion64 rotEnd;

    @Setup(Level.Trial)
    public void setUp() {
        arena = Arena.ofConfined();
        mat64A = new Float64SquareMatrix(MATRIX_DIM, arena);
        mat64B = new Float64SquareMatrix(MATRIX_DIM, arena);
        rhs64 = new double[MATRIX_DIM];

        for (int r = 0; r < MATRIX_DIM; r++) {
            rhs64[r] = 1.0 + 0.125 * r;
            for (int c = 0; c < MATRIX_DIM; c++) {
                double valA = (r == c)
                        ? (MATRIX_DIM + 4.0 + r * 0.25)
                        : (((r * 7 + c * 3) % 11) - 5) * 0.1;
                double valB = (r == c)
                        ? (MATRIX_DIM + 2.0 + c * 0.25)
                        : (((r * 5 + c * 9) % 11) - 5) * 0.1;
                mat64A.set(r, c, valA);
                mat64B.set(r, c, valB);
            }
        }

        rotStart = Quaternion64.fromAxisAngle(1.0, 2.0, 3.0, 0.35);
        rotEnd = Quaternion64.fromAxisAngle(-2.0, 1.0, 0.5, 1.85);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (arena != null) {
            arena.close();
        }
    }

    @Benchmark
    public double ffmMultiply64x64() {
        try (Float64Matrix product = mat64A.multiply(mat64B)) {
            return product.get(0, 0);
        }
    }

    @Benchmark
    public double[] ffmSolve64x64() {
        return mat64A.solve(rhs64);
    }

    @Benchmark
    public double[] quaternion64RotateVector() {
        return rotStart.rotate(1.25, -0.75, 2.5);
    }

    @Benchmark
    public Quaternion64 quaternion64Slerp() {
        return rotStart.slerp(rotEnd, 0.42);
    }
}
