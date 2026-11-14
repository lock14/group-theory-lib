/**
 * Fluent F-bounded (self-typed) interfaces for elements of algebraic structures.
 *
 * <p>In the 3.0 architecture, algebraic operations are modeled through two complementary layers:
 * carrier-external structure typeclasses ({@link lock14.algebra.structure}) that define
 * identities, algebraic laws, and generic algorithms over arbitrary carrier types {@code T},
 * and self-typed element interfaces ({@code E extends ...<E>}) in this package that provide
 * ergonomic instance-method syntax ({@code a.add(b)}, {@code a.multiply(b)}, {@code g.combine(h)})
 * for concrete algebraic value types.
 *
 * <p>Core self-typed element contracts include:
 * <ul>
 *   <li>{@link lock14.algebra.element.GroupElement} &mdash; associative combination, inversion,
 *       and right cancellation ({@code remove}).</li>
 *   <li>{@link lock14.algebra.element.RingElement} &mdash; ring addition, negation, subtraction,
 *       and multiplication.</li>
 *   <li>{@link lock14.algebra.element.DivisionRingElement} &mdash; two-sided multiplicative
 *       inversion and left/right division over (possibly non-commutative) skew-fields.</li>
 *   <li>{@link lock14.algebra.element.FieldElement} &mdash; commutative field arithmetic where
 *       left and right division coincide.</li>
 *   <li>{@link lock14.algebra.element.ModuleElement} &mdash; additive abelian group operations
 *       together with scalar multiplication over a scalar ring {@code R}.</li>
 * </ul>
 */
package lock14.algebra.element;
