# 📐 group-theory-lib

[![CI](https://github.com/lock14/group-theory-lib/actions/workflows/ci.yml/badge.svg)](https://github.com/lock14/group-theory-lib/actions/workflows/ci.yml)
[![Java 25](https://img.shields.io/badge/Java-25%20LTS-orange.svg)](https://openjdk.org/projects/jdk/25/)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

A modern, type-safe, high-performance abstract algebra and linear algebra library for the JVM, engineered with **Java 25 LTS** and **Algebraic Data Types (ADTs)**.

---

## ⚡ Highlights

* **Dual-Layer Architecture:** Pure functional typeclasses in `lock14.algebra.structure` decoupled from ergonomic fluent elements in `lock14.algebra.element` (`a.add(b).multiply(c)`).
* **Algebraic Data Types & Valhalla Ready:** Sealed interfaces (`Matrix<T>`) and immutable value records with exhaustive pattern matching and zero-identity semantics.
* **Modern Java 25 Idioms:** Stream Gatherers (prefix scans & windowing), Scoped Values for ambient contexts, and Foreign Function & Memory (FFM) off-heap allocations.
* **Axiomatically Verified:** 110 property-based and unit test suites running 100,000+ randomized trials with `jqwik`.

---

## 📦 Concrete Algebraic Domains

| Domain | Record Type | Algebraic Structure | Key Capabilities |
| :--- | :--- | :--- | :--- |
| **$\mathbb{Q}$ (Rationals)** | `Rational` | `Field<Rational>` | Exact fraction arithmetic with automatic GCD reduction & sign normalization |
| **$\mathbb{C}$ (Complex)** | `Complex` | `Field<Complex>` | Polar forms, Euler's identity, and Smith's scaling algorithm |
| **$\mathbb{Z}/n\mathbb{Z}$ (Modular)** | `ModuloInteger` | `CommutativeRing` / `Field` | Ring $\mathbb{Z}/n\mathbb{Z}$, Galois field $\mathbb{F}_p$, ambient `ScopedValue` modulus |
| **$\mathbb{H}$ (Quaternions)** | `Quaternion` | Division Ring | Hamilton 4D spatial rotations & non-commutative algebra |
| **$R[x]$ (Polynomials)** | `Polynomial<T>` | `CommutativeRing` | Horner's evaluation, symbolic derivatives over arbitrary rings |
| **$S_n$ (Symmetric)** | `Permutation` | `Group<Permutation>` | Permutation composition, parity/sign homomorphism, cycle notation |
| **$M_{m\times n}(R)$ (Matrices)** | `Matrix<T>` | `Ring<SquareMatrix<T>>` | Sealed ADT (`DenseMatrix`, `SquareMatrix`), Gaussian elimination, inversion |

---

## 🚀 Quick Start & Usage Examples

<details open>
<summary><b>1. Exact Fractions & Ambient Modular Arithmetic</b></summary>

```java
// Exact fraction arithmetic
Rational half = Rational.of(1, 2);
Rational third = Rational.of(1, 3);
Rational fiveSixths = half.add(third); // 5/6

// Ambient Modulo Arithmetic via Scoped Values
ScopedValue.where(AlgebraicContext.MODULUS, BigInteger.valueOf(17)).run(() -> {
    ModuloInteger a = ModuloInteger.of(12);
    ModuloInteger b = ModuloInteger.of(10);
    ModuloInteger result = a.add(b); // 5 (mod 17)
});
```
</details>

<details open>
<summary><b>2. Matrix Operations & Exhaustive Pattern Matching</b></summary>

```java
// Invert a Square Matrix over Rational Field
SquareMatrix<Rational> matrix = SquareMatrix.of(Rational.field(), new Rational[][]{
    {Rational.of(1), Rational.of(2)},
    {Rational.of(3), Rational.of(4)}
});
SquareMatrix<Rational> inv = matrix.inverse(); // [-2, 1; 3/2, -1/2]

// Exhaustive pattern matching on Matrix ADT without downcasting
String summary = switch (matrix) {
    case SquareMatrix<?> sm -> "Square " + sm.dimension() + "x" + sm.dimension() + " (det=" + sm.determinant() + ")";
    case DenseMatrix<?> dm  -> "Rectangular " + dm.rows() + "x" + dm.cols();
};
```
</details>

<details>
<summary><b>3. Monoid Stream Gatherers & Prefix Scans</b></summary>

```java
Monoid<Complex> additive = Complex.field().asAdditiveGroup();
List<Complex> terms = List.of(Complex.of(1, 1), Complex.of(2, 0), Complex.of(0, 3));

// Compute rolling prefix sum using Java 25 Stream Gatherers
List<Complex> prefixSums = terms.stream()
    .gather(additive.scanGatherer())
    .toList(); // [0, 1+i, 3+i, 3+4i]
```
</details>

---

## 🧪 Build & Test Suite

Requires **OpenJDK 25 LTS** or higher:

```bash
# Run the complete property-based & unit test suite
./mvnw clean test
```
All tests execute under JUnit 5 Jupiter and jqwik with `--enable-preview` on JDK 25 LTS.
