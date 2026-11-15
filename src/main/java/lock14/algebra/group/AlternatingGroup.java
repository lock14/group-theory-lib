package lock14.algebra.group;

import java.math.BigInteger;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.FiniteGroup;

/**
 * The Alternating Group {@code A_n} of all even permutations on {@code n} elements ({@code n >= 1}).
 */
public final class AlternatingGroup implements FiniteGroup<Permutation> {

    private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);

    private final int degree;
    private final Set<Permutation> elementsView;

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
        this.elementsView = new ElementsSet();
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
        return elementsView;
    }

    @Override
    public Collection<Permutation> generators() {
        if (degree <= 2) {
            return List.of(identity());
        }
        List<Permutation> gens = new ArrayList<>(degree - 2);
        for (int k = 2; k < degree; k++) {
            gens.add(Permutation.fromCycles(degree, List.of(List.of(0, 1, k))));
        }
        return List.copyOf(gens);
    }

    @Override
    public BigInteger order() {
        if (degree <= 2) {
            return BigInteger.ONE;
        }
        BigInteger factOver2 = BigInteger.ONE;
        for (int i = 3; i <= degree; i++) {
            factOver2 = factOver2.multiply(BigInteger.valueOf(i));
        }
        return factOver2;
    }

    @Override
    public BigInteger elementOrder(Permutation element) {
        checkInGroup(element);
        return element.order();
    }

    @Override
    public boolean contains(Permutation element) {
        Objects.requireNonNull(element, "element cannot be null");
        return element.degree() == degree && element.isEven();
    }

    @Override
    public boolean isAbelian() {
        return degree <= 3;
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

    private final class ElementsSet extends AbstractSet<Permutation> {
        @Override
        public int size() {
            return order().min(MAX_INT).intValue();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return o instanceof Permutation p && AlternatingGroup.this.contains(p);
        }

        @Override
        public Iterator<Permutation> iterator() {
            return new Iterator<>() {
                private final int[] state;
                private int sign = 1;
                private boolean hasMore = true;

                {
                    state = new int[degree];
                    for (int i = 0; i < degree; i++) {
                        state[i] = i;
                    }
                }

                @Override
                public boolean hasNext() {
                    return hasMore;
                }

                @Override
                public Permutation next() {
                    if (!hasMore) {
                        throw new NoSuchElementException();
                    }
                    Permutation current = new Permutation(state.clone());
                    while (true) {
                        int factor = SymmetricGroup.advanceLexicographic(state);
                        if (factor == 0) {
                            hasMore = false;
                            break;
                        }
                        sign *= factor;
                        if (sign == 1) {
                            break;
                        }
                    }
                    return current;
                }
            };
        }
    }
}
