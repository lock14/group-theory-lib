/**
 * Core algebraic structure typeclasses, structure-preserving morphisms, and universal product constructions.
 *
 * <p>In the 3.0 architecture, algebraic structures are modeled as carrier-external typeclasses
 * parameterized over an arbitrary element type {@code T}, decoupling algebraic laws, neutral
 * elements ({@code identity()}, {@code zero()}, {@code one()}), and generic algorithms from
 * concrete element representations while complementing the fluent self-typed element contracts
 * in {@link lock14.algebra.element}.
 *
 * <p>This package organizes three foundational families:
 * <ul>
 *   <li><b>Single-operation hierarchy:</b> {@link lock14.algebra.structure.Magma},
 *       {@link lock14.algebra.structure.Semigroup}, {@link lock14.algebra.structure.Monoid},
 *       {@link lock14.algebra.structure.Group}, {@link lock14.algebra.structure.AbelianGroup},
 *       and {@link lock14.algebra.structure.FiniteGroup}.</li>
 *   <li><b>Ring, domain, and module hierarchy:</b> {@link lock14.algebra.structure.Semiring},
 *       {@link lock14.algebra.structure.Ring}, {@link lock14.algebra.structure.CommutativeRing},
 *       {@link lock14.algebra.structure.IntegralDomain},
 *       {@link lock14.algebra.structure.EuclideanDomain},
 *       {@link lock14.algebra.structure.DivisionRing}, {@link lock14.algebra.structure.Field},
 *       {@link lock14.algebra.structure.LeftModule}, and
 *       {@link lock14.algebra.structure.VectorSpace}.</li>
 *   <li><b>Morphisms and universal constructions:</b>
 *       {@link lock14.algebra.structure.GroupHomomorphism},
 *       {@link lock14.algebra.structure.RingHomomorphism},
 *       {@link lock14.algebra.structure.Pair}, and
 *       {@link lock14.algebra.structure.DirectProduct}.</li>
 * </ul>
 */
package lock14.algebra.structure;
