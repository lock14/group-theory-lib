package lock14.algebra.linear;

import java.util.Objects;
import lock14.algebra.structure.Ring;

/**
 * The Non-Commutative Matrix Ring M_n(R) of n x n square matrices over a Ring R.
 *
 * @param <T> the scalar entry type
 */
public record MatrixRing<T>(int dimension, Ring<T> entryRing) implements Ring<SquareMatrix<T>> {

    public MatrixRing {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Matrix ring dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(entryRing, "entryRing cannot be null");
    }

    @Override
    public SquareMatrix<T> zero() {
        return SquareMatrix.zeros(dimension, entryRing);
    }

    @Override
    public SquareMatrix<T> one() {
        return SquareMatrix.identity(dimension, entryRing);
    }

    @Override
    public SquareMatrix<T> add(SquareMatrix<T> a, SquareMatrix<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.add(b);
    }

    @Override
    public SquareMatrix<T> negate(SquareMatrix<T> a) {
        checkInRing(a);
        return a.negate();
    }

    @Override
    public SquareMatrix<T> subtract(SquareMatrix<T> a, SquareMatrix<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.subtract(b);
    }

    @Override
    public SquareMatrix<T> multiply(SquareMatrix<T> a, SquareMatrix<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.multiply(b);
    }

    private void checkInRing(SquareMatrix<T> m) {
        Objects.requireNonNull(m, "matrix cannot be null");
        if (m.dimension() != this.dimension) {
            throw new IllegalArgumentException("Matrix dimension " + m.dimension() + " does not match ring dimension " + this.dimension);
        }
    }
}
