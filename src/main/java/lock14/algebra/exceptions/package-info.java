/**
 * Unchecked exception hierarchy for algebraic domain, compatibility, and structural contract violations.
 *
 * <p>In the 3.0 architecture, both carrier-external algebraic typeclasses
 * ({@link lock14.algebra.structure}) and fluent self-typed domain elements
 * ({@link lock14.algebra.element}) enforce mathematical preconditions using unchecked
 * runtime exceptions:
 * <ul>
 *   <li>{@link lock14.algebra.exceptions.AlgebraException} &mdash; root unchecked exception for
 *       general algebraic domain and structural contract violations.</li>
 *   <li>{@link lock14.algebra.exceptions.IncompatibleAlgebraException} &mdash; thrown when operands
 *       belong to incompatible parent structures (such as distinct moduli, scalar rings, or degrees).</li>
 *   <li>{@link lock14.algebra.exceptions.NonInvertibleElementException} &mdash; thrown when attempting
 *       to invert zero, a non-unit ring element, or a singular matrix.</li>
 *   <li>{@link lock14.algebra.exceptions.DimensionMismatchException} &mdash; thrown when vector or
 *       matrix dimensions violate operation compatibility constraints.</li>
 *   <li>{@link lock14.algebra.exceptions.UndefinedOperationException} &mdash; thrown when an operation
 *       is mathematically undefined over the governing algebraic structure.</li>
 * </ul>
 */
package lock14.algebra.exceptions;
