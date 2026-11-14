package lock14.algebra.linear;

import java.util.Objects;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.group.Permutation;
import lock14.algebra.structure.Field;

/**
 * Partial-pivoted LU Decomposition {@code P * A = L * U} of a {@link SquareMatrix} over a {@link Field} {@code F},
 * where {@code L} is unit lower-triangular, {@code U} is upper-triangular, and {@code P} is a row permutation.
 *
 * @param <T>            scalar field element type
 * @param rowPermutation the row permutation {@code P} applied to {@code A}
 * @param lower          the unit lower-triangular factor {@code L}
 * @param upper          the upper-triangular factor {@code U}
 * @param swapSign       the parity sign ({@code +1} or {@code -1}) of the row swaps in {@code rowPermutation}
 * @param field          the underlying scalar field {@code F}
 */
public record LuDecomposition<T>(
    Permutation rowPermutation,
    SquareMatrix<T> lower,
    SquareMatrix<T> upper,
    int swapSign,
    Field<T> field
) {
    /**
     * Validates the LU decomposition factors and swap sign ({@code +1} or {@code -1}).
     *
     * @param rowPermutation the row permutation {@code P}
     * @param lower          the unit lower-triangular factor {@code L}
     * @param upper          the upper-triangular factor {@code U}
     * @param swapSign       the row-swap parity ({@code +1} or {@code -1})
     * @param field          the underlying scalar field
     */
    public LuDecomposition {
        Objects.requireNonNull(rowPermutation, "rowPermutation cannot be null");
        Objects.requireNonNull(lower, "lower cannot be null");
        Objects.requireNonNull(upper, "upper cannot be null");
        Objects.requireNonNull(field, "field cannot be null");
        if (swapSign != 1 && swapSign != -1) {
            throw new IllegalArgumentException("swapSign must be +1 or -1");
        }
    }

    /**
     * Constructs the explicit {@code n x n} permutation matrix {@code P} satisfying {@code P * A = L * U}.
     *
     * @return the {@code n x n} permutation {@link SquareMatrix} {@code P}
     */
    public SquareMatrix<T> permutationMatrix() {
        int n = lower.dimension();
        Object[] data = new Object[n * n];
        T zero = field.zero();
        T one = field.one();
        for (int i = 0; i < data.length; i++) {
            data[i] = zero;
        }
        for (int r = 0; r < n; r++) {
            int col = rowPermutation.apply(r);
            data[r * n + col] = one;
        }
        return new SquareMatrix<>(n, field, data);
    }

    /**
     * Returns {@code true} if every diagonal entry of {@link #upper()} is non-zero in {@link #field()}.
     *
     * @return {@code true} if the decomposed matrix is nonsingular
     */
    public boolean isNonsingular() {
        int n = upper.dimension();
        for (int i = 0; i < n; i++) {
            if (field.isZero(upper.get(i, i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Computes {@code det(A) = swapSign * prod_i U[i, i]} in {@code O(n)} scalar multiplications.
     *
     * @return the determinant of the decomposed matrix in {@link #field()}
     */
    public T determinant() {
        int n = upper.dimension();
        T prod = field.one();
        for (int i = 0; i < n; i++) {
            T diag = upper.get(i, i);
            if (field.isZero(diag)) {
                return field.zero();
            }
            prod = field.multiply(prod, diag);
        }
        return swapSign < 0 ? field.negate(prod) : prod;
    }

    /**
     * Solves {@code A * x = b} in {@code O(n^2)} operations via forward and backward substitution.
     *
     * @param b the right-hand side vector of dimension {@code n} over {@link #field()}
     * @return the unique solution vector {@code x}
     * @throws NonInvertibleElementException if the decomposed matrix is singular
     */
    @SuppressWarnings("unchecked")
    public Vector<T> solve(Vector<T> b) {
        Objects.requireNonNull(b, "b cannot be null");
        int n = upper.dimension();
        if (b.dimension() != n) {
            throw new DimensionMismatchException("RHS vector dimension " + b.dimension() + " does not match " + n);
        }
        if (!Objects.equals(b.ring(), field)) {
            throw new IncompatibleAlgebraException("RHS vector scalar ring does not match LU field");
        }

        Object[] y = new Object[n];
        for (int i = 0; i < n; i++) {
            T sum = b.get(rowPermutation.apply(i));
            for (int j = 0; j < i; j++) {
                sum = field.subtract(sum, field.multiply(lower.get(i, j), (T) y[j]));
            }
            y[i] = sum;
        }

        Object[] x = new Object[n];
        for (int i = n - 1; i >= 0; i--) {
            T diag = upper.get(i, i);
            if (field.isZero(diag)) {
                throw new NonInvertibleElementException("Matrix is singular; cannot solve A * x = b");
            }
            T sum = (T) y[i];
            for (int j = i + 1; j < n; j++) {
                sum = field.subtract(sum, field.multiply(upper.get(i, j), (T) x[j]));
            }
            x[i] = field.divide(sum, diag);
        }
        return new Vector<>(n, field, x);
    }
}
