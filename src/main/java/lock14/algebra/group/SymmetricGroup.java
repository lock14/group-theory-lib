package lock14.algebra.group;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.UndefinedOperationException;
import lock14.algebra.structure.FiniteGroup;
import lock14.algebra.structure.GroupHomomorphism;

/**
 * The Symmetric Group {@code S_n} of all permutations on {@code n} elements ({@code n >= 1}).
 */
public final class SymmetricGroup implements FiniteGroup<Permutation> {

    private static final int MAX_ENUMERABLE_DEGREE = 8;

    private final int degree;
    private volatile Set<Permutation> cachedElements;

    public SymmetricGroup(int degree) {
        if (degree <= 0) {
            throw new IllegalArgumentException("Degree must be positive: " + degree);
        }
        this.degree = degree;
    }

    public int degree() {
        return degree;
    }

    @Override
    public Set<Permutation> elements() {
        if (degree > MAX_ENUMERABLE_DEGREE) {
            throw new UndefinedOperationException(
                "Exhaustive enumeration of S_" + degree + " exceeds in-memory limit (degree > " + MAX_ENUMERABLE_DEGREE + ")"
            );
        }
        Set<Permutation> local = cachedElements;
        if (local == null) {
            synchronized (this) {
                local = cachedElements;
                if (local == null) {
                    local = enumerateAll(degree);
                    cachedElements = local;
                }
            }
        }
        return local;
    }

    @Override
    public long order() {
        if (degree > 20) {
            throw new ArithmeticException("Order of S_" + degree + " overflows 64-bit long");
        }
        long fact = 1L;
        for (int i = 2; i <= degree; i++) {
            fact = Math.multiplyExact(fact, i);
        }
        return fact;
    }

    @Override
    public long elementOrder(Permutation element) {
        checkInGroup(element);
        return element.order();
    }

    @Override
    public boolean contains(Permutation element) {
        Objects.requireNonNull(element, "element cannot be null");
        return element.degree() == degree;
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
     */
    public GroupHomomorphism<Permutation, Integer> signHomomorphism() {
        return p -> {
            checkInGroup(p);
            return p.isEven() ? 0 : 1;
        };
    }

    private void checkInGroup(Permutation p) {
        Objects.requireNonNull(p, "permutation cannot be null");
        if (p.degree() != degree) {
            throw new IncompatibleAlgebraException(
                "Permutation degree " + p.degree() + " does not belong to S_" + degree
            );
        }
    }

    static Set<Permutation> enumerateAll(int n) {
        Set<Permutation> out = new LinkedHashSet<>();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i;
        }
        out.add(Permutation.of(arr));
        while (nextPermutation(arr)) {
            out.add(Permutation.of(arr));
        }
        return Set.copyOf(out);
    }

    private static boolean nextPermutation(int[] a) {
        int i = a.length - 2;
        while (i >= 0 && a[i] >= a[i + 1]) {
            i--;
        }
        if (i < 0) {
            return false;
        }
        int j = a.length - 1;
        while (a[j] <= a[i]) {
            j--;
        }
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
        for (int l = i + 1, r = a.length - 1; l < r; l++, r--) {
            tmp = a[l];
            a[l] = a[r];
            a[r] = tmp;
        }
        return true;
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
}
