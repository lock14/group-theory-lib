package lock14.algebra.discrete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import lock14.algebra.element.GroupElement;

/**
 * An immutable Permutation of the finite set {0, 1, ..., n-1}.
 */
public record Permutation(int[] mapping) implements GroupElement<Permutation> {

    public Permutation {
        Objects.requireNonNull(mapping, "mapping cannot be null");
        if (mapping.length == 0) {
            throw new IllegalArgumentException("Permutation degree must be at least 1");
        }
        int n = mapping.length;
        boolean[] seen = new boolean[n];
        for (int val : mapping) {
            if (val < 0 || val >= n) {
                throw new IllegalArgumentException("Permutation element " + val + " out of bounds for degree " + n);
            }
            if (seen[val]) {
                throw new IllegalArgumentException("Duplicate element in permutation: " + val);
            }
            seen[val] = true;
        }
        mapping = mapping.clone(); // Defensive copy
    }

    public static Permutation of(int... mapping) {
        return new Permutation(mapping);
    }

    public static Permutation identity(int n) {
        if (n <= 0) throw new IllegalArgumentException("Degree must be positive: " + n);
        int[] id = new int[n];
        for (int i = 0; i < n; i++) id[i] = i;
        return new Permutation(id);
    }

    public static Permutation transposition(int n, int i, int j) {
        if (i < 0 || i >= n || j < 0 || j >= n) {
            throw new IndexOutOfBoundsException("Indices (" + i + ", " + j + ") out of range for degree " + n);
        }
        int[] map = new int[n];
        for (int k = 0; k < n; k++) map[k] = k;
        map[i] = j;
        map[j] = i;
        return new Permutation(map);
    }

    public int degree() {
        return mapping.length;
    }

    public int apply(int i) {
        return mapping[i];
    }

    @Override
    public Permutation combine(Permutation other) {
        Objects.requireNonNull(other, "other cannot be null");
        if (this.degree() != other.degree()) {
            throw new IllegalArgumentException("Cannot compose permutations of different degrees: " + degree() + " vs " + other.degree());
        }
        int n = degree();
        int[] res = new int[n];
        // (this * other)(i) = this(other(i))
        for (int i = 0; i < n; i++) {
            res[i] = this.mapping[other.mapping[i]];
        }
        return new Permutation(res);
    }

    @Override
    public Permutation inverse() {
        int n = degree();
        int[] inv = new int[n];
        for (int i = 0; i < n; i++) {
            inv[this.mapping[i]] = i;
        }
        return new Permutation(inv);
    }

    /**
     * Returns the sign/parity of the permutation (+1 for even, -1 for odd).
     */
    public int sign() {
        int n = degree();
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
        // Parity formula: (-1)^(n - number of cycles)
        return ((n - numCycles) % 2 == 0) ? 1 : -1;
    }

    public boolean isEven() {
        return sign() == 1;
    }

    public boolean isOdd() {
        return sign() == -1;
    }

    /**
     * Computes the disjoint cycle decomposition of this permutation.
     */
    public List<List<Integer>> cycles() {
        int n = degree();
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
                    cycleList.add(cycle);
                }
            }
        }
        return cycleList;
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
        if (c.isEmpty()) return "(id)";
        StringBuilder sb = new StringBuilder();
        for (List<Integer> cycle : c) {
            sb.append("(");
            for (int i = 0; i < cycle.size(); i++) {
                if (i > 0) sb.append(" ");
                sb.append(cycle.get(i));
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
