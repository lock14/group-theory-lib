# Contributing to group-theory-lib

Thank you for your interest in contributing to `lock14/group-theory-lib`! This document outlines our architectural invariants, package conventions, testing and benchmarking workflows, and pull request guidelines.

---

## 1. Core Architectural Principles

*   **Exact vs. Numerical Separation:**
    *   `lock14.algebra.structure` (`Group`, `FiniteGroup`, `Ring`, `IntegralDomain`, `EuclideanDomain`, `DivisionRing`, `Field`, `LeftModule`, `VectorSpace`) is strictly for **100% mathematically exact** types (`equals` matches mathematical equality).
    *   IEEE-754 floating-point approximations (`ApproximateField`, `Float64Field`, `Complex64`, `Quaternion64`) and off-heap Foreign Function & Memory matrices (`Float64Matrix`) live in `lock14.algebra.numeric` using `ScopedValue`-configured tolerances (`NumericContext.EPSILON`).
*   **Zero Runtime Dependencies:** `module lock14.algebra` depends *only* on `java.base`. Test and benchmark libraries (`junit-jupiter`, `assertj-core`, `jqwik`, `jmh-core`) are strictly scoped to testing and benchmarking.
*   **Interface Segregation:** Never implement an algebraic interface only to throw `UndefinedOperationException` at runtime. Use sealed types (`Matrix<T> permits SquareMatrix, RectangularMatrix`) and pair total `Optional<T> tryReciprocal` / `tryInverse` methods with `NonInvertibleElementException`-throwing `reciprocal()` / `inverse()`.
*   **Immutability & Explicit Ownership:** Exact elements encapsulate their backing storage (defensive copies at public boundaries, package-private zero-copy constructors internally) and carry their parent ring/modulus/group explicitly, throwing `IncompatibleAlgebraException` on mismatched algebras.

---

## 2. Package Structure

Each domain package (`structure`, `group`, `numbers`, `polynomial`, `quotient`, `linear`, `numeric`) maintains four coordinated artifacts:

1.  **`src/main/java/lock14/algebra/<pkg>/`**: Implementation classes plus `package-info.java` overview with JEP 413 `{@snippet}` examples.
2.  **`src/test/java/lock14/algebra/<pkg>/<Type>Test.java`**: `jqwik` property-based `*Laws` verification plus AssertJ edge-case unit tests.
3.  **`src/test/java/lock14/algebra/<pkg>/<Pkg>ExamplesTest.java`**: Runnable consumer-perspective examples demarcated with `// @start region="..."` / `// @end`.
4.  **`src/jmh/java/lock14/algebra/benchmarks/<Pkg>Benchmarks.java`**: JMH microbenchmarks measuring latency (`ns/op`) and allocation rate (`B/op`).

---

## 3. Development Setup & Workflow

### Prerequisites

*   **OpenJDK 25 LTS** or higher
*   **Git** (Maven is bundled via `./mvnw` / `.\mvnw.cmd`)

### Clone, Test & Generate Javadocs

```bash
git clone https://github.com/lock14/group-theory-lib.git
cd group-theory-lib

# Run validation, full unit + jqwik property test suite, and strict Javadoc verification
./mvnw clean validate test javadoc:javadoc
```

### Property-Based Law Testing (`jqwik`)

Every concrete algebraic structure must implement the corresponding `*Laws` contract interface in `src/test/java/lock14/algebra/laws/` (for example, `FieldLaws<T>`, `EuclideanDomainLaws<T>`, `DivisionRingLaws<T>`, or `FiniteGroupLaws<T>`):

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

Once the `-Pbenchmarks` profile is active, compile and run JMH benchmarks (including GC allocation profiling via `-prof gc`):

```bash
# Compile JMH benchmarks
./mvnw -Pbenchmarks test-compile

# Run a targeted benchmark suite with GC allocation tracking
./mvnw -Pbenchmarks test-compile exec:exec "-Djmh.args=LinearBenchmarks -wi 2 -i 4 -f 1 -prof gc"
```

---

## 4. Pre-Pull-Request Verification Checklist

Before opening a pull request, ensure all checks succeed locally with zero errors or warnings:

```bash
# 1. Enforcer, Checkstyle, strict compiler (-Xlint:all -Werror), tests, JaCoCo, and Javadocs
./mvnw --batch-mode --no-transfer-progress clean validate test javadoc:javadoc

# 2. Verify JMH benchmarks compile cleanly (when modifying public APIs or benchmarks)
./mvnw --batch-mode --no-transfer-progress -Pbenchmarks test-compile
```

---

## 5. Pull Request Guidelines

1.  **Branch Naming:** Use descriptive prefixes (`feat/...`, `fix/...`, `perf/...`, `docs/...`, `ci/...`, `chore/...`).
2.  **Commit Messages:** Follow [Conventional Commits](https://www.conventionalcommits.org/) (e.g., `feat: add Cayley graph generator`, `fix: handle degree-0 derivative over characteristic p`).
3.  **PR Template:** Complete [`.github/PULL_REQUEST_TEMPLATE.md`](.github/PULL_REQUEST_TEMPLATE.md), noting algebraic law coverage and any benchmark impact.
4.  **Documentation Sync:** Update Javadocs, `README.md`, `llms.txt`, and `AGENTS.md` together whenever public APIs or packages change.
