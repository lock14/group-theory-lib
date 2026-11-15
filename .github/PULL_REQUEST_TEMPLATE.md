## Summary

Explain the mathematical or architectural problem in the prior code and why this design was chosen.

## Design and architecture checklist

- [ ] **Exact and numerical boundary**: Core `lock14.algebra.structure` implementations are mathematically exact; IEEE-754 and `ScopedValue` tolerance approximations live in `lock14.algebra.numeric`.
- [ ] **Zero runtime dependencies**: `module lock14.algebra` requires only `java.base` (no runtime Maven dependencies added).
- [ ] **Interface segregation and no refused bequests**: Every exposed method is supported across all instances of its receiver (no `UnsupportedOperationException` stubs, fat interfaces, or runtime `instanceof` algebra checks); partial singularities pair `Optional`-returning `try*` queries with `NonInvertibleElementException`- or `ArithmeticException`-throwing methods.
- [ ] **Immutability and ownership**: Backing arrays are encapsulated with defensive boundary copies, and parameterized elements enforce explicit parent algebra ownership (`IncompatibleAlgebraException`).
- [ ] **Package quartet**: Domain updates maintain aligned implementation, `*Test.java` (`jqwik` laws and AssertJ), `*ExamplesTest.java` (`{@snippet}`), and JMH benchmarks where applicable.

## Verification

- [ ] Ran `./mvnw --batch-mode --no-transfer-progress clean validate test javadoc:javadoc` with zero errors or warnings.
- [ ] Ran `./mvnw --batch-mode --no-transfer-progress -Pbenchmarks test-compile` (when modifying public APIs or benchmarks).
- [ ] Verified axiomatic mathematical laws via `jqwik` `*Laws` property tests and deterministic AssertJ edge cases.
- [ ] Updated and synchronized documentation (Javadocs, `README.md`, `CONTRIBUTING.md`, `AGENTS.md`, `llms.txt`).
