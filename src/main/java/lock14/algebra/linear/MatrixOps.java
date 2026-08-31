package lock14.algebra.linear;

import java.util.Objects;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.structure.Field;
import lock14.algebra.structure.Ring;

/**
 * Pure functional numerical and algorithmic kernels for Matrix operations.
 */
final class MatrixOps {

    private MatrixOps() {}

    @SuppressWarnings("unchecked")
    static <T> Object[] add(Ring<T> ring, Object[] a, Object[] b) {
        Object[] res = new Object[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = ring.add((T) a[i], (T) b[i]);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    static <T> Object[] subtract(Ring<T> ring, Object[] a, Object[] b) {
        Object[] res = new Object[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = ring.subtract((T) a[i], (T) b[i]);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    static <T> Object[] negate(Ring<T> ring, Object[] a) {
        Object[] res = new Object[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = ring.negate((T) a[i]);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    static <T> Object[] scale(Ring<T> ring, Object[] a, T scalar) {
        Object[] res = new Object[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = ring.multiply(scalar, (T) a[i]);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    static <T> Object[] multiply(Ring<T> ring, Object[] a, Object[] b, int rowsA, int colsA, int colsB) {
        Object[] result = new Object[rowsA * colsB];

        // Cache-friendly i-k-j loop order
        for (int i = 0; i < rowsA; i++) {
            int outRowOffset = i * colsB;
            int thisRowOffset = i * colsA;
            for (int k = 0; k < colsA; k++) {
                T aVal = (T) a[thisRowOffset + k];
                if (ring.isZero(aVal)) continue;
                int otherRowOffset = k * colsB;
                for (int j = 0; j < colsB; j++) {
                    T bVal = (T) b[otherRowOffset + j];
                    if (ring.isZero(bVal)) continue;
                    T prod = ring.multiply(aVal, bVal);
                    int idx = outRowOffset + j;
                    result[idx] = result[idx] == null ? prod : ring.add((T) result[idx], prod);
                }
            }
        }
        T zero = ring.zero();
        for (int idx = 0; idx < result.length; idx++) {
            if (result[idx] == null) result[idx] = zero;
        }
        return result;
    }

    static Object[] transpose(Object[] data, int rows, int cols) {
        Object[] res = new Object[rows * cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                res[c * rows + r] = data[r * cols + c];
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    static <T> T trace(Ring<T> ring, Object[] data, int rows, int cols) {
        if (rows != cols) {
            throw new IllegalArgumentException("Trace is only defined for square matrices, but got " + rows + "x" + cols);
        }
        T sum = ring.zero();
        for (int i = 0; i < rows; i++) {
            sum = ring.add(sum, (T) data[i * cols + i]);
        }
        return sum;
    }

    @SuppressWarnings("unchecked")
    static <T> T determinant(Ring<T> ring, Object[] data, int n) {
        if (!(ring instanceof Field<T> field)) {
            return computeLaplaceDet(ring, data, n);
        }

        Object[][] a = new Object[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = data[i * n + j];
            }
        }

        T det = field.one();
        int sign = 1;

        for (int i = 0; i < n; i++) {
            int pivot = -1;
            for (int r = i; r < n; r++) {
                if (!field.isZero((T) a[r][i])) {
                    pivot = r;
                    break;
                }
            }
            if (pivot == -1) {
                return field.zero();
            }
            if (pivot != i) {
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

    @SuppressWarnings("unchecked")
    private static <T> T computeLaplaceDet(Ring<T> ring, Object[] data, int n) {
        if (n == 1) return (T) data[0];
        if (n == 2) {
            return ring.subtract(
                ring.multiply((T) data[0], (T) data[3]),
                ring.multiply((T) data[1], (T) data[2])
            );
        }
        T det = ring.zero();
        for (int j = 0; j < n; j++) {
            T elem = (T) data[j];
            if (ring.isZero(elem)) continue;
            Object[] minorData = minor(data, n, 0, j);
            T subDet = determinant(ring, minorData, n - 1);
            T term = ring.multiply(elem, subDet);
            det = (j % 2 == 0) ? ring.add(det, term) : ring.subtract(det, term);
        }
        return det;
    }

    private static Object[] minor(Object[] data, int n, int rowToRemove, int colToRemove) {
        Object[] minorData = new Object[(n - 1) * (n - 1)];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            if (i == rowToRemove) continue;
            for (int j = 0; j < n; j++) {
                if (j == colToRemove) continue;
                minorData[idx++] = data[i * n + j];
            }
        }
        return minorData;
    }

    @SuppressWarnings("unchecked")
    static <T> Object[] inverse(Ring<T> ring, Object[] data, int n) {
        if (!(ring instanceof Field<T> field)) {
            throw new UnsupportedOperationException("Matrix inversion currently requires a Field");
        }
        Object[][] aug = new Object[n][2 * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                aug[i][j] = data[i * n + j];
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
        return invData;
    }
}
