package lock14.algebra.linear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.exceptions.UndefinedOperationException;
import lock14.algebra.group.Permutation;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.DivisionRing;
import lock14.algebra.structure.EuclideanDomain;
import lock14.algebra.structure.Field;
import lock14.algebra.structure.Ring;

/**
 * Pure exact algorithmic kernels for {@link Matrix}, {@link SquareMatrix}, and {@link RectangularMatrix}.
 */
final class MatrixOps {

    private MatrixOps() {}

    static <T> Object[] flatten2D(T[][] matrix2D) {
        Objects.requireNonNull(matrix2D, "matrix2D cannot be null");
        int r = matrix2D.length;
        if (r == 0 || matrix2D[0] == null || matrix2D[0].length == 0) {
            throw new IllegalArgumentException("Provided 2D array dimensions must be positive");
        }
        int c = matrix2D[0].length;
        Object[] flat = new Object[Math.multiplyExact(r, c)];
        for (int i = 0; i < r; i++) {
            T[] row = Objects.requireNonNull(matrix2D[i], "Matrix row cannot be null at index " + i);
            if (row.length != c) {
                throw new IllegalArgumentException("Ragged 2D array: row " + i + " has length " + row.length + " expected " + c);
            }
            for (int j = 0; j < c; j++) {
                flat[i * c + j] = Objects.requireNonNull(row[j], "Element cannot be null at (" + i + ", " + j + ")");
            }
        }
        return flat;
    }

    static <T> Matrix<T> submatrix(Ring<T> ring, Object[] data, int rows, int cols, int r0, int r1, int c0, int c1) {
        if (r0 < 0 || r1 > rows || r0 >= r1 || c0 < 0 || c1 > cols || c0 >= c1) {
            throw new IndexOutOfBoundsException(
                String.format("Invalid submatrix range [%d, %d) x [%d, %d) for %dx%d matrix", r0, r1, c0, c1, rows, cols)
            );
        }
        int subRows = r1 - r0;
        int subCols = c1 - c0;
        Object[] sub = new Object[subRows * subCols];
        for (int r = 0; r < subRows; r++) {
            System.arraycopy(data, (r0 + r) * cols + c0, sub, r * subCols, subCols);
        }
        return subRows == subCols
            ? new SquareMatrix<>(subRows, ring, sub)
            : new RectangularMatrix<>(subRows, subCols, ring, sub);
    }

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

    @SuppressWarnings("unchecked")
    static <T> Object[] multiplyColumnVector(Ring<T> ring, Object[] matData, Object[] vecData, int rows, int cols) {
        Object[] res = new Object[rows];
        for (int r = 0; r < rows; r++) {
            T sum = ring.zero();
            int rowOffset = r * cols;
            for (int c = 0; c < cols; c++) {
                T aVal = (T) matData[rowOffset + c];
                if (ring.isZero(aVal)) continue;
                T vVal = (T) vecData[c];
                if (ring.isZero(vVal)) continue;
                sum = ring.add(sum, ring.multiply(aVal, vVal));
            }
            res[r] = sum;
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    static <T> Object[] multiplyRowVector(Ring<T> ring, Object[] vecData, Object[] matData, int rows, int cols) {
        Object[] res = new Object[cols];
        for (int i = 0; i < rows; i++) {
            T vVal = (T) vecData[i];
            if (ring.isZero(vVal)) continue;
            int rowOffset = i * cols;
            for (int j = 0; j < cols; j++) {
                T mVal = (T) matData[rowOffset + j];
                if (ring.isZero(mVal)) continue;
                T prod = ring.multiply(vVal, mVal);
                res[j] = res[j] == null ? prod : ring.add((T) res[j], prod);
            }
        }
        T zero = ring.zero();
        for (int j = 0; j < cols; j++) {
            if (res[j] == null) res[j] = zero;
        }
        return res;
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
    static <T> T trace(Ring<T> ring, Object[] data, int n) {
        T sum = ring.zero();
        for (int i = 0; i < n; i++) {
            sum = ring.add(sum, (T) data[i * n + i]);
        }
        return sum;
    }

    @SuppressWarnings("unchecked")
    static <T> T determinant(Ring<T> ring, Object[] data, int n) {
        if (!(ring instanceof CommutativeRing<T> commRing)) {
            throw new UndefinedOperationException("Determinant is not defined over non-commutative rings: " + ring);
        }
        if (n == 1) {
            return (T) data[0];
        }
        if (n == 2) {
            return commRing.subtract(
                commRing.multiply((T) data[0], (T) data[3]),
                commRing.multiply((T) data[1], (T) data[2])
            );
        }
        if (ring instanceof Field<T> field) {
            return lu(field, data, n).determinant();
        }
        if (ring instanceof EuclideanDomain<T> domain) {
            return determinantBareiss(domain, data, n);
        }
        return determinantBird(commRing, data, n);
    }

    @SuppressWarnings("unchecked")
    static <T> LuDecomposition<T> lu(Ring<T> ring, Object[] data, int n) {
        if (!(ring instanceof Field<T> field)) {
            throw new UndefinedOperationException("LU decomposition requires a Field, got: " + ring);
        }
        Object[][] u = new Object[n][n];
        Object[][] l = new Object[n][n];
        T zero = field.zero();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                u[i][j] = data[i * n + j];
                l[i][j] = zero;
            }
        }

        int[] perm = new int[n];
        for (int i = 0; i < n; i++) {
            perm[i] = i;
        }
        int swapSign = 1;

        for (int k = 0; k < n; k++) {
            int pivot = -1;
            for (int r = k; r < n; r++) {
                if (!field.isZero((T) u[r][k])) {
                    pivot = r;
                    break;
                }
            }
            if (pivot == -1) {
                l[k][k] = field.one();
                continue;
            }
            if (pivot != k) {
                Object[] tmpU = u[k];
                u[k] = u[pivot];
                u[pivot] = tmpU;

                for (int c = 0; c < k; c++) {
                    Object tmpL = l[k][c];
                    l[k][c] = l[pivot][c];
                    l[pivot][c] = tmpL;
                }

                int tmpP = perm[k];
                perm[k] = perm[pivot];
                perm[pivot] = tmpP;
                swapSign = -swapSign;
            }

            l[k][k] = field.one();
            T pivotVal = (T) u[k][k];
            for (int i = k + 1; i < n; i++) {
                T entry = (T) u[i][k];
                if (!field.isZero(entry)) {
                    T factor = field.divide(entry, pivotVal);
                    l[i][k] = factor;
                    u[i][k] = zero;
                    for (int j = k + 1; j < n; j++) {
                        u[i][j] = field.subtract((T) u[i][j], field.multiply(factor, (T) u[k][j]));
                    }
                }
            }
        }

        Object[] flatL = new Object[n * n];
        Object[] flatU = new Object[n * n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(l[i], 0, flatL, i * n, n);
            System.arraycopy(u[i], 0, flatU, i * n, n);
        }
        return new LuDecomposition<>(
            Permutation.of(perm),
            new SquareMatrix<>(n, field, flatL),
            new SquareMatrix<>(n, field, flatU),
            swapSign,
            field
        );
    }

    @SuppressWarnings("unchecked")
    static <T> RrefResult<T> rref(Ring<T> ring, Object[] data, int rows, int cols) {
        if (!(ring instanceof DivisionRing<T> divRing)) {
            throw new UndefinedOperationException("RREF requires a DivisionRing or Field, got: " + ring);
        }
        Object[][] m = new Object[rows][cols];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(data, r * cols, m[r], 0, cols);
        }

        List<Integer> pivots = new ArrayList<>();
        int leadRow = 0;

        for (int col = 0; col < cols && leadRow < rows; col++) {
            int pivot = -1;
            for (int r = leadRow; r < rows; r++) {
                if (!divRing.isZero((T) m[r][col])) {
                    pivot = r;
                    break;
                }
            }
            if (pivot == -1) {
                continue;
            }
            if (pivot != leadRow) {
                Object[] tmp = m[leadRow];
                m[leadRow] = m[pivot];
                m[pivot] = tmp;
            }

            T invPivot = divRing.reciprocal((T) m[leadRow][col]);
            for (int c = col; c < cols; c++) {
                m[leadRow][c] = divRing.multiply(invPivot, (T) m[leadRow][c]);
            }

            for (int r = 0; r < rows; r++) {
                if (r != leadRow && !divRing.isZero((T) m[r][col])) {
                    T factor = (T) m[r][col];
                    for (int c = col; c < cols; c++) {
                        m[r][c] = divRing.subtract((T) m[r][c], divRing.multiply(factor, (T) m[leadRow][c]));
                    }
                }
            }
            pivots.add(col);
            leadRow++;
        }

        Object[] flat = new Object[rows * cols];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(m[r], 0, flat, r * cols, cols);
        }
        Matrix<T> reduced = rows == cols
            ? new SquareMatrix<>(rows, ring, flat)
            : new RectangularMatrix<>(rows, cols, ring, flat);
        return new RrefResult<>(reduced, pivots, pivots.size(), cols - pivots.size());
    }

    static <T> List<Vector<T>> kernel(RrefResult<T> rref) {
        Matrix<T> red = rref.reduced();
        @SuppressWarnings("unchecked")
        DivisionRing<T> divRing = (DivisionRing<T>) red.ring();
        int cols = red.cols();
        List<Integer> pivots = rref.pivotColumns();
        boolean[] isPivot = new boolean[cols];
        for (int p : pivots) {
            isPivot[p] = true;
        }

        List<Vector<T>> basis = new ArrayList<>(rref.nullity());
        T zero = divRing.zero();
        T one = divRing.one();
        for (int freeCol = 0; freeCol < cols; freeCol++) {
            if (isPivot[freeCol]) {
                continue;
            }
            Object[] vec = new Object[cols];
            Arrays.fill(vec, zero);
            vec[freeCol] = one;
            for (int i = 0; i < pivots.size(); i++) {
                int pivotCol = pivots.get(i);
                vec[pivotCol] = divRing.negate(red.get(i, freeCol));
            }
            basis.add(new Vector<>(cols, divRing, vec));
        }
        return List.copyOf(basis);
    }

    /**
     * Left-row Gauss-Jordan linear system solver for {@code A * X = B} over any {@link DivisionRing} {@code D}.
     */
    @SuppressWarnings("unchecked")
    static <T> Object[] solve(Ring<T> ring, Object[] aData, Object[] bData, int n, int rhsCols) {
        if (!(ring instanceof DivisionRing<T> divRing)) {
            throw new UndefinedOperationException("Linear system solving / inversion requires a DivisionRing: " + ring);
        }
        int totalCols = n + rhsCols;
        Object[][] aug = new Object[n][totalCols];
        for (int i = 0; i < n; i++) {
            System.arraycopy(aData, i * n, aug[i], 0, n);
            System.arraycopy(bData, i * rhsCols, aug[i], n, rhsCols);
        }

        for (int i = 0; i < n; i++) {
            int pivot = -1;
            for (int r = i; r < n; r++) {
                if (!divRing.isZero((T) aug[r][i])) {
                    pivot = r;
                    break;
                }
            }
            if (pivot == -1) {
                throw new NonInvertibleElementException("Matrix is singular");
            }
            if (pivot != i) {
                Object[] tmp = aug[i];
                aug[i] = aug[pivot];
                aug[pivot] = tmp;
            }

            T invPivot = divRing.reciprocal((T) aug[i][i]);
            for (int c = i; c < totalCols; c++) {
                aug[i][c] = divRing.multiply(invPivot, (T) aug[i][c]);
            }

            for (int r = 0; r < n; r++) {
                if (r != i && !divRing.isZero((T) aug[r][i])) {
                    T factor = (T) aug[r][i];
                    for (int c = i; c < totalCols; c++) {
                        aug[r][c] = divRing.subtract((T) aug[r][c], divRing.multiply(factor, (T) aug[i][c]));
                    }
                }
            }
        }

        Object[] sol = new Object[n * rhsCols];
        for (int i = 0; i < n; i++) {
            System.arraycopy(aug[i], n, sol, i * rhsCols, rhsCols);
        }
        return sol;
    }

    @SuppressWarnings("unchecked")
    private static <T> T determinantBareiss(EuclideanDomain<T> domain, Object[] data, int n) {
        Object[][] m = new Object[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(data, i * n, m[i], 0, n);
        }

        int sign = 1;
        T prevPivot = domain.one();

        for (int k = 0; k < n - 1; k++) {
            if (domain.isZero((T) m[k][k])) {
                int swapRow = -1;
                for (int r = k + 1; r < n; r++) {
                    if (!domain.isZero((T) m[r][k])) {
                        swapRow = r;
                        break;
                    }
                }
                if (swapRow == -1) {
                    return domain.zero();
                }
                Object[] temp = m[k];
                m[k] = m[swapRow];
                m[swapRow] = temp;
                sign = -sign;
            }

            T pivot = (T) m[k][k];
            for (int i = k + 1; i < n; i++) {
                for (int j = k + 1; j < n; j++) {
                    T num = domain.subtract(
                        domain.multiply(pivot, (T) m[i][j]),
                        domain.multiply((T) m[i][k], (T) m[k][j])
                    );
                    m[i][j] = domain.divide(num, prevPivot);
                }
            }
            prevPivot = pivot;
        }

        T det = (T) m[n - 1][n - 1];
        return sign < 0 ? domain.negate(det) : det;
    }

    @SuppressWarnings("unchecked")
    private static <T> T determinantBird(CommutativeRing<T> ring, Object[] data, int n) {
        Object[][] a = new Object[n][n];
        Object[][] x = new Object[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Object val = data[i * n + j];
                a[i][j] = val;
                x[i][j] = val;
            }
        }

        for (int step = 1; step < n; step++) {
            Object[][] mu = new Object[n][n];
            T diagSuffixSum = ring.zero();
            for (int i = n - 1; i >= 0; i--) {
                mu[i][i] = ring.negate(diagSuffixSum);
                diagSuffixSum = ring.add(diagSuffixSum, (T) x[i][i]);
                for (int j = i + 1; j < n; j++) {
                    mu[i][j] = x[i][j];
                }
            }

            if (step == n - 1) {
                T finalEntry = ring.zero();
                for (int k = 0; k < n; k++) {
                    T muVal = (T) mu[0][k];
                    if (ring.isZero(muVal)) continue;
                    T aVal = (T) a[k][0];
                    if (ring.isZero(aVal)) continue;
                    finalEntry = ring.add(finalEntry, ring.multiply(muVal, aVal));
                }
                return (n % 2 == 1) ? finalEntry : ring.negate(finalEntry);
            }

            Object[][] nextX = new Object[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    T sum = ring.zero();
                    for (int k = i; k < n; k++) {
                        T muVal = (T) mu[i][k];
                        if (ring.isZero(muVal)) continue;
                        T aVal = (T) a[k][j];
                        if (ring.isZero(aVal)) continue;
                        sum = ring.add(sum, ring.multiply(muVal, aVal));
                    }
                    nextX[i][j] = sum;
                }
            }
            x = nextX;
        }

        T val = (T) x[0][0];
        return (n % 2 == 1) ? val : ring.negate(val);
    }
}
