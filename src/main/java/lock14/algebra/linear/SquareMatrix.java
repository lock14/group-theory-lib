package lock14.algebra.linear;

import java.util.Arrays;
import java.util.Objects;
import lock14.algebra.element.RingElement;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.structure.Field;
import lock14.algebra.structure.Ring;

/**
 * An immutable n x n Square Matrix over a Ring R.
 *
 * @param <T> the entry type in Ring R
 */
public final class SquareMatrix<T> extends DenseMatrix<T> implements RingElement<SquareMatrix<T>> {

    public SquareMatrix(int n, Ring<T> ring, T[] elements) {
        if (n <= 0) {
            throw new IllegalArgumentException("Square matrix dimension must be positive: " + n);
        }
        if (elements == null || elements.length != n * n) {
            throw new DimensionMismatchException("Element count does not match square dimension " + n + "x" + n);
        }
        super(n, n, ring, elements);
    }

    private SquareMatrix(int n, Ring<T> ring, Object[] data, boolean trusted) {
        super(n, n, ring, data, trusted);
    }

    public static <T> SquareMatrix<T> zeros(int n, Ring<T> ring) {
        if (n <= 0) throw new IllegalArgumentException("Dimension must be positive: " + n);
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[n * n];
        Arrays.fill(arr, ring.zero());
        return new SquareMatrix<>(n, ring, arr, true);
    }

    public static <T> SquareMatrix<T> identity(int n, Ring<T> ring) {
        if (n <= 0) throw new IllegalArgumentException("Dimension must be positive: " + n);
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[n * n];
        Arrays.fill(arr, ring.zero());
        for (int i = 0; i < n; i++) {
            arr[i * n + i] = ring.one();
        }
        return new SquareMatrix<>(n, ring, arr, true);
    }

    public static <T> SquareMatrix<T> of(Ring<T> ring, T[][] matrix2D) {
        Objects.requireNonNull(matrix2D, "matrix2D cannot be null");
        int n = matrix2D.length;
        if (n == 0 || matrix2D[0].length != n) {
            throw new IllegalArgumentException("Provided 2D array is not square (" + n + "x" + (n == 0 ? 0 : matrix2D[0].length) + ")");
        }
        DenseMatrix<T> dense = DenseMatrix.of(ring, matrix2D);
        return new SquareMatrix<>(n, ring, dense.data, true);
    }

    public int dimension() {
        return rows;
    }

    @Override
    public SquareMatrix<T> add(SquareMatrix<T> other) {
        DenseMatrix<T> res = super.add(other);
        return new SquareMatrix<>(rows, ring, res.data, true);
    }

    @Override
    public SquareMatrix<T> negate() {
        DenseMatrix<T> res = super.negate();
        return new SquareMatrix<>(rows, ring, res.data, true);
    }

    @Override
    public SquareMatrix<T> subtract(SquareMatrix<T> other) {
        DenseMatrix<T> res = super.subtract(other);
        return new SquareMatrix<>(rows, ring, res.data, true);
    }

    @Override
    public SquareMatrix<T> multiply(SquareMatrix<T> other) {
        DenseMatrix<T> res = super.multiply(other);
        return new SquareMatrix<>(rows, ring, res.data, true);
    }

    @Override
    public SquareMatrix<T> scale(T scalar) {
        DenseMatrix<T> res = super.scale(scalar);
        return new SquareMatrix<>(rows, ring, res.data, true);
    }

    @Override
    public SquareMatrix<T> transpose() {
        DenseMatrix<T> res = super.transpose();
        return new SquareMatrix<>(rows, ring, res.data, true);
    }

    /**
     * Computes the determinant of this matrix when defined over a {@link Field}.
     * Uses Gaussian elimination with partial pivoting in O(n^3) operations.
     */
    @SuppressWarnings("unchecked")
    public T determinant() {
        if (!(ring instanceof Field<T> field)) {
            // Laplace expansion for small general rings
            return computeLaplaceDet();
        }

        int n = rows;
        Object[][] a = new Object[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = get(i, j);
            }
        }

        T det = field.one();
        int sign = 1;

        for (int i = 0; i < n; i++) {
            // Find pivot
            int pivot = -1;
            for (int r = i; r < n; r++) {
                if (!field.isZero((T) a[r][i])) {
                    pivot = r;
                    break;
                }
            }
            if (pivot == -1) {
                return field.zero(); // Singular matrix
            }
            if (pivot != i) {
                // Swap rows
                Object[] temp = a[i];
                a[i] = a[pivot];
                a[pivot] = temp;
                sign = -sign;
            }

            T pivotVal = (T) a[i][i];
            det = field.multiply(det, pivotVal);

            for (int r = i + 1; r < n; r++) {
                if (!field.isZero((T) a[r][i])) {
                    T factor = field.divide((T) a[r][i], pivotVal);
                    for (int c = i; c < n; c++) {
                        a[r][c] = field.subtract((T) a[r][c], field.multiply(factor, (T) a[i][c]));
                    }
                }
            }
        }

        return sign < 0 ? field.negate(det) : det;
    }

    /**
     * Inverts this matrix using Gauss-Jordan elimination over a {@link Field}.
     */
    @SuppressWarnings("unchecked")
    public SquareMatrix<T> inverse() {
        if (!(ring instanceof Field<T> field)) {
            throw new UnsupportedOperationException("Matrix inversion currently requires a Field");
        }
        int n = rows;
        Object[][] aug = new Object[n][2 * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                aug[i][j] = get(i, j);
                aug[i][j + n] = (i == j) ? field.one() : field.zero();
            }
        }

        for (int i = 0; i < n; i++) {
            int pivot = -1;
            for (int r = i; r < n; r++) {
                if (!field.isZero((T) aug[r][i])) {
                    pivot = r;
                    break;
                }
            }
            if (pivot == -1) {
                throw new NonInvertibleElementException("Matrix is singular (determinant is zero)");
            }
            if (pivot != i) {
                Object[] temp = aug[i];
                aug[i] = aug[pivot];
                aug[pivot] = temp;
            }

            T pivotVal = (T) aug[i][i];
            T invPivot = field.reciprocal(pivotVal);
            for (int c = 0; c < 2 * n; c++) {
                aug[i][c] = field.multiply((T) aug[i][c], invPivot);
            }

            for (int r = 0; r < n; r++) {
                if (r != i && !field.isZero((T) aug[r][i])) {
                    T factor = (T) aug[r][i];
                    for (int c = 0; c < 2 * n; c++) {
                        aug[r][c] = field.subtract((T) aug[r][c], field.multiply(factor, (T) aug[i][c]));
                    }
                }
            }
        }

        Object[] invData = new Object[n * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                invData[i * n + j] = aug[i][j + n];
            }
        }
        return new SquareMatrix<>(n, ring, invData, true);
    }

    private T computeLaplaceDet() {
        int n = rows;
        if (n == 1) return get(0, 0);
        if (n == 2) {
            return ring.subtract(
                ring.multiply(get(0, 0), get(1, 1)),
                ring.multiply(get(0, 1), get(1, 0))
            );
        }
        T det = ring.zero();
        for (int j = 0; j < n; j++) {
            T elem = get(0, j);
            if (ring.isZero(elem)) continue;
            SquareMatrix<T> sub = minor(0, j);
            T subDet = sub.determinant();
            T term = ring.multiply(elem, subDet);
            det = (j % 2 == 0) ? ring.add(det, term) : ring.subtract(det, term);
        }
        return det;
    }

    @SuppressWarnings("unchecked")
    private SquareMatrix<T> minor(int rowToRemove, int colToRemove) {
        int n = rows;
        Object[] minorData = new Object[(n - 1) * (n - 1)];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            if (i == rowToRemove) continue;
            for (int j = 0; j < n; j++) {
                if (j == colToRemove) continue;
                minorData[idx++] = get(i, j);
            }
        }
        return new SquareMatrix<>(n - 1, ring, minorData, true);
    }
}
