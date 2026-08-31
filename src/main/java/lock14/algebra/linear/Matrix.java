package lock14.algebra.linear;

import java.util.List;
import java.util.stream.Stream;
import lock14.algebra.structure.Ring;

/**
 * Algebraic Data Type (Sum Type) representing an immutable Matrix over a Ring R.
 *
 * @param <T> the scalar entry type in Ring R
 */
public sealed interface Matrix<T> permits DenseMatrix, SquareMatrix {

    /**
     * @return number of rows in this matrix
     */
    int rows();

    /**
     * @return number of columns in this matrix
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
     * @return true if the matrix is square (rows == cols)
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
     * Computes the trace (sum of main diagonal elements) of this matrix.
     */
    T trace();

    /**
     * Transposes this matrix, swapping rows and columns.
     */
    Matrix<T> transpose();

    /**
     * Converts this matrix to a {@link SquareMatrix} if {@code isSquare()} is true.
     *
     * @throws IllegalArgumentException if this matrix is not square
     */
    SquareMatrix<T> asSquareMatrix();
}
