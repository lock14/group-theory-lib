# Repository design principles

*   **Exact and numerical boundary**:
    *   **Exact core**: In `lock14.algebra.structure`, every concrete implementation of `Magma`, `Semigroup`, `Monoid`, `Group`, `AbelianGroup`, `FiniteGroup`, `Semiring`, `Ring`, `CommutativeRing`, `IntegralDomain`, `EuclideanDomain`, `DivisionRing`, `Field`, `LeftModule`, and `VectorSpace` must satisfy its algebraic axioms under exact equality (`equals` matches mathematical equality). Never implement exact core interfaces on IEEE-754 floating-point types where rounding or underflow violates associativity, distributivity, or integral-domain laws.
    *   **Explicit numerical and off-heap layer**: Floating-point approximations (`ApproximateField`, `Float64Field`, `Complex64`, `Quaternion64`) and off-heap Foreign Function and Memory structures (`Float64Matrix`, `Float64SquareMatrix`, `Float64RectangularMatrix`) live exclusively in `lock14.algebra.numeric` and resolve comparison and singularity tolerances through `ScopedValue`-backed `NumericContext.EPSILON` (and `NumericContext.MATH_CONTEXT` for `BigDecimal` conversions).
*   **Zero runtime dependencies**: The JPMS module `lock14.algebra` (`src/main/java/module-info.java`) requires only `java.base`. Never add `compile` or `runtime` dependencies to `pom.xml`; testing, static analysis, and benchmarking dependencies must remain strictly in `test` or `provided` scopes or isolated build profiles.
*   **Dual-layer typeclass and fluent element architecture**:
    *   **Typeclasses**: Interfaces in `lock14.algebra.structure` encode algebraic laws, identity elements (`identity()`, `zero()`, `one()`), generic algorithms (binary exponentiation, Stream `Gatherer` prefix scans, Euclidean Bézout GCD), homomorphisms, and direct products over arbitrary carrier types `T` (including JDK types such as `BigInteger` and `Integer`).
    *   **Self-typed elements**: Domain value types (`Rational`, `Complex<T>`, `Quaternion<T>`, `Residue<T>`, `Polynomial<T>`, `Permutation`, `Vector<T>`) implement F-bounded interfaces in `lock14.algebra.element` (`GroupElement<E>`, `RingElement<E>`, `DivisionRingElement<E>`, `FieldElement<E>`, `ModuleElement<E, S>`) so callers can write fluent expressions (`a.add(b).multiply(c)`) without sacrificing typeclass generality.
*   **Interface segregation, no refused bequests, and total inverses**:
    *   **No refused bequests or fat interfaces**: Never implement an interface or inherit a contract only to throw `UnsupportedOperationException` or a custom capability exception on unsupported methods (*refused bequest*), and never place shape-specific or algebra-specific operations onto a wider base type (*fat interface*). Every method exposed on a type must be genuinely supported across all instances of that type.
    *   **Sealed shape hierarchies**: Partition shape-dependent linear structures via sealed sum types (`sealed interface Matrix<T> permits SquareMatrix, RectangularMatrix` and `sealed interface Float64Matrix permits Float64RectangularMatrix, Float64SquareMatrix`) and narrow via exhaustive pattern matching rather than partial `asSquareMatrix()` conversion methods that throw on non-square receivers.
    *   **Compile-time capability witnesses**: When an operation on a parameterized carrier (`Matrix<T>`, `SquareMatrix<T>`, `Complex<T>`, `Quaternion<T>`) requires stronger algebraic structure than `Ring<T>`, require the corresponding typeclass witness (`CommutativeRing<T>`, `DivisionRing<T>`, `Field<T>`) in the method signature and verify parent ring equality (`IncompatibleAlgebraException` on mismatch) rather than inspecting `ring()` with runtime `instanceof` checks.
    *   **Lazy finite-group carriers and arbitrary-precision orders**: Concrete `FiniteGroup<T>` implementations (`SymmetricGroup`, `AlternatingGroup`, `CyclicGroup`, `DihedralGroup`, `DirectProduct.finiteGroup`) must return exact `BigInteger` orders, expose compact generating sets via `generators()`, and back `elements()` with lazy unmodifiable `Set<T>` views rather than throwing capability or overflow exceptions above arbitrary degree thresholds.
    *   **Total singularity queries**: Pair total, exception-free queries (`Optional<T> tryReciprocal(...)`, `Optional<SquareMatrix<T>> tryInverse(...)`, `Optional<Float64SquareMatrix> tryInverse()`, `Optional<Complex64> tryLog()`, `Optional<Complex64> tryPow(...)`) with convenience methods (`reciprocal`, `inverse`, `log`, `pow`) that throw `NonInvertibleElementException` or `ArithmeticException` strictly at genuine mathematical singularities (zero, non-units, singular matrices, or analytic poles).
*   **Encapsulated zero-copy value carriers and immutability**:
    *   All algebraic elements and structures in exact packages are deeply immutable and thread-safe by construction.
    *   Array-backed carriers (`Permutation`, `Polynomial<T>`, `Vector<T>`, `RectangularMatrix<T>`, `SquareMatrix<T>`) must encapsulate their backing arrays: public constructors and factories validate invariants and defensively copy inputs, accessors return unmodifiable views or defensive copies, and trusted internal operations construct results via package-private zero-copy constructors.
*   **Explicit algebraic ownership**:
    *   Parameterized elements (`Residue<T>`, `Complex<T>`, `Quaternion<T>`, `Polynomial<T>`, `Vector<T>`, `Matrix<T>`, `Coset<T>`) explicitly carry their parent ring, modulus, or group.
    *   Never store exact algebraic moduli or coefficient rings in `ThreadLocal` or `ScopedValue` state. Combining elements from incompatible rings, moduli, or groups must immediately throw `IncompatibleAlgebraException`; incompatible linear dimensions must throw `DimensionMismatchException`.
*   **Off-heap Foreign Function and Memory lifecycle**:
    *   Off-heap structures (`Float64Matrix`, `Float64SquareMatrix`, `Float64RectangularMatrix`) manage native memory via `Arena` and implement `AutoCloseable`. Public factories default to `Arena.ofConfined()` (or accept a caller-supplied `Arena`) for deterministic deallocation.

# Package file structure and layout conventions

Every exported domain package in `module lock14.algebra` follows a standardized four-part layout:

1.  **Main implementation and package overview**: Under `src/main/java/lock14/algebra/<pkg>/`:
    *   `package-info.java`: Architectural overview of the package embedding verified JEP 413 `{@snippet}` examples.
    *   `<Type>.java`: Immutable elements, typeclass implementations, and package-private algorithmic helpers (such as `MatrixOps.java`).
2.  **Axiomatic law and unit tests**: `src/test/java/lock14/algebra/<pkg>/<Type>Test.java` provides `jqwik` property-based law verification implementing the matching `*Laws` interface from `lock14.algebra.laws`, paired with deterministic JUnit 5 and AssertJ edge-case tests.
3.  **Runnable consumer examples**: `src/test/java/lock14/algebra/<pkg>/<Pkg>ExamplesTest.java` contains end-to-end public API demonstrations demarcated with `// @start region="..."` and `// @end` comments, executed by JUnit and rendered in `package-info.java` Javadocs.
4.  **JMH benchmarks**: `src/test/java/lock14/algebra/<pkg>/<Pkg>Benchmarks.java` measures latency (`ns/op`) and allocation rate (`B/op`) inside the matching domain package under Maven's standard `src/test/java` source tree (never introduce non-standard `src/jmh/` source roots or catch-all `benchmarks` subpackages), executed via the `-Pbenchmarks` Maven profile.

## Internal section ordering in Java files

Maintain consistent top-to-bottom ordering within `.java` classes:

1.  `package` declaration and sorted `import` statements (no wildcard `*` imports).
2.  Class, interface, or record Javadoc (including `@param <T>` and all record `@param` component tags).
3.  `@Serial private static final long serialVersionUID = 1L;` (on `Serializable` and `Throwable` subclasses) and `public static final` constants.
4.  Instance fields (`private final`).
5.  Constructors (public and compact record constructors first, package-private and private zero-copy constructors second).
6.  Public static factory methods (`of`, `zero`, `one`, `zeros`, `identity`).
7.  Public algebraic operations, structural queries, and conversions.
8.  `equals`, `hashCode`, and `toString` overrides.
9.  Package-private and `private` helper methods.

# Naming, Javadoc, and code style conventions

*   **Formatting and style**: Follow `.editorconfig` and `checkstyle.xml`: UTF-8 encoding, LF (`\n`) line endings, 4-space indentation for `.java` files (2-space for `.xml` and `.yml`), no tab characters, no trailing whitespace, and no wildcard or unused imports.
*   **Zero compiler warnings**: All main, test, and JMH sources must compile cleanly under `javac --release 25 -Xlint:all -Werror`. Annotate safe generic varargs factories with `@SafeVarargs` (making instance methods `final` where required) and keep `@SuppressWarnings("unchecked")` narrowly scoped to verified array casts.
*   **Zero Javadoc warnings**: Every `public` and `protected` type, record component, constructor, constant, and method in exported packages must have complete JDK 25 `doclint`-compliant Javadoc (`<failOnWarnings>true</failOnWarnings>`). Always wrap generic and arrow notation (`<`, `>`, `&`, `->`) inside `{@code ...}` or `{@link ...}`.

# Testing conventions

*   **Axiomatic law suites**: Whenever adding or modifying an algebraic structure, its test class must implement the strongest applicable `*Laws` interface from `lock14.algebra.laws` (`MagmaLaws`, `SemigroupLaws`, `MonoidLaws`, `GroupLaws`, `AbelianGroupLaws`, `FiniteGroupLaws`, `SemiringLaws`, `RingLaws`, `CommutativeRingLaws`, `IntegralDomainLaws`, `EuclideanDomainLaws`, `DivisionRingLaws`, `FieldLaws`, `ModuleLaws`, `VectorSpaceLaws`, or `ApproximateFieldLaws`).
*   **Generator edge cases**: `@Provide Arbitrary<T>` generators must include structural edge cases (`zero`, `one`, additive and multiplicative inverses, zero divisors, boundary residues, negative numerators) so `jqwik` exercises algebraic corners automatically.
*   **Deterministic AssertJ edge cases**: Pair property tests with focused `@Test` methods using AssertJ (`assertThat`, `assertThatThrownBy`) covering known mathematical identities (such as $S_4/V_4 \cong S_3$, Burnside's lemma counts, AES $GF(2^8)$ S-box inverses, $PA = LU$, rank-nullity, and exception paths).

# Benchmarking conventions

*   **Standard Maven package locality**: Benchmarks live alongside unit and example tests in `src/test/java/lock14/algebra/<pkg>/<Pkg>Benchmarks.java` under package `lock14.algebra.<pkg>`, exercising public APIs and executed via JMH annotation processing in the `-Pbenchmarks` profile.
*   **Standard annotations**: Annotate suites with `@BenchmarkMode(Mode.AverageTime)`, `@OutputTimeUnit(TimeUnit.NANOSECONDS)`, and `@State(Scope.Benchmark)`.
*   **Fixture isolation and dead-code prevention**: Pre-populate algebraic structures, matrices, polynomials, and FFM `Arena` instances inside `@Setup(Level.Trial)` (closing off-heap arenas in `@TearDown(Level.Trial)`), and always return computed results (or feed `Blackhole`) so JIT cannot elide work.
*   **Allocation tracking**: Run benchmarks with `-prof gc` to inspect `gc.alloc.rate.norm` (`B/op`) alongside latency (`ns/op`).

# Consumer examples and snippets

*   Every domain package (`structure`, `group`, `numbers`, `polynomial`, `quotient`, `linear`, `numeric`) maintains a `<Pkg>ExamplesTest.java` suite containing concise, idiomatic usage snippets wrapped in `// @start region="name"` and `// @end`.
*   Embed those regions in the corresponding `package-info.java` via `{@snippet class="lock14.algebra.<pkg>.<Pkg>ExamplesTest" region="name"}` so examples are compiled, asserted by JUnit, and verified by `javadoc` on every build.

# Technical writing and typography conventions

Technical documentation in this repository is engineered for cognitive efficiency, scannability, and mathematical accuracy across `README.md`, `CONTRIBUTING.md`, `SECURITY.md`, `AGENTS.md`, `llms.txt`, Javadocs, pull request descriptions, issues, and commit messages:

*   **Intent-driven structure and information density**:
    *   **Diátaxis separation**: Separate documentation by reader intent (minimal quick-start commands, imperative task guides, austere lookup tables, and design explanations) rather than mixing tutorials, reference tables, and history in one stream.
    *   **Non-accretion rule**: Never append standalone notes or bullets to the end of a section; integrate changes into the appropriate section or table and prune superseded statements so documents read as a single cohesive draft.
    *   **Zero fluff and zero aspirational documentation**: Omit marketing buzzwords ("effortlessly", "blazing fast", "seamlessly") and conversational preambles ("Note that...", "It is worth mentioning..."). Document only behavior implemented and verified in the current codebase.
*   **Typography and formatting restraint**:
    *   **One format per span**: Never combine bold, italics, code font, or hyperlinks on the same text (no bolded code spans, no bolded links, no bold-italics, and no bolded first-column table cells).
    *   **Strict role separation**:
        *   **Code font**: Use backticks strictly for literal code, Java types and methods, Maven coordinates, CLI commands, flags, and file paths. Keep sentence punctuation outside backticks.
        *   **Bold**: Use bold (`**...**`) only for UI labels and run-in list headings followed by a colon (`:`). Never use a dash after a run-in heading, and never use bold for mid-sentence emphasis or whole clauses.
        *   **Italics**: Use italics (`_..._` or `*...*`) only when introducing a term on first use or for titles of standalone works.
    *   **Sentence-case headings and table headers**: Use sentence case for all document titles, section headings, and table column headers (capitalizing only the first word and proper nouns or standard mathematical and technical acronyms such as `Java`, `JPMS`, `Maven`, `JMH`, `JUnit`, `AssertJ`, `Checkstyle`, `CodeQL`, `FFM`, `IEEE-754`, `LU`, `RREF`, `GCD`, `SLERP`, `AES`, `Galois`, `Gaussian`, `Euclidean`, `Burnside`, `Rabin`, `README`, and `Git`), with no trailing punctuation.
    *   **Mathematical notation allowed, decorative symbols prohibited**: Standard LaTeX mathematical notation ($G/N$, $S_4/V_4 \cong S_3$, $\mathbb{Z}[i]$, $GF(p^k)$, $PA = LU$, $O(n^2)$) is encouraged for algebraic structures, identities, dimensions, and complexity bounds. Omit decorative emojis, GitHub emoji shortcodes, and ASCII or LaTeX arrows (`->`, `$\rightarrow$`) used as prose connectors outside mathematical formulas, code blocks, and diagrams; write plain English verbs ("extends", "maps to", "takes precedence over") instead.
    *   **No horizontal section dividers**: Do not place horizontal rules (`---`) between sections; Markdown headings provide structural hierarchy.
    *   **Clean punctuation and list discipline**: Do not use italic parenthetical asides for notes or caveats; state important details as normal sentences and reserve `> [!IMPORTANT]` callouts for critical invariants. Do not use slashes (`/`) in prose to join English alternatives (write "or" or "and" instead), reserving `/` for file paths, URLs, literal commands, units (`ns/op`, `B/op`), and mathematical quotients ($G/N$, $\mathbb{Z}/n\mathbb{Z}$). Use numbered lists strictly for sequential steps and bulleted lists for non-sequential sets.
*   **Surface-specific standards**:
    *   **READMEs**: Open with a concise summary of what the library provides and its Java target, followed by quick-start dependency and build snippets, domain examples, and a clean package reference table. Keep build plumbing and agent rules out of `README.md`.
    *   **Pull request descriptions**: Follow the Google CL Description style with a standalone imperative title, a summary explaining "why" before "what" grouped by subsystem, and lean plain-text checklists without decorative emojis. On Windows PowerShell, always pass PR and issue Markdown bodies via `--body-file <path>` (never double-quoted `--body "..."` strings containing backticks).
    *   **Issues**: Structure bug reports around four factual sections (environment, minimal reproducible Java snippet, expected behavior, and actual behavior with stack trace) and frame feature proposals around the mathematical or workflow problem before proposing an API design.
    *   **Comments**: Write complete, contract-focused Javadocs on exported types and methods. Keep implementation comments focused on "why" a non-obvious algorithmic or numerical constraint exists rather than "what" the code literally does, and prefix non-blocking review polish suggestions with `Nit:`.
    *   **Commit messages**: Follow Chris Beams' seven rules: imperative subject line targeting 50 characters (hard cap at 72) with no trailing period, a blank line, and a 72-character wrapped plain-text body explaining what changed and why (compatible with Conventional Commit prefixes such as `feat:`, `fix:`, `perf:`, `docs:`, `ci:`, `chore:`).

# Documentation roles and synchronization

*   **User-facing documentation**: `README.md` is reserved strictly for public overview, quick-start examples, package capability tables, badges, and build instructions. Do not put internal plumbing or agent instructions in `README.md`.
*   **Contributor-facing documentation**: `CONTRIBUTING.md` is the dedicated setup, testing, benchmarking, and pull-request guide for human contributors.
*   **Agent and architectural invariants**: `AGENTS.md` is the single canonical source of truth at the repository root for architectural invariants, layout rules, testing and benchmarking standards, technical writing rules, and agent workflows (do not create `.agents/AGENTS.md` or `CLAUDE.md` symlinks).
*   **LLM reference index**: `llms.txt` provides a concise machine-readable summary of module exports, core APIs, and design invariants.
*   **Synchronization rule**: Any change that adds, renames, or alters public packages, types, or architectural invariants must update Javadocs, `README.md`, `CONTRIBUTING.md`, `llms.txt`, and `AGENTS.md` in the same pull request.

# Pull request and workflow conventions

*   **Conventional Commits and branch naming**: Use descriptive branches (`feat/...`, `fix/...`, `perf/...`, `docs/...`, `ci/...`, `chore/...`) and imperative commit messages.
*   **Pull request description synchronization**: Complete `.github/PULL_REQUEST_TEMPLATE.md`. Whenever follow-up commits expand or adjust a pull request branch, proactively update the pull request description via `gh pr edit --body-file <path>` so it stays accurate.

# Continuous learning and self-correction

*   **Codifying corrections**: Whenever corrected by the user regarding an architectural choice, mathematical convention, code style rule, technical writing rule, or workflow preference, immediately update `AGENTS.md` to codify the underlying rule before concluding the task so future sessions adhere to it automatically.

# Pre-completion verification checklist

Before concluding any code modification task, run and verify all of the following commands succeed with zero errors or warnings:

1.  `./mvnw --batch-mode --no-transfer-progress clean validate test javadoc:javadoc`
2.  `./mvnw --batch-mode --no-transfer-progress -Pbenchmarks test-compile`
