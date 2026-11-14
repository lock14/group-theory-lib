# Repository Design Principles

*   **Exact vs. Numerical Boundary:**
    *   **100% Exact Core (`lock14.algebra.structure`):** Every concrete implementation of `Magma`, `Semigroup`, `Monoid`, `Group`, `AbelianGroup`, `FiniteGroup`, `Semiring`, `Ring`, `CommutativeRing`, `IntegralDomain`, `EuclideanDomain`, `DivisionRing`, `Field`, `LeftModule`, and `VectorSpace` MUST satisfy its algebraic axioms under exact equality (`equals` matches mathematical equality). Never implement exact core interfaces on IEEE-754 floating-point types where rounding or underflow violates associativity, distributivity, or integral-domain laws.
    *   **Explicit Numerical & Off-Heap Layer (`lock14.algebra.numeric`):** Floating-point approximations (`ApproximateField`, `Float64Field`, `Complex64`, `Quaternion64`) and off-heap Foreign Function & Memory structures (`Float64Matrix`) live exclusively in `lock14.algebra.numeric` and resolve comparison/singularity tolerances through `ScopedValue`-backed `NumericContext.EPSILON` (and `NumericContext.MATH_CONTEXT` for `BigDecimal` conversions).
*   **Zero Runtime Dependencies:** The JPMS module `lock14.algebra` (`src/main/java/module-info.java`) requires *only* `java.base`. Never add `compile` or `runtime` dependencies to `pom.xml`; testing, static analysis, and benchmarking dependencies must remain strictly in `test` / `provided` scopes or isolated build profiles.
*   **Dual-Layer Typeclass & Fluent Element Architecture:**
    *   **Typeclasses (`lock14.algebra.structure`):** Encode algebraic laws, identity/zero/one elements (`identity()`, `zero()`, `one()`), generic algorithms (binary exponentiation, Stream `Gatherer` prefix scans, Euclidean Bézout GCD), homomorphisms, and direct products over arbitrary carrier types `T` (including JDK types like `BigInteger` and `Integer`).
    *   **Self-Typed Elements (`lock14.algebra.element`):** Domain value types (`Rational`, `Complex<T>`, `Quaternion<T>`, `Residue<T>`, `Polynomial<T>`, `Permutation`, `Vector<T>`) implement F-bounded interfaces (`GroupElement<E>`, `RingElement<E>`, `DivisionRingElement<E>`, `FieldElement<E>`, `ModuleElement<E, S>`) so callers can write fluent expressions (`a.add(b).multiply(c)`) without sacrificing typeclass generality.
*   **Interface Segregation (No "Unsupported Operation" Runtime Lies):**
    *   Types must never implement an algebraic or linear interface only to throw `UndefinedOperationException` at runtime (e.g., rectangular matrices must not expose `determinant()` or `inverse()`, and rings with zero divisors must not implement `Field`).
    *   Use sealed algebraic data types (`sealed interface Matrix<T> permits SquareMatrix, RectangularMatrix`) to separate shape-specific operations at compile time.
    *   Pair total, exception-free invertibility queries (`Optional<T> tryReciprocal(T)`, `Optional<SquareMatrix<T>> tryInverse()`) with throwing convenience methods (`reciprocal()`, `inverse()`) that throw `NonInvertibleElementException` on zero, non-units, or singular matrices.
*   **Encapsulated Zero-Copy Value Carriers & Immutability:**
    *   All algebraic elements and structures in exact packages are deeply immutable and thread-safe by construction.
    *   Array-backed carriers (`Permutation`, `Polynomial<T>`, `Vector<T>`, `RectangularMatrix<T>`, `SquareMatrix<T>`) MUST encapsulate their backing arrays: public constructors/factories validate invariants and defensively copy inputs, accessors return unmodifiable views or defensive copies, and trusted internal operations construct results via package-private zero-copy constructors.
*   **Explicit Algebraic Ownership (No Ambient Algebra State):**
    *   Parameterized elements (`Residue<T>`, `Complex<T>`, `Quaternion<T>`, `Polynomial<T>`, `Vector<T>`, `Matrix<T>`, `Coset<T>`) explicitly carry their parent ring, modulus, or group.
    *   Never store exact algebraic moduli or coefficient rings in `ThreadLocal` or `ScopedValue` state. Combining elements from incompatible rings, moduli, or groups MUST immediately throw `IncompatibleAlgebraException`; incompatible linear dimensions MUST throw `DimensionMismatchException`.
*   **Off-Heap Foreign Function & Memory (`java.lang.foreign`) Lifecycle:**
    *   Off-heap structures (`Float64Matrix`) manage native memory via `Arena` and implement `AutoCloseable`. Public factories default to `Arena.ofConfined()` (or accept a caller-supplied `Arena`) for deterministic deallocation.

# Package File Structure & Layout Conventions

Every exported domain package in `module lock14.algebra` follows a standardized 4-part layout:

1.  **Main Implementation & Package Overview (`src/main/java/lock14/algebra/<pkg>/`):**
    *   `package-info.java`: Architectural overview of the package embedding verified JEP 413 `{@snippet}` examples.
    *   `<Type>.java`: Immutable elements, typeclass implementations, and package-private algorithmic helpers (e.g., `MatrixOps.java`).
2.  **Axiomatic Law & Unit Tests (`src/test/java/lock14/algebra/<pkg>/<Type>Test.java`):**
    *   `jqwik` property-based law verification implementing the matching `*Laws` interface from `lock14.algebra.laws`, paired with deterministic JUnit 5 + AssertJ edge-case tests.
3.  **Runnable Consumer Examples (`src/test/java/lock14/algebra/<pkg>/<Pkg>ExamplesTest.java`):**
    *   End-to-end public API demonstrations demarcated with `// @start region="..."` and `// @end` comments, executed by JUnit and rendered in `package-info.java` Javadocs.
4.  **JMH Benchmarks (`src/jmh/java/lock14/algebra/benchmarks/<Pkg>Benchmarks.java`):**
    *   Throughput/latency (`ns/op`) and allocation-rate (`B/op`) microbenchmarks compiled and executed via the isolated `-Pbenchmarks` Maven profile.

## Internal Section Ordering in `.java` Files

Maintain consistent top-to-bottom ordering within classes:
1.  `package` declaration and sorted `import` statements (no wildcard `*` imports).
2.  Class/interface/record Javadoc (including `@param <T>` and all record `@param` component tags).
3.  `@Serial private static final long serialVersionUID = 1L;` (on `Serializable` / `Throwable` subclasses) and `public static final` constants.
4.  Instance fields (`private final`).
5.  Constructors (public/compact record constructors first, package-private/private zero-copy constructors second).
6.  Public static factory methods (`of`, `zero`, `one`, `zeros`, `identity`, etc.).
7.  Public algebraic operations, structural queries, and conversions.
8.  `equals`, `hashCode`, and `toString` overrides.
9.  Package-private and `private` helper methods.

# Naming, Javadoc & Code Style Conventions

*   **Formatting & Style:** Follow `.editorconfig` and `checkstyle.xml`: UTF-8 encoding, LF (`\n`) line endings, 4-space indentation for `.java` files (2-space for `.xml`/`.yml`), no tab characters, no trailing whitespace, and no wildcard or unused imports.
*   **Zero Compiler Warnings:** All main, test, and JMH sources must compile cleanly under `javac --release 25 -Xlint:all -Werror`. Annotate safe generic varargs factories with `@SafeVarargs` (making instance methods `final` where required) and keep `@SuppressWarnings("unchecked")` narrowly scoped to verified array casts.
*   **Zero Javadoc Warnings:** Every `public` and `protected` type, record component, constructor, constant, and method in exported packages MUST have complete JDK 25 `doclint`-compliant Javadoc (`<failOnWarnings>true</failOnWarnings>`). Always wrap mathematical inequalities and generic/arrow notation (`<`, `>`, `&`, `->`) inside `{@code ...}` or `{@link ...}`.

# Testing Conventions

*   **Axiomatic Law Suites (`lock14.algebra.laws`):** Whenever adding or modifying an algebraic structure, its test class MUST implement the strongest applicable `*Laws` interface (`MagmaLaws`, `SemigroupLaws`, `MonoidLaws`, `GroupLaws`, `AbelianGroupLaws`, `FiniteGroupLaws`, `SemiringLaws`, `RingLaws`, `CommutativeRingLaws`, `IntegralDomainLaws`, `EuclideanDomainLaws`, `DivisionRingLaws`, `FieldLaws`, `ModuleLaws`, `VectorSpaceLaws`, or `ApproximateFieldLaws`).
*   **Generator Edge Cases:** `@Provide Arbitrary<T>` generators must include structural edge cases (`zero`, `one`, additive/multiplicative inverses, zero divisors, boundary residues, negative numerators) so `jqwik` exercises algebraic corners automatically.
*   **Deterministic AssertJ Edge Cases:** Pair property tests with focused `@Test` methods using AssertJ (`assertThat`, `assertThatThrownBy`) covering known mathematical identities (e.g., $S_4/V_4 \cong S_3$, Burnside's lemma counts, AES $GF(2^8)$ S-box inverses, $PA = LU$, rank-nullity, and exception paths).

# Benchmarking Conventions (JMH)

*   **Isolation:** Benchmarks live in `src/jmh/java/lock14/algebra/benchmarks/` under package `lock14.algebra.benchmarks` and consume `lock14.algebra.*` strictly through public exported APIs via the `-Pbenchmarks` profile.
*   **Standard Annotations:** Annotate suites with `@BenchmarkMode(Mode.AverageTime)`, `@OutputTimeUnit(TimeUnit.NANOSECONDS)`, and `@State(Scope.Benchmark)`.
*   **Fixture Isolation & Dead-Code Prevention:** Pre-populate algebraic structures, matrices, polynomials, and FFM `Arena`s inside `@Setup(Level.Trial)` (closing off-heap arenas in `@TearDown(Level.Trial)`), and always return computed results (or feed `Blackhole`) so JIT cannot elide work.
*   **Allocation Tracking:** Run benchmarks with `-prof gc` to inspect `gc.alloc.rate.norm` (`B/op`) alongside latency (`ns/op`).

# Consumer Examples (`*ExamplesTest.java` & `{@snippet}`)

*   Every domain package (`structure`, `group`, `numbers`, `polynomial`, `quotient`, `linear`, `numeric`) maintains a `<Pkg>ExamplesTest.java` suite containing concise, idiomatic usage snippets wrapped in `// @start region="name"` ... `// @end`.
*   Embed those regions in the corresponding `package-info.java` via `{@snippet class="lock14.algebra.<pkg>.<Pkg>ExamplesTest" region="name"}` so examples are compiled, asserted by JUnit, and verified by `javadoc` on every build.

# Documentation Roles & Synchronization

*   **`README.md` (Consumer-Facing Only):** Reserved strictly for public overview, quick-start examples, package capability tables, badges, and build instructions. Do not put internal plumbing or agent instructions in `README.md`.
*   **`AGENTS.md` (Architecture & Agent Directives):** Canonical source of truth for architectural invariants, layout rules, testing/benchmarking standards, and agent workflows.
*   **`CONTRIBUTING.md` (Contributor Guide):** Human-friendly setup, workflow, and pull-request guide.
*   **`llms.txt` (LLM Reference Index):** Concise machine-readable summary of module exports, core APIs, and invariants.
*   **Synchronization Rule:** Any change that adds, renames, or alters public packages, types, or architectural invariants MUST update **Javadocs**, **`README.md`**, **`llms.txt`**, and **`AGENTS.md`** in the same pull request.

# Pull Request & Workflow Conventions

*   **Conventional Commits & Branch Naming:** Use descriptive branches (`feat/...`, `fix/...`, `perf/...`, `docs/...`, `ci/...`, `chore/...`) and Conventional Commit messages.
*   **PR Description Synchronization:** Fill out `.github/PULL_REQUEST_TEMPLATE.md` completely. Whenever follow-up commits expand or adjust a PR branch, proactively update the PR description via `gh pr edit` so it stays accurate.

# Continuous Learning & Self-Correction

*   **Codifying Corrections:** Whenever corrected by the user regarding an architectural choice, mathematical convention, code style rule, or workflow preference, update `AGENTS.md` to codify the underlying rule so future sessions adhere to it automatically.

# Pre-Completion Verification Checklist

Before concluding any code modification task, run and verify all of the following commands succeed with zero errors or warnings:
1. `./mvnw --batch-mode --no-transfer-progress clean validate test javadoc:javadoc`
2. `./mvnw --batch-mode --no-transfer-progress -Pbenchmarks test-compile`
