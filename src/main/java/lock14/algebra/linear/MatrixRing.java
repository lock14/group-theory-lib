package lock14.algebra.linear;

import java.util.Objects;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.Ring;

/**
 * The Matrix Ring {@code M_n(R)} of {@code n x n} {@link SquareMatrix} elements over an exact {@link Ring} {@code R}.
 *
 * @param <T>       scalar ring element type
 * @param dimension the positive matrix dimension {@code n}
 * @param entryRing the underlying scalar entry ring {@code R}
 */
public record MatrixRing<T>(int dimension, Ring<T> entryRing) implements Ring<SquareMatrix<T>> {

    /**
     * Validates that {@code dimension} is positive and {@code entryRing} is non-null.
     *
     * @param dimension the positive matrix dimension {@code n}
     * @param entryRing the underlying scalar entry ring
     */
    public MatrixRing {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Matrix ring dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(entryRing, "entryRing cannot be null");
    }

    /**
     * Constructs an {@code n x n} {@link SquareMatrix} in this matrix ring from {@code n * n} row-major elements.
     *
     * @param elements the {@code dimension * dimension} entries in row-major order
     * @return an {@code n x n} {@link SquareMatrix} in {@code M_n(R)}
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public final SquareMatrix<T> of(T... elements) {
        return SquareMatrix.of(dimension, entryRing, elements);
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

    @Override
    public boolean isZero(SquareMatrix<T> element) {
        checkInRing(element);
        return Ring.super.isZero(element);
    }

    @Override
    public boolean isOne(SquareMatrix<T> element) {
        checkInRing(element);
        return Ring.super.isOne(element);
    }

    private void checkInRing(SquareMatrix<T> m) {
        Objects.requireNonNull(m, "matrix cannot be null");
        if (m.dimension() != this.dimension) {
            throw new DimensionMismatchException(
                "Matrix dimension " + m.dimension() + " does not match ring dimension " + this.dimension
            );
        }
        if (!Objects.equals(m.ring(), this.entryRing)) {
            throw new IncompatibleAlgebraException(
                "Matrix entry ring " + m.ring() + " does not match ring entryRing " + this.entryRing
            );
        }
    }
}
