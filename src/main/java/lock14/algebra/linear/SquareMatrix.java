package lock14.algebra.linear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Gatherers;
import java.util.stream.Stream;
import lock14.algebra.element.ModuleElement;
import lock14.algebra.element.RingElement;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.structure.Ring;

/**
 * An immutable {@code n x n} Square Matrix over an exact {@link Ring} {@code R},
 * backed by an encapsulated flat row-major array with zero-copy internal arithmetic.
 *
 * @param <T> scalar ring entry type
 */
public final class SquareMatrix<T>
    implements Matrix<T>, RingElement<SquareMatrix<T>>, ModuleElement<SquareMatrix<T>, T> {

    private final int dimension;
    private final Ring<T> ring;
    private final Object[] data;

    SquareMatrix(int dimension, Ring<T> ring, Object[] trustedData) {
        this.dimension = dimension;
        this.ring = ring;
        this.data = trustedData;
    }

    private static <T> SquareMatrix<T> validated(int dimension, Ring<T> ring, Object[] rawData, boolean cloneArray) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Square matrix dimension must be positive: " + dimension);
        }
        Objects.requireNonNull(ring, "ring cannot be null");
        Objects.requireNonNull(rawData, "elements cannot be null");
        int expectedLength;
        try {
            expectedLength = Math.multiplyExact(dimension, dimension);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Matrix dimension overflows array capacity: " + dimension, ex);
        }
        if (rawData.length != expectedLength) {
            throw new DimensionMismatchException(
                "Element count " + rawData.length + " does not match square dimension " + dimension + "x" + dimension
            );
        }
        Object[] target = cloneArray ? new Object[expectedLength] : rawData;
        for (int i = 0; i < expectedLength; i++) {
            Object elem = Objects.requireNonNull(rawData[i], "Matrix element cannot be null at index " + i);
            if (cloneArray) {
                target[i] = elem;
            }
        }
        return new SquareMatrix<>(dimension, ring, target);
    }

    /**
     * Constructs an {@code n x n} square matrix over {@code ring} from {@code n * n} row-major elements.
     *
     * @param <T>      scalar ring entry type
     * @param n        the positive matrix dimension
     * @param ring     the scalar entry ring
     * @param elements the {@code n * n} entries in row-major order
     * @return an immutable {@code n x n} {@link SquareMatrix}
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> SquareMatrix<T> of(int n, Ring<T> ring, T... elements) {
        return validated(n, ring, elements, true);
    }

    /**
     * Constructs an {@code n x n} square matrix over {@code ring} from a square 2D array.
     *
     * @param <T>      scalar ring entry type
     * @param ring     the scalar entry ring
     * @param matrix2D non-empty {@code n x n} 2D array of entries
     * @return an immutable {@link SquareMatrix}
     */
    public static <T> SquareMatrix<T> of(Ring<T> ring, T[][] matrix2D) {
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] flat = MatrixOps.flatten2D(matrix2D);
        int n = matrix2D.length;
        int cols = matrix2D[0].length;
        if (cols != n) {
            throw new IllegalArgumentException("Provided 2D array is not square (" + n + "x" + cols + ")");
        }
        return validated(n, ring, flat, false);
    }

    /**
     * Constructs the {@code n x n} zero matrix over {@code ring}.
     *
     * @param <T>  scalar ring entry type
     * @param n    the positive matrix dimension
     * @param ring the scalar entry ring
     * @return the {@code n x n} zero {@link SquareMatrix}
     */
    public static <T> SquareMatrix<T> zeros(int n, Ring<T> ring) {
        if (n <= 0) throw new IllegalArgumentException("Dimension must be positive: " + n);
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[Math.multiplyExact(n, n)];
        Arrays.fill(arr, ring.zero());
        return new SquareMatrix<>(n, ring, arr);
    }

    /**
     * Constructs the {@code n x n} multiplicative identity matrix over {@code ring}.
     *
     * @param <T>  scalar ring entry type
     * @param n    the positive matrix dimension
     * @param ring the scalar entry ring
     * @return the {@code n x n} identity {@link SquareMatrix}
     */
    public static <T> SquareMatrix<T> identity(int n, Ring<T> ring) {
        if (n <= 0) throw new IllegalArgumentException("Dimension must be positive: " + n);
        Objects.requireNonNull(ring, "ring cannot be null");
        Object[] arr = new Object[Math.multiplyExact(n, n)];
        Arrays.fill(arr, ring.zero());
        T one = ring.one();
        for (int i = 0; i < n; i++) {
            arr[i * n + i] = one;
        }
        return new SquareMatrix<>(n, ring, arr);
    }

    /**
     * Returns the side dimension {@code n} of this {@code n x n} square matrix.
     *
     * @return the positive dimension {@code n}
     */
    public int dimension() {
        return dimension;
    }

    @Override
    public int rows() {
        return dimension;
    }

    @Override
    public int cols() {
        return dimension;
    }

    @Override
    public Ring<T> ring() {
        return ring;
    }

    Object[] rawData() {
        return data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int r, int c) {
        if (r < 0 || r >= dimension || c < 0 || c >= dimension) {
            throw new IndexOutOfBoundsException("Matrix index (" + r + ", " + c + ") out of bounds for " + dimension + "x" + dimension);
        }
        return (T) data[r * dimension + c];
    }

    @Override
    public SquareMatrix<T> with(int r, int c, T val) {
        Objects.requireNonNull(val, "val cannot be null");
        get(r, c);
        Object[] copy = data.clone();
        copy[r * dimension + c] = val;
        return new SquareMatrix<>(dimension, ring, copy);
    }

    @Override
    public Matrix<T> submatrix(int r0, int r1, int c0, int c1) {
        return MatrixOps.submatrix(ring, data, dimension, dimension, r0, r1, c0, c1);
    }

    @Override
    public SquareMatrix<T> add(SquareMatrix<T> other) {
        checkSameDimension(other);
        checkSameRing(other.ring);
        return new SquareMatrix<>(dimension, ring, MatrixOps.add(ring, data, other.data));
    }

    @Override
    public SquareMatrix<T> add(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return switch (other) {
            case SquareMatrix<T> sm -> add(sm);
            case RectangularMatrix<T> rm -> {
                checkSameRing(rm.ring());
                throw new DimensionMismatchException(this.dimension, this.dimension, rm.rows(), rm.cols());
            }
        };
    }

    @Override
    public SquareMatrix<T> negate() {
        return new SquareMatrix<>(dimension, ring, MatrixOps.negate(ring, data));
    }

    @Override
    public SquareMatrix<T> subtract(SquareMatrix<T> other) {
        checkSameDimension(other);
        checkSameRing(other.ring);
        return new SquareMatrix<>(dimension, ring, MatrixOps.subtract(ring, data, other.data));
    }

    @Override
    public SquareMatrix<T> subtract(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return switch (other) {
            case SquareMatrix<T> sm -> subtract(sm);
            case RectangularMatrix<T> rm -> {
                checkSameRing(rm.ring());
                throw new DimensionMismatchException(this.dimension, this.dimension, rm.rows(), rm.cols());
            }
        };
    }

    @Override
    public SquareMatrix<T> multiply(SquareMatrix<T> other) {
        checkSameDimension(other);
        checkSameRing(other.ring);
        return new SquareMatrix<>(dimension, ring, MatrixOps.multiply(ring, data, other.data, dimension, dimension, dimension));
    }

    /**
     * Multiplies this {@code n x n} square matrix on the right by an {@code n x m} {@link RectangularMatrix} ({@code m != n}),
     * producing an {@code n x m} {@link RectangularMatrix}.
     *
     * @param other a rectangular matrix over the same ring with {@code other.rows() == this.dimension()}
     * @return the {@code n x m} product {@link RectangularMatrix}
     */
    public RectangularMatrix<T> multiply(RectangularMatrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        checkSameRing(other.ring());
        if (this.dimension != other.rows()) {
            throw new DimensionMismatchException(
                "Cannot multiply matrix " + dimension + "x" + dimension + " with " + other.rows() + "x" + other.cols()
            );
        }
        return new RectangularMatrix<>(
            this.dimension,
            other.cols(),
            ring,
            MatrixOps.multiply(ring, data, other.rawData(), this.dimension, this.dimension, other.cols())
        );
    }

    @Override
    public Matrix<T> multiply(Matrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        return switch (other) {
            case SquareMatrix<T> sm -> multiply(sm);
            case RectangularMatrix<T> rm -> multiply(rm);
        };
    }

    @Override
    public Vector<T> multiply(Vector<T> vector) {
        Objects.requireNonNull(vector, "vector cannot be null");
        checkSameRing(vector.ring());
        if (this.dimension != vector.dimension()) {
            throw new DimensionMismatchException(
                "Cannot multiply matrix " + dimension + "x" + dimension + " with vector of dimension " + vector.dimension()
            );
        }
        return new Vector<>(this.dimension, ring, MatrixOps.multiplyColumnVector(ring, data, vector.rawData(), this.dimension, this.dimension));
    }

    @Override
    public SquareMatrix<T> scale(T scalar) {
        Objects.requireNonNull(scalar, "scalar cannot be null");
        return new SquareMatrix<>(dimension, ring, MatrixOps.scale(ring, data, scalar));
    }

    @Override
    public SquareMatrix<T> transpose() {
        return new SquareMatrix<>(dimension, ring, MatrixOps.transpose(data, dimension, dimension));
    }

    /**
     * Computes the matrix trace {@code sum_i this[i, i]} in {@link #ring()}.
     *
     * @return the sum of the main diagonal entries
     */
    public T trace() {
        return MatrixOps.trace(ring, data, dimension);
    }

    /**
     * Computes the exact determinant {@code det(this)} over any {@link lock14.algebra.structure.CommutativeRing}
     * (via LU over fields, fraction-free Bareiss elimination over Euclidean domains, or Bird's division-free
     * algorithm over general commutative rings).
     *
     * @return the determinant in {@link #ring()}
     */
    public T determinant() {
        return MatrixOps.determinant(ring, data, dimension);
    }

    @Override
    public RrefResult<T> rref() {
        return MatrixOps.rref(ring, data, dimension, dimension);
    }

    /**
     * Computes the partial-pivoted LU decomposition {@code P * this = L * U} when {@link #ring()} is a {@link lock14.algebra.structure.Field}.
     *
     * @return the {@link LuDecomposition} of this matrix
     */
    public LuDecomposition<T> lu() {
        return MatrixOps.lu(ring, data, dimension);
    }

    /**
     * Solves {@code this * x = rhs} over any {@link lock14.algebra.structure.DivisionRing} or {@link lock14.algebra.structure.Field}.
     *
     * @param rhs the right-hand side vector of dimension {@link #dimension()}
     * @return the unique solution vector {@code x}
     * @throws NonInvertibleElementException if this matrix is singular
     */
    public Vector<T> solve(Vector<T> rhs) {
        Objects.requireNonNull(rhs, "rhs cannot be null");
        checkSameRing(rhs.ring());
        if (rhs.dimension() != dimension) {
            throw new DimensionMismatchException(
                "RHS vector dimension " + rhs.dimension() + " does not match matrix dimension " + dimension
            );
        }
        Object[] sol = MatrixOps.solve(ring, data, rhs.rawData(), dimension, 1);
        return new Vector<>(dimension, ring, sol);
    }

    /**
     * Solves {@code this * X = rhs} for multiple right-hand sides over any {@link lock14.algebra.structure.DivisionRing}.
     *
     * @param rhs the right-hand side matrix with {@code rhs.rows() == this.dimension()}
     * @return the unique solution matrix {@code X} of shape {@code dimension() x rhs.cols()}
     * @throws NonInvertibleElementException if this matrix is singular
     */
    public Matrix<T> solve(Matrix<T> rhs) {
        Objects.requireNonNull(rhs, "rhs cannot be null");
        checkSameRing(rhs.ring());
        if (rhs.rows() != dimension) {
            throw new DimensionMismatchException(
                "RHS matrix rows " + rhs.rows() + " do not match matrix dimension " + dimension
            );
        }
        Object[] rhsData = switch (rhs) {
            case SquareMatrix<T> sm -> sm.rawData();
            case RectangularMatrix<T> rm -> rm.rawData();
        };
        Object[] sol = MatrixOps.solve(ring, data, rhsData, dimension, rhs.cols());
        return dimension == rhs.cols()
            ? new SquareMatrix<>(dimension, ring, sol)
            : new RectangularMatrix<>(dimension, rhs.cols(), ring, sol);
    }

    /**
     * Attempts to compute the multiplicative inverse {@code this^-1} over a {@link lock14.algebra.structure.DivisionRing}.
     *
     * @return an {@link Optional} containing {@code this^-1} if nonsingular, or {@link Optional#empty()} if singular
     */
    public Optional<SquareMatrix<T>> tryInverse() {
        try {
            return Optional.of(inverse());
        } catch (NonInvertibleElementException ex) {
            return Optional.empty();
        }
    }

    /**
     * Computes the exact multiplicative inverse {@code this^-1} over a {@link lock14.algebra.structure.DivisionRing}.
     *
     * @return the inverse {@link SquareMatrix} {@code this^-1}
     * @throws NonInvertibleElementException if this matrix is singular
     */
    public SquareMatrix<T> inverse() {
        SquareMatrix<T> id = identity(dimension, ring);
        Object[] invData = MatrixOps.solve(ring, data, id.data, dimension, dimension);
        return new SquareMatrix<>(dimension, ring, invData);
    }

    @Override
    public List<T> row(int r) {
        if (r < 0 || r >= dimension) throw new IndexOutOfBoundsException("Row " + r + " out of bounds");
        List<T> res = new ArrayList<>(dimension);
        for (int c = 0; c < dimension; c++) {
            res.add(get(r, c));
        }
        return List.copyOf(res);
    }

    @Override
    public List<T> col(int c) {
        if (c < 0 || c >= dimension) throw new IndexOutOfBoundsException("Col " + c + " out of bounds");
        List<T> res = new ArrayList<>(dimension);
        for (int r = 0; r < dimension; r++) {
            res.add(get(r, c));
        }
        return List.copyOf(res);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<List<T>> streamRows() {
        return Arrays.stream((T[]) data).gather(Gatherers.windowFixed(dimension));
    }

    @Override
    public SquareMatrix<T> asSquareMatrix() {
        return this;
    }

    private void checkSameDimension(SquareMatrix<T> other) {
        Objects.requireNonNull(other, "other matrix cannot be null");
        if (this.dimension != other.dimension) {
            throw new DimensionMismatchException(this.dimension, this.dimension, other.dimension, other.dimension);
        }
    }

    private void checkSameRing(Ring<T> otherRing) {
        if (!Objects.equals(this.ring, otherRing)) {
            throw new IncompatibleAlgebraException("Cannot operate on matrices/vectors over different rings");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SquareMatrix<?> other
            && dimension == other.dimension
            && Objects.equals(ring, other.ring)
            && Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(dimension, ring) + Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(dimension).append('x').append(dimension).append(" SquareMatrix]\n");
        for (int i = 0; i < dimension; i++) {
            sb.append("  [");
            for (int j = 0; j < dimension; j++) {
                if (j > 0) sb.append(", ");
                sb.append(get(i, j));
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}
