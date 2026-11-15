package lock14.algebra.group;

import java.math.BigInteger;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.FiniteGroup;
import lock14.algebra.structure.GroupHomomorphism;

/**
 * The Symmetric Group {@code S_n} of all permutations on {@code n} elements ({@code n >= 1}).
 */
public final class SymmetricGroup implements FiniteGroup<Permutation> {

    private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);

    private final int degree;
    private final Set<Permutation> elementsView;

    /**
     * Constructs the symmetric group {@code S_n} of degree {@code n}.
     *
     * @param degree the number of permuted elements {@code n} ({@code degree >= 1})
     * @throws IllegalArgumentException if {@code degree <= 0}
     */
    public SymmetricGroup(int degree) {
        if (degree <= 0) {
            throw new IllegalArgumentException("Degree must be positive: " + degree);
        }
        this.degree = degree;
        this.elementsView = new ElementsSet();
    }

    /**
     * Returns the degree {@code n} of this symmetric group {@code S_n}.
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
        if (degree == 1) {
            return List.of(identity());
        }
        if (degree == 2) {
            return List.of(Permutation.transposition(2, 0, 1));
        }
        int[] cycle = new int[degree];
        for (int i = 0; i < degree - 1; i++) {
            cycle[i] = i + 1;
        }
        cycle[degree - 1] = 0;
        return List.of(Permutation.transposition(degree, 0, 1), new Permutation(cycle));
    }

    @Override
    public BigInteger order() {
        BigInteger fact = BigInteger.ONE;
        for (int i = 2; i <= degree; i++) {
            fact = fact.multiply(BigInteger.valueOf(i));
        }
        return fact;
    }

    @Override
    public BigInteger elementOrder(Permutation element) {
        checkInGroup(element);
        return element.order();
    }

    @Override
    public boolean contains(Permutation element) {
        Objects.requireNonNull(element, "element cannot be null");
        return element.degree() == degree;
    }

    @Override
    public boolean isAbelian() {
        return degree <= 2;
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

    /**
     * Returns the sign homomorphism {@code sgn: S_n -> C_2} mapping even permutations to {@code 0}
     * and odd permutations to {@code 1}.
     *
     * @return the parity homomorphism into {@link CyclicGroup} {@code C_2}
     */
    public GroupHomomorphism<Permutation, Integer> signHomomorphism() {
        return p -> {
            checkInGroup(p);
            return p.isEven() ? 0 : 1;
        };
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SymmetricGroup other && this.degree == other.degree;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(degree);
    }

    @Override
    public String toString() {
        return "S_" + degree;
    }

    private void checkInGroup(Permutation p) {
        Objects.requireNonNull(p, "permutation cannot be null");
        if (p.degree() != degree) {
            throw new IncompatibleAlgebraException(
                "Permutation degree " + p.degree() + " does not belong to S_" + degree
            );
        }
    }

    /**
     * Advances {@code a} in-place to the next lexicographic permutation, returning the parity flip
     * ({@code 1} if parity is unchanged, {@code -1} if parity flipped, or {@code 0} if exhausted).
     */
    static int advanceLexicographic(int[] a) {
        int i = a.length - 2;
        while (i >= 0 && a[i] >= a[i + 1]) {
            i--;
        }
        if (i < 0) {
            return 0;
        }
        int j = a.length - 1;
        while (a[j] <= a[i]) {
            j--;
        }
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
        int flips = 1;
        int suffixLen = a.length - (i + 1);
        flips += suffixLen / 2;
        for (int l = i + 1, r = a.length - 1; l < r; l++, r--) {
            tmp = a[l];
            a[l] = a[r];
            a[r] = tmp;
        }
        return (flips & 1) == 0 ? 1 : -1;
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
            return o instanceof Permutation p && SymmetricGroup.this.contains(p);
        }

        @Override
        public Iterator<Permutation> iterator() {
            return new Iterator<>() {
                private final int[] state;
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
                    hasMore = advanceLexicographic(state) != 0;
                    return current;
                }
            };
        }
    }
}
