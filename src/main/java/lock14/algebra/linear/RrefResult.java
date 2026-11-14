package lock14.algebra.linear;

import java.util.List;
import java.util.Objects;

/**
 * Result of Reduced Row Echelon Form (RREF) elimination on a {@link Matrix}.
 *
 * @param <T> scalar division-ring / field entry type
 */
public record RrefResult<T>(
    Matrix<T> reduced,
    List<Integer> pivotColumns,
    int rank,
    int nullity
) {
    public RrefResult {
        Objects.requireNonNull(reduced, "reduced cannot be null");
        Objects.requireNonNull(pivotColumns, "pivotColumns cannot be null");
        pivotColumns = List.copyOf(pivotColumns);
        if (rank < 0 || nullity < 0 || rank + nullity != reduced.cols()) {
            throw new IllegalArgumentException("Rank-nullity invariant violated");
        }
    }
}
