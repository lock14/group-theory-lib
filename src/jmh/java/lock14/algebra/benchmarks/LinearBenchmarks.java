package lock14.algebra.benchmarks;

import java.util.concurrent.TimeUnit;
import lock14.algebra.linear.RectangularMatrix;
import lock14.algebra.linear.RrefResult;
import lock14.algebra.linear.SquareMatrix;
import lock14.algebra.linear.Vector;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
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
import org.openjdk.jmh.annotations.Warmup;

/**
 * JMH microbenchmarks for exact rational matrix multiplication, determinant, LU linear solve,
 * and rectangular RREF computation in {@code lock14.algebra.linear}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 4, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class LinearBenchmarks {

    private static final int SQUARE_DIM = 8;
    private static final int RECT_ROWS = 8;
    private static final int RECT_COLS = 12;

    private SquareMatrix<Rational> matA;
    private SquareMatrix<Rational> matB;
    private Vector<Rational> rhs8;
    private RectangularMatrix<Rational> rect8x12;

    @Setup(Level.Trial)
    public void setUp() {
        Rational[][] entriesA = new Rational[SQUARE_DIM][SQUARE_DIM];
        Rational[][] entriesB = new Rational[SQUARE_DIM][SQUARE_DIM];
        Rational[] rhsEntries = new Rational[SQUARE_DIM];
        for (int r = 0; r < SQUARE_DIM; r++) {
            rhsEntries[r] = Rational.of(r + 1L);
            for (int c = 0; c < SQUARE_DIM; c++) {
                entriesA[r][c] = (r == c)
                        ? Rational.of(10L + r)
                        : Rational.of(((r * 3L + c) % 5L) - 2L);
                entriesB[r][c] = (r == c)
                        ? Rational.of(7L + c)
                        : Rational.of(((r + c * 2L) % 5L) - 2L);
            }
        }
        matA = SquareMatrix.of(Rationals.FIELD, entriesA);
        matB = SquareMatrix.of(Rationals.FIELD, entriesB);
        rhs8 = Vector.of(Rationals.FIELD, rhsEntries);

        Rational[][] rectEntries = new Rational[RECT_ROWS][RECT_COLS];
        for (int r = 0; r < RECT_ROWS; r++) {
            for (int c = 0; c < SQUARE_DIM; c++) {
                rectEntries[r][c] = entriesA[r][c];
            }
            for (int c = SQUARE_DIM; c < RECT_COLS; c++) {
                int srcA = c - SQUARE_DIM;
                int srcB = (srcA + 1) % SQUARE_DIM;
                rectEntries[r][c] = entriesA[r][srcA].subtract(entriesA[r][srcB]);
            }
        }
        rect8x12 = RectangularMatrix.of(Rationals.FIELD, rectEntries);
    }

    @Benchmark
    public SquareMatrix<Rational> squareMultiply8x8OverQ() {
        return matA.multiply(matB);
    }

    @Benchmark
    public Rational determinant8x8OverQ() {
        return matA.determinant();
    }

    @Benchmark
    public Vector<Rational> luSolve8x8OverQ() {
        return matA.solve(rhs8);
    }

    @Benchmark
    public RrefResult<Rational> rref8x12OverQ() {
        return rect8x12.rref();
    }
}
