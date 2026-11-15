# Contributing to group-theory-lib

This document outlines the architectural invariants, package conventions, testing and benchmarking workflows, and pull request guidelines for `lock14/group-theory-lib`.

## Core architectural principles

*   **Exact and numerical separation**:
    *   `lock14.algebra.structure` (`Group`, `FiniteGroup`, `Ring`, `IntegralDomain`, `EuclideanDomain`, `DivisionRing`, `Field`, `LeftModule`, `VectorSpace`) is strictly for mathematically exact types (`equals` matches mathematical equality).
    *   IEEE-754 floating-point approximations (`ApproximateField`, `Float64Field`, `Complex64`, `Quaternion64`) and off-heap Foreign Function and Memory matrices (`Float64Matrix`, `Float64SquareMatrix`, `Float64RectangularMatrix`) live in `lock14.algebra.numeric` using `ScopedValue`-configured tolerances (`NumericContext.EPSILON`).
*   **Zero runtime dependencies**: `module lock14.algebra` depends only on `java.base`. Test and benchmark libraries (`junit-jupiter`, `assertj-core`, `jqwik`, `jmh-core`) are strictly scoped to testing and benchmarking.
*   **Interface segregation and no refused bequests**: Never implement an interface or expose a method on a wider type only to throw `UnsupportedOperationException` or a runtime capability exception (*refused bequest* and *fat interface* anti-patterns). Partition shapes at compile time via sealed types (`Matrix<T> permits SquareMatrix, RectangularMatrix` and `Float64Matrix permits Float64RectangularMatrix, Float64SquareMatrix`), pass explicit typeclass witnesses (`CommutativeRing<T>`, `DivisionRing<T>`, `Field<T>`) when operations require stronger scalar laws, back `FiniteGroup.elements()` with lazy `Set` views and `BigInteger` orders, and pair total `Optional` queries (`tryReciprocal`, `tryInverse`, `tryLog`, `tryPow`) with `NonInvertibleElementException`- or `ArithmeticException`-throwing convenience methods at mathematical singularities.
*   **Immutability and explicit ownership**: Exact elements encapsulate their backing storage (defensive copies at public boundaries, package-private zero-copy constructors internally) and carry their parent ring, modulus, or group explicitly, throwing `IncompatibleAlgebraException` on mismatched algebras.

## Package structure

Each domain package (`structure`, `group`, `numbers`, `polynomial`, `quotient`, `linear`, `numeric`) maintains four coordinated artifacts:

1.  **Main implementation**: `src/main/java/lock14/algebra/<pkg>/` contains implementation classes and a `package-info.java` overview with JEP 413 `{@snippet}` examples.
2.  **Law and unit tests**: `src/test/java/lock14/algebra/<pkg>/<Type>Test.java` contains `jqwik` property-based `*Laws` verification and AssertJ edge-case unit tests.
3.  **Consumer examples**: `src/test/java/lock14/algebra/<pkg>/<Pkg>ExamplesTest.java` contains runnable consumer-perspective examples demarcated with `// @start region="..."` and `// @end`.
4.  **Microbenchmarks**: `src/test/java/lock14/algebra/<pkg>/<Pkg>Benchmarks.java` contains JMH microbenchmarks measuring latency (`ns/op`) and allocation rate (`B/op`) inside the matching domain package.

## Development setup and workflow

### Prerequisites

*   OpenJDK 25 LTS or higher
*   Git (Maven is bundled via `./mvnw` and `.\mvnw.cmd`)

### Clone, test, and generate Javadocs

```bash
git clone https://github.com/lock14/group-theory-lib.git
cd group-theory-lib

# Run validation, full unit and jqwik property test suite, and strict Javadoc verification
./mvnw clean validate test javadoc:javadoc
```

### Property-based law testing (`jqwik`)

Every concrete algebraic structure must implement the corresponding `*Laws` contract interface in `src/test/java/lock14/algebra/laws/` (such as `FieldLaws<T>`, `EuclideanDomainLaws<T>`, `DivisionRingLaws<T>`, or `FiniteGroupLaws<T>`):

```java
class MyFieldTest implements FieldLaws<MyElement> {
    @Override
    public Field<MyElement> algebraicStructure() {
        return MyField.INSTANCE;
    }

    @Provide
    @Override
    public Arbitrary<MyElement> elements() {
        // Include 0, 1, additive inverses, and random elements
    }
}
```

### Benchmarking (JMH)

Activate the `-Pbenchmarks` profile to compile and run JMH benchmarks with GC allocation profiling (`-prof gc`):

```bash
# Compile JMH benchmarks
./mvnw -Pbenchmarks test-compile

# Run a targeted benchmark suite with GC allocation tracking
./mvnw -Pbenchmarks test-compile exec:exec "-Djmh.args=LinearBenchmarks -wi 2 -i 4 -f 1 -prof gc"
```

## Pre-pull-request verification checklist

Before opening a pull request, verify all checks succeed locally with zero errors or warnings:

```bash
# 1. Enforcer, Checkstyle, strict compiler (-Xlint:all -Werror), tests, JaCoCo, and Javadocs
./mvnw --batch-mode --no-transfer-progress clean validate test javadoc:javadoc

# 2. Verify JMH benchmarks compile cleanly (when modifying public APIs or benchmarks)
./mvnw --batch-mode --no-transfer-progress -Pbenchmarks test-compile
```

## Pull request guidelines

1.  **Branch naming**: Use descriptive prefixes (`feat/...`, `fix/...`, `perf/...`, `docs/...`, `ci/...`, `chore/...`).
2.  **Commit messages**: Follow [Conventional Commits](https://www.conventionalcommits.org/) with an imperative subject line of 50 characters or fewer (hard cap at 72) and no trailing period (for example, `feat: add Cayley graph generator` or `fix: handle degree-0 derivative over characteristic p`).
3.  **Pull request template**: Complete the [pull request template](.github/PULL_REQUEST_TEMPLATE.md), explaining "why" before "what" and noting algebraic law coverage and any benchmark impact.
4.  **Documentation synchronization**: Update Javadocs, `README.md`, `CONTRIBUTING.md`, `llms.txt`, and `AGENTS.md` together whenever public APIs, packages, or invariants change.
