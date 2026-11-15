package lock14.algebra.linear;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lock14.algebra.structure.DivisionRing;
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

    /**
     * Constructs a {@code rows x cols} matrix over {@code ring} from row-major elements,
     * returning a {@link SquareMatrix} if {@code rows == cols} or a {@link RectangularMatrix} otherwise.
     *
     * @param <T>      scalar ring entry type
     * @param rows     the positive row count
     * @param cols     the positive column count
     * @param ring     the scalar entry ring
     * @param elements the {@code rows * cols} entries in row-major order
     * @return an immutable {@link Matrix}
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    static <T> Matrix<T> of(int rows, int cols, Ring<T> ring, T... elements) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive: " + rows + "x" + cols);
        }
        return rows == cols
            ? SquareMatrix.of(rows, ring, elements)
            : RectangularMatrix.of(rows, cols, ring, elements);
    }

    /**
     * Constructs a matrix over {@code ring} from a non-empty rectangular 2D array,
     * returning a {@link SquareMatrix} if square or a {@link RectangularMatrix} otherwise.
     *
     * @param <T>      scalar ring entry type
     * @param ring     the scalar entry ring
     * @param matrix2D non-empty rectangular 2D array of entries
     * @return an immutable {@link Matrix}
     */
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

    /**
     * Constructs the {@code rows x cols} zero matrix over {@code ring}.
     *
     * @param <T>  scalar ring entry type
     * @param rows the positive row count
     * @param cols the positive column count
     * @param ring the scalar entry ring
     * @return the {@code rows x cols} zero matrix
     */
    static <T> Matrix<T> zeros(int rows, int cols, Ring<T> ring) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive: " + rows + "x" + cols);
        }
        return rows == cols
            ? SquareMatrix.zeros(rows, ring)
            : RectangularMatrix.zeros(rows, cols, ring);
    }

    /**
     * Constructs the {@code n x n} multiplicative identity matrix over {@code ring}.
     *
     * @param <T>  scalar ring entry type
     * @param n    the positive matrix dimension
     * @param ring the scalar entry ring
     * @return the {@code n x n} identity {@link SquareMatrix}
     */
    static <T> SquareMatrix<T> identity(int n, Ring<T> ring) {
        return SquareMatrix.identity(n, ring);
    }

    /**
     * Returns the number of rows in this matrix.
     *
     * @return the positive row count
     */
    int rows();

    /**
     * Returns the number of columns in this matrix.
     *
     * @return the positive column count
     */
    int cols();

    /**
     * Returns the scalar {@link Ring} over which this matrix is defined.
     *
     * @return the scalar entry ring
     */
    Ring<T> ring();

    /**
     * Returns the entry at zero-based row {@code r} and column {@code c}.
     *
     * @param r zero-based row index in {@code [0, rows())}
     * @param c zero-based column index in {@code [0, cols())}
     * @return the entry at {@code (r, c)}
     */
    T get(int r, int c);

    /**
     * Returns a copy of this matrix with entry {@code (r, c)} replaced by {@code val}.
     *
     * @param r   zero-based row index in {@code [0, rows())}
     * @param c   zero-based column index in {@code [0, cols())}
     * @param val the replacement scalar entry
     * @return an updated matrix of the same shape
     */
    Matrix<T> with(int r, int c, T val);

    /**
     * Returns {@code true} if {@code rows() == cols()}.
     *
     * @return {@code true} if this matrix is a {@link SquareMatrix}
     */
    default boolean isSquare() {
        return rows() == cols();
    }

    /**
     * Returns an unmodifiable list containing the entries of zero-based row {@code r}.
     *
     * @param r zero-based row index in {@code [0, rows())}
     * @return the entries of row {@code r}
     */
    List<T> row(int r);

    /**
     * Returns an unmodifiable list containing the entries of zero-based column {@code c}.
     *
     * @param c zero-based column index in {@code [0, cols())}
     * @return the entries of column {@code c}
     */
    List<T> col(int c);

    /**
     * Returns zero-based row {@code r} as a {@link Vector}.
     *
     * @param r zero-based row index in {@code [0, rows())}
     * @return row {@code r} as a coordinate vector
     */
    default Vector<T> rowVector(int r) {
        return Vector.of(ring(), row(r));
    }

    /**
     * Returns zero-based column {@code c} as a {@link Vector}.
     *
     * @param c zero-based column index in {@code [0, cols())}
     * @return column {@code c} as a coordinate vector
     */
    default Vector<T> colVector(int c) {
        return Vector.of(ring(), col(c));
    }

    /**
     * Extracts the submatrix covering half-open row range {@code [r0, r1)} and column range {@code [c0, c1)},
     * automatically returning a {@link SquareMatrix} when {@code (r1 - r0) == (c1 - c0)} or a {@link RectangularMatrix} otherwise.
     *
     * @param r0 inclusive starting row index
     * @param r1 exclusive ending row index
     * @param c0 inclusive starting column index
     * @param c1 exclusive ending column index
     * @return the extracted {@code (r1 - r0) x (c1 - c0)} submatrix
     */
    Matrix<T> submatrix(int r0, int r1, int c0, int c1);

    /**
     * Returns a sequential {@link Stream} of unmodifiable row lists from top to bottom.
     *
     * @return a stream of matrix rows
     */
    Stream<List<T>> streamRows();

    /**
     * Computes the entrywise sum {@code this + other}.
     *
     * @param other a matrix of identical dimensions over the same ring
     * @return the sum matrix {@code this + other}
     */
    Matrix<T> add(Matrix<T> other);

    /**
     * Computes the entrywise additive inverse {@code -this}.
     *
     * @return the negated matrix {@code -this}
     */
    Matrix<T> negate();

    /**
     * Computes the entrywise difference {@code this - other}.
     *
     * @param other a matrix of identical dimensions over the same ring
     * @return the difference matrix {@code this - other}
     */
    default Matrix<T> subtract(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return add(other.negate());
    }

    /**
     * Left-scales every entry of this matrix by {@code scalar}.
     *
     * @param scalar the scalar multiplier in {@link #ring()}
     * @return the scaled matrix {@code scalar * this}
     */
    Matrix<T> scale(T scalar);

    /**
     * Computes the matrix product {@code this * other}.
     *
     * @param other a matrix over the same ring with {@code other.rows() == this.cols()}
     * @return the {@code this.rows() x other.cols()} product matrix
     */
    Matrix<T> multiply(Matrix<T> other);

    /**
     * Computes the matrix-column-vector product {@code this * vector}.
     *
     * @param vector a coordinate vector over the same ring of dimension {@link #cols()}
     * @return the product vector of dimension {@link #rows()}
     */
    Vector<T> multiply(Vector<T> vector);

    /**
     * Returns the transpose {@code this^T} of shape {@code cols() x rows()}.
     *
     * @return the transposed matrix
     */
    Matrix<T> transpose();

    /**
     * Computes the Reduced Row Echelon Form (RREF) over {@code divisionRing}.
     *
     * @param divisionRing the governing {@link DivisionRing} matching {@link #ring()}
     * @return the {@link RrefResult} containing the reduced matrix, pivot columns, rank, and nullity
     */
    RrefResult<T> rref(DivisionRing<T> divisionRing);

    /**
     * Returns the row and column rank of this matrix over {@code divisionRing}.
     *
     * @param divisionRing the governing {@link DivisionRing} matching {@link #ring()}
     * @return the matrix rank
     */
    default int rank(DivisionRing<T> divisionRing) {
        return rref(divisionRing).rank();
    }

    /**
     * Returns the right nullity ({@code cols() - rank(divisionRing)}) of this matrix over {@code divisionRing}.
     *
     * @param divisionRing the governing {@link DivisionRing} matching {@link #ring()}
     * @return the matrix nullity
     */
    default int nullity(DivisionRing<T> divisionRing) {
        return rref(divisionRing).nullity();
    }

    /**
     * Computes a basis for the right nullspace (kernel) {@code { v in D^cols | this * v = 0 }}
     * over {@code divisionRing}.
     *
     * @param divisionRing the governing {@link DivisionRing} matching {@link #ring()}
     * @return an unmodifiable list of basis vectors spanning the right kernel
     */
    default List<Vector<T>> kernel(DivisionRing<T> divisionRing) {
        return MatrixOps.kernel(divisionRing, rref(divisionRing));
    }
}
