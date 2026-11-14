package lock14.algebra.group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import lock14.algebra.element.GroupElement;
import lock14.algebra.exceptions.IncompatibleAlgebraException;

/**
 * An immutable bijection on {@code {0, 1, ..., n-1}}, backed by an encapsulated array with
 * zero-copy internal operations.
 */
public final class Permutation implements GroupElement<Permutation> {

    private final int[] mapping;

    private Permutation(int[] trustedMapping) {
        this.mapping = trustedMapping;
    }

    public static Permutation of(int... mapping) {
        Objects.requireNonNull(mapping, "mapping cannot be null");
        if (mapping.length == 0) {
            throw new IllegalArgumentException("Permutation degree must be at least 1");
        }
        int n = mapping.length;
        boolean[] seen = new boolean[n];
        int[] copy = new int[n];
        for (int i = 0; i < n; i++) {
            int val = mapping[i];
            if (val < 0 || val >= n) {
                throw new IllegalArgumentException("Permutation element " + val + " out of bounds for degree " + n);
            }
            if (seen[val]) {
                throw new IllegalArgumentException("Duplicate element in permutation: " + val);
            }
            seen[val] = true;
            copy[i] = val;
        }
        return new Permutation(copy);
    }

    public static Permutation identity(int degree) {
        if (degree <= 0) {
            throw new IllegalArgumentException("Degree must be positive: " + degree);
        }
        int[] id = new int[degree];
        for (int i = 0; i < degree; i++) {
            id[i] = i;
        }
        return new Permutation(id);
    }

    public static Permutation transposition(int degree, int i, int j) {
        if (degree <= 0) {
            throw new IllegalArgumentException("Degree must be positive: " + degree);
        }
        if (i < 0 || i >= degree || j < 0 || j >= degree) {
            throw new IndexOutOfBoundsException("Indices (" + i + ", " + j + ") out of range for degree " + degree);
        }
        int[] map = new int[degree];
        for (int k = 0; k < degree; k++) {
            map[k] = k;
        }
        map[i] = j;
        map[j] = i;
        return new Permutation(map);
    }

    /**
     * Constructs a permutation of {@code degree} by composing a list of cycles (left-to-right application:
     * {@code (c_1)(c_2)...(c_k)}).
     */
    public static Permutation fromCycles(int degree, List<List<Integer>> cycles) {
        if (degree <= 0) {
            throw new IllegalArgumentException("Degree must be positive: " + degree);
        }
        Objects.requireNonNull(cycles, "cycles cannot be null");
        Permutation acc = identity(degree);
        for (List<Integer> cycle : cycles) {
            Objects.requireNonNull(cycle, "cycle cannot be null");
            if (cycle.isEmpty()) {
                continue;
            }
            boolean[] seenInCycle = new boolean[degree];
            int[] single = new int[degree];
            for (int i = 0; i < degree; i++) {
                single[i] = i;
            }
            for (int idx = 0; idx < cycle.size(); idx++) {
                int from = Objects.requireNonNull(cycle.get(idx), "cycle entry cannot be null");
                int to = Objects.requireNonNull(cycle.get((idx + 1) % cycle.size()), "cycle entry cannot be null");
                if (from < 0 || from >= degree || to < 0 || to >= degree) {
                    throw new IndexOutOfBoundsException("Cycle entry out of bounds for degree " + degree);
                }
                if (seenInCycle[from]) {
                    throw new IllegalArgumentException("Duplicate element " + from + " within a single cycle");
                }
                seenInCycle[from] = true;
                single[from] = to;
            }
            acc = acc.combine(new Permutation(single));
        }
        return acc;
    }

    public int degree() {
        return mapping.length;
    }

    public int apply(int i) {
        if (i < 0 || i >= mapping.length) {
            throw new IndexOutOfBoundsException("Index " + i + " out of bounds for degree " + mapping.length);
        }
        return mapping[i];
    }

    public int[] toArray() {
        return mapping.clone();
    }

    public List<Integer> toList() {
        List<Integer> list = new ArrayList<>(mapping.length);
        for (int v : mapping) {
            list.add(v);
        }
        return List.copyOf(list);
    }

    @Override
    public Permutation combine(Permutation other) {
        Objects.requireNonNull(other, "other cannot be null");
        if (this.mapping.length != other.mapping.length) {
            throw new IncompatibleAlgebraException(
                "Cannot compose permutations of different degrees: " + mapping.length + " vs " + other.mapping.length
            );
        }
        int n = mapping.length;
        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            res[i] = this.mapping[other.mapping[i]];
        }
        return new Permutation(res);
    }

    @Override
    public Permutation inverse() {
        int n = mapping.length;
        int[] inv = new int[n];
        for (int i = 0; i < n; i++) {
            inv[this.mapping[i]] = i;
        }
        return new Permutation(inv);
    }

    public int sign() {
        int n = mapping.length;
        int numCycles = 0;
        BitSet visited = new BitSet(n);
        for (int i = 0; i < n; i++) {
            if (!visited.get(i)) {
                numCycles++;
                int curr = i;
                while (!visited.get(curr)) {
                    visited.set(curr);
                    curr = mapping[curr];
                }
            }
        }
        return ((n - numCycles) % 2 == 0) ? 1 : -1;
    }

    public boolean isEven() {
        return sign() == 1;
    }

    public boolean isOdd() {
        return sign() == -1;
    }

    public List<List<Integer>> cycles() {
        int n = mapping.length;
        List<List<Integer>> cycleList = new ArrayList<>();
        BitSet visited = new BitSet(n);
        for (int i = 0; i < n; i++) {
            if (!visited.get(i)) {
                List<Integer> cycle = new ArrayList<>();
                int curr = i;
                while (!visited.get(curr)) {
                    visited.set(curr);
                    cycle.add(curr);
                    curr = mapping[curr];
                }
                if (cycle.size() > 1) {
                    cycleList.add(List.copyOf(cycle));
                }
            }
        }
        return List.copyOf(cycleList);
    }

    /**
     * Computes the exact multiplicative order of this permutation (LCM of its disjoint cycle lengths).
     */
    public long order() {
        long ord = 1L;
        for (List<Integer> cycle : cycles()) {
            long len = cycle.size();
            ord = Math.multiplyExact(ord / gcd(ord, len), len);
        }
        return ord;
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Permutation other && Arrays.equals(mapping, other.mapping);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mapping);
    }

    @Override
    public String toString() {
        List<List<Integer>> c = cycles();
        if (c.isEmpty()) {
            return "(id)";
        }
        StringBuilder sb = new StringBuilder();
        for (List<Integer> cycle : c) {
            sb.append('(');
            for (int i = 0; i < cycle.size(); i++) {
                if (i > 0) sb.append(' ');
                sb.append(cycle.get(i));
            }
            sb.append(')');
        }
        return sb.toString();
    }
}
