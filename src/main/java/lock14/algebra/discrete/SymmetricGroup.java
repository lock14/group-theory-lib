package lock14.algebra.discrete;

import java.util.Objects;
import lock14.algebra.structure.Group;

/**
 * The Symmetric Group S_n on n elements.
 */
public record SymmetricGroup(int degree) implements Group<Permutation> {

    public SymmetricGroup {
        if (degree <= 0) {
            throw new IllegalArgumentException("Degree must be positive: " + degree);
        }
    }

    @Override
    public Permutation identity() {
        return Permutation.identity(degree);
    }

    @Override
    public Permutation combine(Permutation left, Permutation right) {
        checkInGroup(left);
        checkInGroup(right);
        return left.combine(right);
    }

    @Override
    public Permutation inverse(Permutation element) {
        checkInGroup(element);
        return element.inverse();
    }

    private void checkInGroup(Permutation p) {
        Objects.requireNonNull(p, "permutation cannot be null");
        if (p.degree() != this.degree) {
            throw new IllegalArgumentException("Permutation degree " + p.degree() + " does not belong to S_" + degree);
        }
    }
}
