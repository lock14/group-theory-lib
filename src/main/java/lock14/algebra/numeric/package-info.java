/**
 * Approximate IEEE-754 floating-point algebra, thread-scoped numerical tolerances, and off-heap matrices.
 *
 * <p>Provides thread-scoped tolerance and decimal precision configuration via
 * {@link lock14.algebra.numeric.NumericContext}, an explicit {@link lock14.algebra.numeric.ApproximateField}
 * contract distinguishing rounded floating-point arithmetic from exact algebraic fields, 64-bit real,
 * complex, and quaternion numeric types ({@link lock14.algebra.numeric.Float64Field},
 * {@link lock14.algebra.numeric.Complex64}, {@link lock14.algebra.numeric.Quaternion64}), and a sealed
 * off-heap Foreign Function and Memory dense matrix sum type ({@link lock14.algebra.numeric.Float64Matrix})
 * partitioning square ({@link lock14.algebra.numeric.Float64SquareMatrix}) and strictly non-square
 * ({@link lock14.algebra.numeric.Float64RectangularMatrix}) matrices.</p>
 *
 * <h2>Example</h2>
 * {@snippet class="lock14.algebra.numeric.NumericExamplesTest" region="ffm-solve-and-rotation"}
 */
package lock14.algebra.numeric;
