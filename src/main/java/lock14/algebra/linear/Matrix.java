package lock14.algebra.linear;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lock14.algebra.structure.Ring;

/**
 * Canonical, disjoint Algebraic Data Type (Sum Type) representing an immutable Matrix over an exact {@link Ring} {@code R}:
 * <ul>
 *   <li>{@link SquareMatrix} exclusively represents {@code n x n} square matrices ({@code n >= 1}).</li>
 *   <li>{@link RectangularMatrix} exclusively represents {@code m x n} non-square matrices ({@code m, n >= 1, m != n}).</li>
 * </ul>
 *
 * @param <T> scalar ring entry type
 */
public sealed interface Matrix<T> permits RectangularMatrix, SquareMatrix {

    @SafeVarargs
    static <T> Matrix<T> of(int rows, int cols, Ring<T> ring, T... elements) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive: " + rows + "x" + cols);
        }
        return rows == cols
            ? SquareMatrix.of(rows, ring, elements)
            : RectangularMatrix.of(rows, cols, ring, elements);
    }

    static <T> Matrix<T> of(Ring<T> ring, T[][] matrix2D) {
        Objects.requireNonNull(ring, "ring cannot be null");
        Objects.requireNonNull(matrix2D, "matrix2D cannot be null");
        if (matrix2D.length == 0 || matrix2D[0] == null || matrix2D[0].length == 0) {
            throw new IllegalArgumentException("Matrix 2D array dimensions must be positive");
        }
        return matrix2D.length == matrix2D[0].length
            ? SquareMatrix.of(ring, matrix2D)
            : RectangularMatrix.of(ring, matrix2D);
    }

    static <T> Matrix<T> zeros(int rows, int cols, Ring<T> ring) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive: " + rows + "x" + cols);
        }
        return rows == cols
            ? SquareMatrix.zeros(rows, ring)
            : RectangularMatrix.zeros(rows, cols, ring);
    }

    static <T> SquareMatrix<T> identity(int n, Ring<T> ring) {
        return SquareMatrix.identity(n, ring);
    }

    int rows();

    int cols();

    Ring<T> ring();

    T get(int r, int c);

    Matrix<T> with(int r, int c, T val);

    default boolean isSquare() {
        return rows() == cols();
    }

    List<T> row(int r);

    List<T> col(int c);

    default Vector<T> rowVector(int r) {
        return Vector.of(ring(), row(r));
    }

    default Vector<T> colVector(int c) {
        return Vector.of(ring(), col(c));
    }

    /**
     * Extracts the submatrix covering half-open row range {@code [r0, r1)} and column range {@code [c0, c1)},
     * automatically returning a {@link SquareMatrix} when {@code (r1 - r0) == (c1 - c0)} or a {@link RectangularMatrix} otherwise.
     */
    Matrix<T> submatrix(int r0, int r1, int c0, int c1);

    Stream<List<T>> streamRows();

    Matrix<T> add(Matrix<T> other);

    Matrix<T> negate();

    default Matrix<T> subtract(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return add(other.negate());
    }

    Matrix<T> scale(T scalar);

    Matrix<T> multiply(Matrix<T> other);

    Vector<T> multiply(Vector<T> vector);

    Matrix<T> transpose();

    SquareMatrix<T> asSquareMatrix();

    /**
     * Computes the Reduced Row Echelon Form (RREF) over a {@link lock14.algebra.structure.DivisionRing} or {@link lock14.algebra.structure.Field}.
     */
    RrefResult<T> rref();

    default int rank() {
        return rref().rank();
    }

    default int nullity() {
        return rref().nullity();
    }

    /**
     * Computes a basis for the right nullspace (kernel) {@code { v in R^cols | this * v = 0 }}
     * over a {@link lock14.algebra.structure.DivisionRing} or {@link lock14.algebra.structure.Field}.
     */
    default List<Vector<T>> kernel() {
        return MatrixOps.kernel(rref());
    }
}
