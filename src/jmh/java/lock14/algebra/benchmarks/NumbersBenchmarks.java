package lock14.algebra.benchmarks;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import lock14.algebra.numbers.Complex;
import lock14.algebra.numbers.GaussianIntegers;
import lock14.algebra.numbers.Quaternion;
import lock14.algebra.numbers.QuaternionDivisionRing;
import lock14.algebra.numbers.Rational;
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
 * JMH microbenchmarks for exact rational arithmetic, Gaussian integer Euclidean division/GCD,
 * and rational Hamilton quaternion arithmetic in {@code lock14.algebra.numbers}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 4, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class NumbersBenchmarks {

    private Rational ratA;
    private Rational ratB;
    private Rational ratC;
    private GaussianIntegers gaussianDomain;
    private Complex<BigInteger> gaussA;
    private Complex<BigInteger> gaussB;
    private Quaternion<Rational> quatA;
    private Quaternion<Rational> quatB;

    @Setup(Level.Trial)
    public void setUp() {
        ratA = Rational.of(355, 113);
        ratB = Rational.of(103993, 33102);
        ratC = Rational.of(-22, 7);

        gaussianDomain = GaussianIntegers.DOMAIN;
        gaussA = gaussianDomain.of(241, 113);
        gaussB = gaussianDomain.of(37, -19);

        QuaternionDivisionRing<Rational> hq = QuaternionDivisionRing.overRationals();
        quatA = hq.of(Rational.of(3, 2), Rational.of(-5, 3), Rational.of(7, 4), Rational.of(11, 6));
        quatB = hq.of(Rational.of(2, 5), Rational.of(4, 7), Rational.of(-3, 8), Rational.of(1, 3));
    }

    @Benchmark
    public Rational rationalFusedArithmetic() {
        return ratA.multiply(ratB).add(ratC).reciprocal();
    }

    @Benchmark
    public EuclideanDomain.DivisionResult<Complex<BigInteger>> gaussianDivideAndRemainder() {
        return gaussianDomain.divideAndRemainder(gaussA, gaussB);
    }

    @Benchmark
    public EuclideanDomain.ExtendedGcdResult<Complex<BigInteger>> gaussianExtendedGcd() {
        return gaussianDomain.extendedGcd(gaussA, gaussB);
    }

    @Benchmark
    public Quaternion<Rational> quaternionMultiplyOverQ() {
        return quatA.multiply(quatB);
    }

    @Benchmark
    public Quaternion<Rational> quaternionReciprocalOverQ() {
        return quatA.reciprocal();
    }
}
