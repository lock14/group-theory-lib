package lock14.algebra.group;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.UndefinedOperationException;
import lock14.algebra.structure.FiniteGroup;

/**
 * The Alternating Group {@code A_n} of all even permutations on {@code n} elements ({@code n >= 1}).
 */
public final class AlternatingGroup implements FiniteGroup<Permutation> {

    private static final int MAX_ENUMERABLE_DEGREE = 8;

    private final int degree;
    private volatile Set<Permutation> cachedElements;

    /**
     * Constructs the alternating group {@code A_n} of degree {@code n}.
     *
     * @param degree the number of permuted elements {@code n} ({@code degree >= 1})
     * @throws IllegalArgumentException if {@code degree <= 0}
     */
    public AlternatingGroup(int degree) {
        if (degree <= 0) {
            throw new IllegalArgumentException("Degree must be positive: " + degree);
        }
        this.degree = degree;
    }

    /**
     * Returns the degree {@code n} of this alternating group {@code A_n}.
     *
     * @return the degree {@code n}
     */
    public int degree() {
        return degree;
    }

    @Override
    public Set<Permutation> elements() {
        if (degree > MAX_ENUMERABLE_DEGREE) {
            throw new UndefinedOperationException(
                "Exhaustive enumeration of A_" + degree + " exceeds in-memory limit"
            );
        }
        Set<Permutation> local = cachedElements;
        if (local == null) {
            synchronized (this) {
                local = cachedElements;
                if (local == null) {
                    Set<Permutation> evens = new LinkedHashSet<>();
                    for (Permutation p : SymmetricGroup.enumerateAll(degree)) {
                        if (p.isEven()) {
                            evens.add(p);
                        }
                    }
                    local = Set.copyOf(evens);
                    cachedElements = local;
                }
            }
        }
        return local;
    }

    @Override
    public long order() {
        if (degree == 1) {
            return 1L;
        }
        if (degree > 20) {
            throw new ArithmeticException("Order of A_" + degree + " overflows 64-bit long");
        }
        long factOver2 = 1L;
        for (int i = 3; i <= degree; i++) {
            factOver2 = Math.multiplyExact(factOver2, i);
        }
        return factOver2;
    }

    @Override
    public long elementOrder(Permutation element) {
        checkInGroup(element);
        return element.order();
    }

    @Override
    public boolean contains(Permutation element) {
        Objects.requireNonNull(element, "element cannot be null");
        return element.degree() == degree && element.isEven();
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
        if (p.degree() != degree) {
            throw new IncompatibleAlgebraException(
                "Permutation degree " + p.degree() + " does not belong to A_" + degree
            );
        }
        if (!p.isEven()) {
            throw new IncompatibleAlgebraException("Odd permutation " + p + " does not belong to A_" + degree);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AlternatingGroup other && this.degree == other.degree;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(degree);
    }

    @Override
    public String toString() {
        return "A_" + degree;
    }
}
