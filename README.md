# Abstract Algebra & Linear Algebra Library (Java 25 LTS)

[![CI](https://github.com/lock14/group-theory-lib/actions/workflows/ci.yml/badge.svg)](https://github.com/lock14/group-theory-lib/actions/workflows/ci.yml)
[![Java 25](https://img.shields.io/badge/Java-25%20LTS-orange.svg)](https://openjdk.org/projects/jdk/25/)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

A modern, type-safe, high-performance abstract algebra and linear algebra library for the JVM, engineered with **Java 25 LTS**.

---

## 🌟 Key Features & Architectural Highlights

### 1. Dual-Layer Algebraic Architecture
- **Typeclass / Structure Layer (`lock14.algebra.structure`):** First-class algebraic structures (`Magma`, `Semigroup`, `Monoid`, `Group`, `AbelianGroup`, `Semiring`, `Ring`, `CommutativeRing`, `IntegralDomain`, `EuclideanDomain`, `Field`, `VectorSpace`, `Module`).
- **Fluent Element Interfaces (`lock14.algebra.element`):** Self-typed interfaces (`GroupElement`, `RingElement`, `FieldElement`) enabling concise and expressive chaining (`a.add(b).multiply(c)`).

### 2. Modern Java 25 Platform Capabilities
- **Stream Gatherers (`java.util.stream.Gatherer`):** Built-in rolling prefix scans (`Gatherers.scan()`) and zero-allocation matrix row windowing (`Gatherers.windowFixed()`).
- **Foreign Function & Memory API (FFM):** `NativeDoubleMatrix` with `MemorySegment` and `Arena` for off-heap, zero-GC matrix allocations.
- **Scoped Values (`ScopedValue`):** Ambient algebraic contexts (`AlgebraicContext.MODULUS`, `EPSILON`) for dynamic thread-bound modular arithmetic.
- **Immutable Value Records:** `Complex`, `Rational`, `ModuloInteger`, `Quaternion`, `Permutation`, and `Polynomial`.
- **Flexible Constructor Bodies:** Pre-constructor validation and normalization prior to superclass initialization.

### 3. Concrete Algebraic Domains
- **$\mathbb{Q}$ (Rational Numbers):** Arbitrary-precision exact arithmetic with automatic GCD reduction and sign normalization.
- **$\mathbb{C}$ (Complex Numbers):** Double-precision complex arithmetic with polar representation, Euler's identity, and Smith's scaling algorithm to prevent overflow.
- **$\mathbb{Z}/n\mathbb{Z}$ (Modular Arithmetic):** Quotient ring with coprime modular inversions ($\mathbb{Z}/p\mathbb{Z}$ field).
- **$\mathbb{H}$ (Quaternions):** Hamilton's skew-field / division ring.
- **$R[x]$ (Polynomial Rings):** Univariate polynomials over arbitrary rings with Horner's evaluation and symbolic derivatives.
- **$S_n$ (Symmetric Groups):** Finite permutation groups with composition, inverse, parity/sign homomorphism, and disjoint cycle decomposition.
- **$M_{m\times n}(R)$ (Matrix Engine):** Flat 1D row-major contiguous memory layout, cache-friendly $i$-$k$-$j$ multiplication, trace, transpose, determinant, and Gauss-Jordan inverse.

### 4. Rigorous Property-Based Verification
- **Axiomatic Law Suites:** Automated jqwik property-based tests verifying algebraic axioms (associativity, identity, inverse, distributivity, commutativity) over 100,000+ randomized trials.

---

## 🚀 Quick Start & Usage Examples

### Rational & Modular Arithmetic
```java
// Exact fraction arithmetic
Rational half = Rational.of(1, 2);
Rational third = Rational.of(1, 3);
Rational fiveSixths = half.add(third); // 5/6

// Ambient Modulo Arithmetic using Scoped Values
ScopedValue.where(AlgebraicContext.MODULUS, BigInteger.valueOf(17)).run(() -> {
    ModuloInteger a = ModuloInteger.of(12);
    ModuloInteger b = ModuloInteger.of(10);
    ModuloInteger result = a.add(b); // 5 (mod 17)
});
```

### High-Performance Matrix Operations
```java
RationalField field = RationalField.INSTANCE;

// Matrix multiplication
Rational[][] m1 = {
    {Rational.of(3), Rational.of(-2), Rational.of(5)},
    {Rational.of(3), Rational.of(0),  Rational.of(4)}
};
Rational[][] m2 = {
    {Rational.of(2),  Rational.of(3)},
    {Rational.of(-9), Rational.of(0)},
    {Rational.of(0),  Rational.of(4)}
};

DenseMatrix<Rational> a = DenseMatrix.of(field, m1);
DenseMatrix<Rational> b = DenseMatrix.of(field, m2);
DenseMatrix<Rational> c = a.multiply(b);

// Inverting a Square Matrix
SquareMatrix<Rational> sq = SquareMatrix.of(field, new Rational[][]{
    {Rational.of(1), Rational.of(2)},
    {Rational.of(3), Rational.of(4)}
});
SquareMatrix<Rational> inv = sq.inverse(); // [-2, 1; 3/2, -1/2]
```

### Monoid Stream Gatherers & Prefix Scans
```java
Monoid<Complex> additive = Complex.field().asAdditiveGroup();
List<Complex> terms = List.of(Complex.of(1, 1), Complex.of(2, 0), Complex.of(0, 3));

// Compute rolling prefix sum: [0, 1+i, 3+i, 3+4i]
List<Complex> prefixSums = terms.stream()
    .gather(additive.scanGatherer())
    .toList();
```

---

## 🧪 Running the Verification Suite

```bash
mvn clean test
```
All tests execute under JUnit 5 Jupiter and jqwik with `--enable-preview` on JDK 25 LTS.
