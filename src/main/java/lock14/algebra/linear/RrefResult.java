package lock14.algebra.linear;

import java.util.List;
import java.util.Objects;

/**
 * Result of Reduced Row Echelon Form (RREF) elimination on a {@link Matrix}.
 *
 * @param <T>          scalar division-ring / field entry type
 * @param reduced      the matrix in reduced row echelon form
 * @param pivotColumns zero-based column indices of the leading {@code 1} pivots in strictly ascending order
 * @param rank         the number of non-zero pivot rows ({@code pivotColumns.size()})
 * @param nullity      the dimension of the right nullspace ({@code reduced.cols() - rank})
 */
public record RrefResult<T>(
    Matrix<T> reduced,
    List<Integer> pivotColumns,
    int rank,
    int nullity
) {
    /**
     * Validates the reduced matrix, copies {@code pivotColumns} defensively, and checks the rank-nullity invariant.
     *
     * @param reduced      the matrix in reduced row echelon form
     * @param pivotColumns zero-based indices of the pivot columns
     * @param rank         the matrix rank
     * @param nullity      the matrix right nullity
     */
    public RrefResult {
        Objects.requireNonNull(reduced, "reduced cannot be null");
        Objects.requireNonNull(pivotColumns, "pivotColumns cannot be null");
        pivotColumns = List.copyOf(pivotColumns);
        if (rank < 0 || nullity < 0 || rank + nullity != reduced.cols()) {
            throw new IllegalArgumentException("Rank-nullity invariant violated");
        }
    }
}
