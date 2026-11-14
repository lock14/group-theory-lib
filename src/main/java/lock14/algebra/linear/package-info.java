/**
 * Exact coordinate linear algebra over rings, division rings, Euclidean domains, and fields.
 *
 * <p>Provides immutable coordinate vectors ({@link lock14.algebra.linear.Vector}), free left modules
 * and finite-dimensional vector spaces ({@link lock14.algebra.linear.CoordinateModule},
 * {@link lock14.algebra.linear.CoordinateVectorSpace}), and a sealed {@link lock14.algebra.linear.Matrix}
 * sum type partitioning square ({@link lock14.algebra.linear.SquareMatrix}) and strictly non-square
 * ({@link lock14.algebra.linear.RectangularMatrix}) matrices. Also includes matrix rings
 * ({@link lock14.algebra.linear.MatrixRing}), partial-pivoted LU factorization
 * ({@link lock14.algebra.linear.LuDecomposition}), and Reduced Row Echelon Form elimination
 * ({@link lock14.algebra.linear.RrefResult}).</p>
 *
 * <h2>Example</h2>
 * {@snippet class="lock14.algebra.linear.LinearExamplesTest" region="lu-solve-and-kernel"}
 */
package lock14.algebra.linear;
