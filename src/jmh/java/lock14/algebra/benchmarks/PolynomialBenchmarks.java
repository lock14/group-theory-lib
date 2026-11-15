package lock14.algebra.benchmarks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import lock14.algebra.polynomial.Polynomial;
import lock14.algebra.polynomial.PolynomialEuclideanDomain;
import lock14.algebra.structure.EuclideanDomain;
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
 * JMH microbenchmarks for univariate polynomial multiplication, Euclidean long division,
 * and Horner evaluation over {@code Q[x]} in {@code lock14.algebra.polynomial}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 4, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class PolynomialBenchmarks {

    private static final int DEGREE_HIGH = 16;
    private static final int DEGREE_DIVISOR = 8;

    private PolynomialEuclideanDomain<Rational> qx;
    private Polynomial<Rational> polyA;
    private Polynomial<Rational> polyB;
    private Polynomial<Rational> divisor;
    private Rational evalPoint;

    @Setup(Level.Trial)
    public void setUp() {
        qx = new PolynomialEuclideanDomain<>(Rationals.FIELD);

        List<Rational> coeffsA = new ArrayList<>(DEGREE_HIGH + 1);
        List<Rational> coeffsB = new ArrayList<>(DEGREE_HIGH + 1);
        for (int i = 0; i <= DEGREE_HIGH; i++) {
            coeffsA.add(Rational.of((i % 2 == 0 ? 1L : -1L) * (i + 1L), (i % 4) + 1L));
            coeffsB.add(Rational.of((i * 3L + 1L) % 7L + 1L, (i % 3) + 1L));
        }
        polyA = Polynomial.of(Rationals.FIELD, coeffsA);
        polyB = Polynomial.of(Rationals.FIELD, coeffsB);

        List<Rational> coeffsDiv = new ArrayList<>(DEGREE_DIVISOR + 1);
        for (int i = 0; i < DEGREE_DIVISOR; i++) {
            coeffsDiv.add(Rational.of((i % 2 == 0 ? 1L : -1L) * (i + 1L), 2L));
        }
        coeffsDiv.add(Rational.ONE);
        divisor = Polynomial.of(Rationals.FIELD, coeffsDiv);

        evalPoint = Rational.of(3, 5);
    }

    @Benchmark
    public Polynomial<Rational> multiplyDegree16OverQ() {
        return polyA.multiply(polyB);
    }

    @Benchmark
    public EuclideanDomain.DivisionResult<Polynomial<Rational>> divideAndRemainderDegree16By8OverQ() {
        return qx.divideAndRemainder(polyA, divisor);
    }

    @Benchmark
    public Rational hornerEvaluateDegree16OverQ() {
        return polyA.evaluate(evalPoint);
    }
}
