# 📐 group-theory-lib 3.0

[![CI](https://github.com/lock14/group-theory-lib/actions/workflows/ci.yml/badge.svg)](https://github.com/lock14/group-theory-lib/actions/workflows/ci.yml)
[![Security](https://github.com/lock14/group-theory-lib/actions/workflows/security.yml/badge.svg)](https://github.com/lock14/group-theory-lib/actions/workflows/security.yml)
[![Benchmarks](https://github.com/lock14/group-theory-lib/actions/workflows/benchmark.yml/badge.svg)](https://github.com/lock14/group-theory-lib/actions/workflows/benchmark.yml)
[![Coverage](https://img.shields.io/codecov/c/github/lock14/group-theory-lib)](https://codecov.io/gh/lock14/group-theory-lib)
[![Java 25](https://img.shields.io/badge/Java-25%20LTS-orange.svg)](https://openjdk.org/projects/jdk/25/)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

A clean-slate, axiomatically exact abstract algebra, finite group theory, and linear algebra library for **Java 25 LTS** (`module lock14.algebra`).

---

## ⚡ Architectural Pillars

* **100% Exact Core Hierarchy (`lock14.algebra.structure`):** Every `Group`, `Ring`, `IntegralDomain`, `EuclideanDomain`, `DivisionRing`, and `Field` implementation satisfies its algebraic laws with **exact equality**—never polluted by IEEE-754 rounding or underflow zero divisors.
* **Explicit Numerical & Off-Heap Layer (`lock14.algebra.numeric`):** Floating-point approximations (`ApproximateField`, `Float64Field`, `Complex64`, `Quaternion64`) and off-heap Foreign Function & Memory matrices (`Float64Matrix`) live in a dedicated numerical module with `ScopedValue`-configured tolerances (`NumericContext.EPSILON`).
* **Universal Constructions:** Build quotient groups $G/N$, direct products $G \times H$ and $R \times S$, group actions (orbits, stabilizers, Burnside's lemma), and universal Euclidean quotient rings/fields $E/(m)$ unifying $\mathbb{Z}/n\mathbb{Z}$, $\mathbb{F}_p$, and Galois extension fields $GF(p^k) = \mathbb{F}_p[x]/(f(x))$.
* **Encapsulated Zero-Copy Value Carriers:** `Permutation`, `Polynomial<T>`, `Vector<T>`, `RectangularMatrix<T>`, and `SquareMatrix<T>` encapsulate backing storage behind private zero-copy constructors—eliminating array aliasing and redundant cloning during arithmetic.

---

## 📦 Package & Domain Overview

| Package | Key Types | Algebraic Structures & Capabilities |
| :--- | :--- | :--- |
| `lock14.algebra.structure` | `Group`, `FiniteGroup`, `Ring`, `EuclideanDomain`, `DivisionRing`, `Field`, `LeftModule`, `VectorSpace`, `DirectProduct`, `GroupHomomorphism`, `RingHomomorphism` | Exact algebraic typeclass hierarchy, zero-allocation binary exponentiation, Java 25 `Gatherer` scans, direct products, and morphisms |
| `lock14.algebra.element` | `GroupElement`, `RingElement`, `DivisionRingElement`, `FieldElement`, `ModuleElement` | Self-typed fluent element interfaces enabling natural infix-style arithmetic (`a.add(b)`, `x.reciprocal()`) |
| `lock14.algebra.exceptions` | `AlgebraException`, `NonInvertibleElementException`, `IncompatibleAlgebraException`, `DimensionMismatchException`, `UndefinedOperationException` | Unchecked domain exception hierarchy for invalid algebraic operations |
| `lock14.algebra.group` | `Permutation`, `SymmetricGroup`, `AlternatingGroup`, `CyclicGroup`, `DihedralGroup`, `Subgroup`, `Coset`, `QuotientGroup`, `GroupAction` | Finite groups ($S_n, A_n, C_n, D_n$), normal subgroup verification, quotient groups $G/N$, Orbit-Stabilizer, and Burnside's lemma |
| `lock14.algebra.numbers` | `Integers`, `Rational`, `Rationals`, `Complex<T>`, `GaussianIntegers`, `ComplexField<T>`, `Quaternion<T>`, `QuaternionRing<T>`, `QuaternionDivisionRing<T>` | Canonical least-non-negative Euclidean $\mathbb{Z}$, exact $\mathbb{Q}$, Euclidean Gaussian integers $\mathbb{Z}[i]$, $\mathbb{Q}(i)$, and exact division ring $\mathbb{H}(\mathbb{Q})$ |
| `lock14.algebra.polynomial` | `Polynomial<T>`, `PolynomialRing<T>`, `NonCommutativePolynomialRing<T>`, `PolynomialEuclideanDomain<T>` | Univariate polynomials over commutative & non-commutative rings, monic Euclidean long division, Bézout GCD, Horner evaluation, derivatives |
| `lock14.algebra.quotient` | `Residue<T>`, `QuotientRing<T>`, `QuotientField<T>`, `ZMod`, `GaloisFields` | Universal quotients $E/(m)$, $\mathbb{Z}/n\mathbb{Z}$, $\mathbb{F}_p$, and Rabin-verified finite extension fields $GF(p^k)$ (including AES $GF(2^8)$) |
| `lock14.algebra.linear` | `Matrix<T>`, `SquareMatrix<T>`, `RectangularMatrix<T>`, `Vector<T>`, `LuDecomposition<T>`, `RrefResult<T>`, `MatrixRing<T>` | Disjoint sealed `Matrix<T>` ADT, `rref()`, `rank()`, `nullity()`, right `kernel()` basis, `PA = LU`, `solve()`, Bareiss & Bird determinants |
| `lock14.algebra.numeric` | `ApproximateField<T>`, `Float64Field`, `Complex64`, `Quaternion64`, `Float64Matrix`, `NumericContext` | Smith-scaled `Complex64`, 3D axis-angle/SLERP `Quaternion64`, and FFM `Arena`-backed `Float64Matrix` with partial-pivoted `solve()` |

---

## 🚀 Quick Start Examples

<details open>
<summary><b>1. Finite Group Quotients (S₄ / V₄ ≅ S₃) & Burnside's Lemma</b></summary>

```java
SymmetricGroup s4 = new SymmetricGroup(4);
Subgroup<Permutation> v4 = Subgroup.of(s4, Set.of(
    Permutation.identity(4),
    Permutation.fromCycles(4, List.of(List.of(0, 1), List.of(2, 3))),
    Permutation.fromCycles(4, List.of(List.of(0, 2), List.of(1, 3))),
    Permutation.fromCycles(4, List.of(List.of(0, 3), List.of(1, 2)))
));

QuotientGroup<Permutation> quotient = QuotientGroup.of(s4, v4); // order 6, non-abelian
long conjugacyClasses = GroupAction.conjugation(s4).countOrbitsBurnside(s4, s4.elements()); // 5
```
</details>

<details open>
<summary><b>2. Universal Quotients & Galois Extension Fields GF(2⁸)</b></summary>

```java
// Prime field F_17 and modular ring Z/12Z with explicit parent ownership
var f17 = ZMod.field(17);
var inv3 = ZMod.element(f17, 3).reciprocal(); // 6 (mod 17)

// AES Rijndael extension field GF(2^8) = F_2[x] / (x^8 + x^4 + x^3 + x + 1)
// Verified irreducible over F_2 via Rabin's polynomial-time test
var gf256 = GaloisFields.extension(2, 1, 1, 0, 1, 1, 0, 0, 0, 1);
```
</details>

<details open>
<summary><b>3. Exact Linear Algebra: Disjoint Matrix ADT, RREF, Kernel & LU Solve</b></summary>

```java
SquareMatrix<Rational> a = SquareMatrix.of(2, Rationals.FIELD,
    Rational.of(1), Rational.of(2),
    Rational.of(3), Rational.of(4));
Vector<Rational> b = Vector.of(Rationals.FIELD, Rational.of(5), Rational.of(11));

Vector<Rational> x = a.solve(b); // [1, 2]
LuDecomposition<Rational> lu = a.lu(); // P * A == L * U

Matrix<Rational> rect = Matrix.of(2, 4, Rationals.FIELD,
    Rational.of(1), Rational.of(2), Rational.of(0), Rational.of(3),
    Rational.of(0), Rational.of(0), Rational.of(1), Rational.of(-1));
List<Vector<Rational>> nullspaceBasis = rect.kernel(); // 2 basis vectors satisfying rect * v == 0
```
</details>

<details>
<summary><b>4. Off-Heap FFM Float64Matrix & Scoped Numerical Tolerance</b></summary>

```java
try (Float64Matrix m = Float64Matrix.of(new double[][]{{1e-20, 1.0}, {1.0, 1.0}})) {
    double[] solution = ScopedValue.where(NumericContext.EPSILON, 1e-12)
        .call(() -> m.solve(new double[]{1.0, 2.0})); // [1.0, 1.0] via partial pivoting
}
```
</details>

---

## 🧪 Build & Verification

Requires **OpenJDK 25 LTS** or higher:

```bash
# Run Enforcer, Checkstyle, unit & jqwik law tests, and snippet-verified Javadocs
./mvnw clean validate test javadoc:javadoc

# Run JMH microbenchmarks with GC allocation tracking (-prof gc)
./mvnw -Pbenchmarks test-compile exec:exec "-Djmh.args=LinearBenchmarks -wi 2 -i 4 -f 1 -prof gc"
```

---

## 🤝 Contributing & Security

* **Contributing:** We welcome contributions! Please review [CONTRIBUTING.md](CONTRIBUTING.md) for architectural invariants, `jqwik` law-testing conventions, and pull request verification steps.
* **Security:** Please report security vulnerabilities privately according to our [Security Policy](SECURITY.md).

## 📄 License

Apache License, Version 2.0. See [LICENSE](LICENSE).
