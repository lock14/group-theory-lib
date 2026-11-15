package lock14.algebra.benchmarks;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lock14.algebra.group.Coset;
import lock14.algebra.group.GroupAction;
import lock14.algebra.group.Permutation;
import lock14.algebra.group.QuotientGroup;
import lock14.algebra.group.Subgroup;
import lock14.algebra.group.SymmetricGroup;
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
 * JMH microbenchmarks for permutation composition, cycle decomposition, quotient coset arithmetic,
 * and Burnside's lemma orbit counting in {@code lock14.algebra.group}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 4, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class GroupBenchmarks {

    private Permutation permA;
    private Permutation permB;
    private SymmetricGroup s4;
    private QuotientGroup<Permutation> s4ModV4;
    private Coset<Permutation> cosetA;
    private Coset<Permutation> cosetB;
    private GroupAction<Permutation, Permutation> conjugationS4;

    @Setup(Level.Trial)
    public void setUp() {
        permA = Permutation.fromCycles(16, List.of(
                List.of(0, 5, 11, 2, 9),
                List.of(1, 14, 7),
                List.of(3, 12, 6, 15),
                List.of(4, 10, 8, 13)
        ));
        permB = Permutation.fromCycles(16, List.of(
                List.of(0, 3, 7, 15, 2, 11),
                List.of(1, 9, 4, 14),
                List.of(5, 12, 8),
                List.of(6, 13, 10)
        ));

        s4 = new SymmetricGroup(4);
        Subgroup<Permutation> kleinFour = Subgroup.of(s4, Set.of(
                Permutation.identity(4),
                Permutation.fromCycles(4, List.of(List.of(0, 1), List.of(2, 3))),
                Permutation.fromCycles(4, List.of(List.of(0, 2), List.of(1, 3))),
                Permutation.fromCycles(4, List.of(List.of(0, 3), List.of(1, 2)))
        ));
        s4ModV4 = QuotientGroup.of(s4, kleinFour);
        cosetA = kleinFour.leftCoset(Permutation.fromCycles(4, List.of(List.of(0, 1, 2))));
        cosetB = kleinFour.leftCoset(Permutation.transposition(4, 0, 1));
        conjugationS4 = GroupAction.conjugation(s4);
    }

    @Benchmark
    public Permutation composePermutationsS16() {
        return permA.combine(permB);
    }

    @Benchmark
    public List<List<Integer>> decomposeCyclesS16() {
        return permA.cycles();
    }

    @Benchmark
    public Coset<Permutation> quotientCosetMultiplyS4ModV4() {
        return s4ModV4.combine(cosetA, cosetB);
    }

    @Benchmark
    public long burnsideConjugacyClassesS4() {
        return conjugationS4.countOrbitsBurnside(s4, s4.elements());
    }
}
