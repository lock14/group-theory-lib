package lock14.algebra.quotient;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lock14.algebra.polynomial.Polynomial;
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
 * JMH microbenchmarks for AES Rijndael {@code GF(2^8)} extension field arithmetic and Rabin
 * irreducibility verification in {@code lock14.algebra.quotient}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 4, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class QuotientBenchmarks {

    private QuotientField<BigInteger> f2;
    private Polynomial<Residue<BigInteger>> aesModulus;
    private Residue<Polynomial<Residue<BigInteger>>> aesA;
    private Residue<Polynomial<Residue<BigInteger>>> aesB;

    @Setup(Level.Trial)
    public void setUp() {
        f2 = ZMod.field(2);
        Residue<BigInteger> zero = f2.zero();
        Residue<BigInteger> one = f2.one();

        // AES Rijndael modulus: 1 + x + x^3 + x^4 + x^8
        aesModulus = Polynomial.of(f2, List.of(one, one, zero, one, one, zero, zero, zero, one));
        QuotientField<Polynomial<Residue<BigInteger>>> gf256 = GaloisFields.extension(f2, aesModulus);

        // 0x53 = 1 + x + x^4 + x^6 and 0xCA = x + x^3 + x^6 + x^7
        aesA = gf256.of(Polynomial.of(f2, List.of(one, one, zero, zero, one, zero, one)));
        aesB = gf256.of(Polynomial.of(f2, List.of(zero, one, zero, one, zero, zero, one, one)));
    }

    @Benchmark
    public Residue<Polynomial<Residue<BigInteger>>> gf256Multiply() {
        return aesA.multiply(aesB);
    }

    @Benchmark
    public Residue<Polynomial<Residue<BigInteger>>> gf256Reciprocal() {
        return aesA.reciprocal();
    }

    @Benchmark
    public boolean rabinIrreducibilityDegree8OverF2() {
        return GaloisFields.isIrreducible(f2, aesModulus);
    }
}
