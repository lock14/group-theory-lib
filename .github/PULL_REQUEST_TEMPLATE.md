## Description

Please provide a summary of the changes introduced in this pull request, including mathematical motivation and architectural context.

## Type of Change

- [ ] 🐛 Bug fix (non-breaking change which fixes an issue)
- [ ] ✨ New feature (non-breaking change which adds functionality)
- [ ] 💥 Breaking change (fix or feature that would cause existing functionality to change)
- [ ] ⚡ Performance optimization (latency, CPU, memory, or allocation improvements)
- [ ] 📝 Documentation update
- [ ] 🔧 Tooling / CI / Build configuration

## Repository Design & Architecture Checklist

- [ ] **Exact vs. Numerical Boundary:** Core `lock14.algebra.structure` implementations are 100% mathematically exact; IEEE-754 / `ScopedValue` tolerance approximations live in `lock14.algebra.numeric`.
- [ ] **Zero Runtime Dependencies:** `module lock14.algebra` requires only `java.base` (no runtime Maven dependencies added).
- [ ] **Interface Segregation:** No methods compile only to throw `UndefinedOperationException`; partial inverses pair `Optional`-returning `tryReciprocal`/`tryInverse` with `NonInvertibleElementException`-throwing methods.
- [ ] **Immutability & Ownership:** Backing arrays are encapsulated with defensive boundary copies, and parameterized elements enforce explicit parent algebra ownership (`IncompatibleAlgebraException`).
- [ ] **Package Quartet:** Domain updates maintain aligned implementation, `*Test.java` (`jqwik` laws + AssertJ), `*ExamplesTest.java` (`{@snippet}`), and JMH benchmarks where applicable.

## Verification & Testing

- [ ] Ran `./mvnw --batch-mode --no-transfer-progress clean validate test javadoc:javadoc` with zero errors or warnings.
- [ ] Ran `./mvnw --batch-mode --no-transfer-progress -Pbenchmarks test-compile` (when modifying public APIs or benchmarks).
- [ ] Verified axiomatic mathematical laws via `jqwik` `*Laws` property tests and deterministic AssertJ edge cases.
- [ ] Synchronized Javadocs, `README.md`, `llms.txt`, and `AGENTS.md` for any public API or architectural changes.
