# Contributing to `group-theory-lib`

Thank you for your interest in contributing to `group-theory-lib`! This guide explains our architecture, development workflow, and coding standards.

---

## 🛠️ Prerequisites & Setup

- **Java Development Kit (JDK):** **OpenJDK 25 LTS** or higher.
- **Maven:** 3.9+ (or use the included Maven Wrapper `./mvnw`).
- **Preview Features:** The project targets Java 25 with preview features enabled (`--enable-preview`).

### Quick Start
```bash
# Clone the repository
git clone https://github.com/lock14/group-theory-lib.git
cd group-theory-lib

# Verify build and run all 100,000+ property test iterations
./mvnw clean test
```

---

## 📐 Architecture Principles

When adding or refactoring algebraic structures, please follow these core tenets:

1. **Dual-Layer Architecture:**
   - **Typeclasses (`lock14.algebra.structure`):** Pure functional structures (`Magma`, `Semigroup`, `Monoid`, `Group`, `Ring`, `Field`, `VectorSpace`, `Module`) defining binary operations, identities, and inverses.
   - **Fluent Elements (`lock14.algebra.element`):** Self-typed interfaces (`GroupElement`, `RingElement`, `FieldElement`) using CRTP for ergonomic method chaining (`a.add(b).multiply(c)`).

2. **Immutability & Value Records:**
   - Number types and mathematical objects (`Rational`, `Complex`, `Quaternion`, `ModuloInteger`, `Polynomial`, `Permutation`) must be immutable `record` types.
   - Constructors should validate axioms (e.g. non-zero denominators, prime moduli) and normalize representations (e.g. reduced fractions).

3. **Modern Java Platform Idioms:**
   - Leverage Stream Gatherers (`Gatherers.scan()`, `Gatherers.windowFixed()`) for prefix scans and rolling windowing.
   - Use `ScopedValue` for ambient context bindings (`AlgebraicContext.MODULUS`).
   - Use Foreign Function & Memory (FFM) API for off-heap allocations (`NativeDoubleMatrix`).

---

## 🧪 Testing Standards

All mathematical structures must be validated with **Property-Based Testing** using **`jqwik`**:

1. **Extend Axiomatic Law Suites:**
   - Any new group must extend `GroupLaws<T>` or `AbelianGroupLaws<T>`.
   - Any new ring/field must extend `RingLaws<T>`, `CommutativeRingLaws<T>`, `FieldLaws<T>`, or `ApproximateFieldLaws<T>`.
2. **Arbitrary Generators:**
   - Provide an `@Provide Arbitrary<T> elements()` generator covering the full domain and edge cases (e.g. identity, zeroes, units, inverses).

---

## 🚀 Pull Request Guidelines

1. **Branch Naming:**
   - `feat/<feature-name>` for new structures or features.
   - `fix/<bug-name>` for fixes.
   - `chore/<task-name>` for maintenance.
2. **Commit Messages:** Follow Conventional Commits (e.g., `feat: add Lie algebra structure`, `fix: normalize zero signs in complex division`).
3. **CI Gatekeeper:** Ensure `./mvnw clean test` passes locally before submitting a PR.
