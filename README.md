# group-theory-lib

[![CI](https://github.com/lock14/group-theory-lib/actions/workflows/ci.yml/badge.svg)](https://github.com/lock14/group-theory-lib/actions/workflows/ci.yml)
[![Security](https://github.com/lock14/group-theory-lib/actions/workflows/security.yml/badge.svg)](https://github.com/lock14/group-theory-lib/actions/workflows/security.yml)
[![Benchmarks](https://github.com/lock14/group-theory-lib/actions/workflows/benchmark.yml/badge.svg)](https://github.com/lock14/group-theory-lib/actions/workflows/benchmark.yml)
[![Coverage](https://img.shields.io/codecov/c/github/lock14/group-theory-lib/main)](https://app.codecov.io/gh/lock14/group-theory-lib/tree/main)
[![Java 25](https://img.shields.io/badge/Java-25%20LTS-orange.svg)](https://openjdk.org/projects/jdk/25/)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

An exact abstract algebra, finite group theory, linear algebra, and off-heap numerical computing library for Java 25 LTS (`module lock14.algebra`) with zero runtime dependencies.

## Quick start

Requires OpenJDK 25 LTS or higher:

```xml
<dependency>
  <groupId>lock14</groupId>
  <artifactId>group-theory-lib</artifactId>
  <version>3.0.0-SNAPSHOT</version>
</dependency>
```

Clone, run the full test suite, and build Javadocs:

```bash
git clone https://github.com/lock14/group-theory-lib.git
cd group-theory-lib
./mvnw clean validate test javadoc:javadoc
```

## Architectural pillars

*   **Exact core hierarchy**: Every `Group`, `Ring`, `IntegralDomain`, `EuclideanDomain`, `DivisionRing`, and `Field` implementation in `lock14.algebra.structure` satisfies its algebraic axioms under exact mathematical equality—never compromised by IEEE-754 rounding or underflow zero divisors.
*   **Explicit numerical and off-heap layer**: Floating-point approximations (`ApproximateField`, `Float64Field`, `Complex64`, `Quaternion64`) and off-heap Foreign Function and Memory matrices (`Float64Matrix`, `Float64SquareMatrix`, `Float64RectangularMatrix`) live in `lock14.algebra.numeric` with `ScopedValue`-configured tolerances (`NumericContext.EPSILON`).
*   **Compile-time interface segregation**: Sealed matrix sum types (`Matrix<T>` and `Float64Matrix`), explicit scalar typeclass witnesses (`CommutativeRing<T>`, `DivisionRing<T>`, `Field<T>`), and lazy `FiniteGroup<T>` element views ensure every exposed method is supported across all instances without runtime capability exceptions.
*   **Universal constructions**: Build quotient groups $G/N$, direct products $G \times H$ and $R \times S$, group actions (orbits, stabilizers, Burnside's lemma), and universal Euclidean quotient rings and fields $E/(m)$ unifying $\mathbb{Z}/n\mathbb{Z}$, $\mathbb{F}_p$, and Galois extension fields $GF(p^k) = \mathbb{F}_p[x]/(f(x))$.
*   **Encapsulated zero-copy value carriers**: `Permutation`, `Polynomial<T>`, `Vector<T>`, `RectangularMatrix<T>`, and `SquareMatrix<T>` encapsulate backing storage behind package-private zero-copy constructors, preventing array aliasing and redundant cloning during arithmetic.

## Usage examples

### Finite group quotients ($S_4 / V_4 \cong S_3$) and Burnside's lemma

```java
SymmetricGroup s4 = new SymmetricGroup(4);
Subgroup<Permutation> v4 = Subgroup.of(s4, Set.of(
    Permutation.identity(4),
    Permutation.fromCycles(4, List.of(List.of(0, 1), List.of(2, 3))),
    Permutation.fromCycles(4, List.of(List.of(0, 2), List.of(1, 3))),
    Permutation.fromCycles(4, List.of(List.of(0, 3), List.of(1, 2)))
));

QuotientGroup<Permutation> quotient = QuotientGroup.of(s4, v4); // order 6, non-abelian
BigInteger conjugacyClasses = GroupAction.conjugation(s4).countOrbitsBurnside(s4, s4.elements()); // 5
```

### Universal quotients and Galois extension fields $GF(2^8)$

```java
// Prime field F_17 and modular ring Z/12Z with explicit parent ownership
var f17 = ZMod.field(17);
var inv3 = ZMod.element(f17, 3).reciprocal(); // 6 (mod 17)

// AES Rijndael extension field GF(2^8) = F_2[x] / (1 + x + x^3 + x^4 + x^8)
// Verified irreducible over F_2 via Rabin's polynomial-time test
var gf256 = GaloisFields.extension(2, 1, 1, 0, 1, 1, 0, 0, 0, 1);
```

### Exact linear algebra: disjoint matrix ADT, RREF, kernel, and LU solve

```java
SquareMatrix<Rational> a = SquareMatrix.of(2, Rationals.FIELD,
    Rational.of(1), Rational.of(2),
    Rational.of(3), Rational.of(4));
Vector<Rational> b = Vector.of(Rationals.FIELD, Rational.of(5), Rational.of(11));

Vector<Rational> x = a.solve(Rationals.FIELD, b); // [1, 2]
LuDecomposition<Rational> lu = a.lu(Rationals.FIELD); // P * A == L * U

Matrix<Rational> rect = Matrix.of(2, 4, Rationals.FIELD,
    Rational.of(1), Rational.of(2), Rational.of(0), Rational.of(3),
    Rational.of(0), Rational.of(0), Rational.of(1), Rational.of(-1));
List<Vector<Rational>> nullspaceBasis = rect.kernel(Rationals.FIELD); // 2 basis vectors satisfying rect * v == 0
```

### Off-heap FFM `Float64SquareMatrix` and scoped numerical tolerance

```java
try (Float64SquareMatrix m = Float64SquareMatrix.of(new double[][]{{1e-20, 1.0}, {1.0, 1.0}})) {
    double[] solution = ScopedValue.where(NumericContext.EPSILON, 1e-12)
        .call(() -> m.solve(new double[]{1.0, 2.0})); // [1.0, 1.0] via partial pivoting
}
```

## Package reference

| Package | Key types | Algebraic structures and capabilities |
| :--- | :--- | :--- |
| `lock14.algebra.structure` | `Group`, `FiniteGroup`, `Ring`, `EuclideanDomain`, `DivisionRing`, `Field`, `LeftModule`, `VectorSpace`, `DirectProduct`, `GroupHomomorphism`, `RingHomomorphism` | Exact algebraic typeclass hierarchy, zero-allocation binary exponentiation, Java 25 `Gatherer` scans, direct products, and morphisms |
| `lock14.algebra.element` | `GroupElement`, `RingElement`, `DivisionRingElement`, `FieldElement`, `ModuleElement` | Self-typed fluent element interfaces enabling natural arithmetic expressions (`a.add(b)`, `x.reciprocal()`) |
| `lock14.algebra.exceptions` | `AlgebraException`, `NonInvertibleElementException`, `IncompatibleAlgebraException`, `DimensionMismatchException` | Unchecked domain exception hierarchy for invalid algebraic and linear operations |
| `lock14.algebra.group` | `Permutation`, `SymmetricGroup`, `AlternatingGroup`, `CyclicGroup`, `DihedralGroup`, `Subgroup`, `Coset`, `QuotientGroup`, `GroupAction` | Lazy finite groups ($S_n, A_n, C_n, D_n$) with `BigInteger` orders, normal subgroup verification, quotient groups $G/N$, Orbit-Stabilizer, and Burnside's lemma |
| `lock14.algebra.numbers` | `Integers`, `Rational`, `Rationals`, `Complex<T>`, `GaussianIntegers`, `ComplexField<T>`, `Quaternion<T>`, `QuaternionRing<T>`, `QuaternionDivisionRing<T>` | Canonical least-non-negative Euclidean $\mathbb{Z}$, exact $\mathbb{Q}$, Euclidean Gaussian integers $\mathbb{Z}[i]$, $\mathbb{Q}(i)$, and exact division ring $\mathbb{H}(\mathbb{Q})$ |
| `lock14.algebra.polynomial` | `Polynomial<T>`, `PolynomialRing<T>`, `NonCommutativePolynomialRing<T>`, `PolynomialEuclideanDomain<T>` | Univariate polynomials over commutative and non-commutative rings, monic Euclidean long division, Bézout GCD, Horner evaluation, and formal derivatives |
| `lock14.algebra.quotient` | `Residue<T>`, `QuotientRing<T>`, `QuotientField<T>`, `ZMod`, `GaloisFields` | Universal quotients $E/(m)$, $\mathbb{Z}/n\mathbb{Z}$, $\mathbb{F}_p$, and Rabin-verified finite extension fields $GF(p^k)$ (including AES $GF(2^8)$) |
| `lock14.algebra.linear` | `Matrix<T>`, `SquareMatrix<T>`, `RectangularMatrix<T>`, `Vector<T>`, `LuDecomposition<T>`, `RrefResult<T>`, `MatrixRing<T>` | Disjoint sealed `Matrix<T>` ADT, `rref`, `rank`, `nullity`, right `kernel` basis, $PA = LU$, `solve`, and Bareiss and Bird determinants |
| `lock14.algebra.numeric` | `ApproximateField<T>`, `Float64Field`, `Complex64`, `Quaternion64`, `Float64Matrix`, `Float64SquareMatrix`, `Float64RectangularMatrix`, `NumericContext` | Smith-scaled `Complex64`, 3D axis-angle and SLERP `Quaternion64`, and sealed FFM `Arena`-backed `Float64Matrix` hierarchy with partial-pivoted `solve` |

## Benchmarks

Run the isolated JMH microbenchmark suite with GC allocation profiling (`-prof gc`):

```bash
./mvnw -Pbenchmarks test-compile exec:exec "-Djmh.args=LinearBenchmarks -wi 2 -i 4 -f 1 -prof gc"
```

## Contributing

Review the [contributing guide](CONTRIBUTING.md) for architectural invariants, `jqwik` law-testing conventions, and pull request verification steps.

## Security

Report security vulnerabilities privately according to the [security policy](SECURITY.md).

## License

Apache License, Version 2.0. See [LICENSE](LICENSE).
