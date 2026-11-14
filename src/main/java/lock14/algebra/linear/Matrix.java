package lock14.algebra.linear;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lock14.algebra.structure.Ring;

/**
 * Canonical, disjoint Algebraic Data Type (Sum Type) representing an immutable Matrix over a {@link Ring} R:
 * <ul>
 *   <li>{@link SquareMatrix} exclusively represents {@code n x n} square matrices ({@code n >= 1}).</li>
 *   <li>{@link DenseMatrix} exclusively represents {@code m x n} rectangular matrices ({@code m, n >= 1, m != n}).</li>
 * </ul>
 *
 * @param <T> the scalar entry type in Ring R
 */
public sealed interface Matrix<T> permits DenseMatrix, SquareMatrix {

    /**
     * Canonical factory creating a {@link SquareMatrix} when {@code rows == cols}
     * and a rectangular {@link DenseMatrix} when {@code rows != cols}.
     */
    @SafeVarargs
    static <T> Matrix<T> of(int rows, int cols, Ring<T> ring, T... elements) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive: " + rows + "x" + cols);
        }
        return rows == cols
            ? SquareMatrix.of(rows, ring, elements)
            : DenseMatrix.of(rows, cols, ring, elements);
    }

    /**
     * Canonical 2D-array factory creating a {@link SquareMatrix} when {@code rows == cols}
     * and a rectangular {@link DenseMatrix} when {@code rows != cols}.
     */
    static <T> Matrix<T> of(Ring<T> ring, T[][] matrix2D) {
        Objects.requireNonNull(ring, "ring cannot be null");
        Objects.requireNonNull(matrix2D, "matrix2D cannot be null");
        if (matrix2D.length == 0 || matrix2D[0] == null || matrix2D[0].length == 0) {
            throw new IllegalArgumentException("Matrix 2D array dimensions must be positive");
        }
        return matrix2D.length == matrix2D[0].length
            ? SquareMatrix.of(ring, matrix2D)
            : DenseMatrix.of(ring, matrix2D);
    }

    /**
     * Canonical zero-matrix factory creating a {@link SquareMatrix} when {@code rows == cols}
     * and a rectangular {@link DenseMatrix} when {@code rows != cols}.
     */
    static <T> Matrix<T> zeros(int rows, int cols, Ring<T> ring) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive: " + rows + "x" + cols);
        }
        return rows == cols
            ? SquareMatrix.zeros(rows, ring)
            : DenseMatrix.zeros(rows, cols, ring);
    }

    /**
     * Returns the {@code n x n} multiplicative identity {@link SquareMatrix}.
     */
    static <T> SquareMatrix<T> identity(int n, Ring<T> ring) {
        return SquareMatrix.identity(n, ring);
    }

    /**
     * @return number of rows in this matrix ({@code >= 1})
     */
    int rows();

    /**
     * @return number of columns in this matrix ({@code >= 1})
     */
    int cols();

    /**
     * @return the algebraic Ring governing matrix entries
     */
    Ring<T> ring();

    /**
     * Gets the element at row {@code r} and column {@code c}.
     */
    T get(int r, int c);

    /**
     * Returns a copy of this matrix with the entry at {@code (r, c)} updated to {@code val}.
     */
    Matrix<T> with(int r, int c, T val);

    /**
     * @return true if and only if this matrix is a {@link SquareMatrix} ({@code rows == cols})
     */
    default boolean isSquare() {
        return rows() == cols();
    }

    /**
     * Extracts row {@code r} as an immutable list of elements.
     */
    List<T> row(int r);

    /**
     * Extracts column {@code c} as an immutable list of elements.
     */
    List<T> col(int c);

    /**
     * Java 25 Stream Gatherer to stream rows of the matrix without pre-allocating full row objects.
     */
    Stream<List<T>> streamRows();

    /**
     * Adds another matrix of identical shape and ring: {@code this + other}.
     */
    Matrix<T> add(Matrix<T> other);

    /**
     * Returns the additive inverse of this matrix: {@code -this}.
     */
    Matrix<T> negate();

    /**
     * Subtracts another matrix of identical shape and ring: {@code this - other}.
     */
    default Matrix<T> subtract(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return add(other.negate());
    }

    /**
     * Scales every entry of this matrix on the left by {@code scalar}: {@code scalar * this}.
     */
    Matrix<T> scale(T scalar);

    /**
     * Multiplies this matrix with another compatible matrix ({@code this.cols() == other.rows()}).
     * Automatically returns a {@link SquareMatrix} when {@code this.rows() == other.cols()},
     * or a {@link DenseMatrix} otherwise.
     */
    Matrix<T> multiply(Matrix<T> other);

    /**
     * Computes the matrix-vector product {@code this * vector}.
     */
    Vector<T> multiply(Vector<T> vector);

    /**
     * Transposes this matrix, swapping rows and columns.
     */
    Matrix<T> transpose();

    /**
     * Returns this matrix as a {@link SquareMatrix} if {@code isSquare()} is true.
     *
     * @throws IllegalArgumentException if this matrix is rectangular
     */
    SquareMatrix<T> asSquareMatrix();
}
