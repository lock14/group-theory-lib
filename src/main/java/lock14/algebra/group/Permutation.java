package lock14.algebra.group;

import java.math.BigInteger;
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

    Permutation(int[] trustedMapping) {
        this.mapping = trustedMapping;
    }

    /**
     * Constructs a permutation from its zero-based image mapping {@code i -> mapping[i]}.
     *
     * @param mapping a bijection on {@code {0, 1, ..., n - 1}} where {@code n >= 1}
     * @return the permutation defined by {@code mapping}
     * @throws IllegalArgumentException if {@code mapping} is empty, out of range, or contains duplicates
     */
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

    /**
     * Returns the identity permutation on {@code {0, 1, ..., degree - 1}}.
     *
     * @param degree the number of elements permuted ({@code degree >= 1})
     * @return the identity permutation of the specified degree
     * @throws IllegalArgumentException if {@code degree <= 0}
     */
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

    /**
     * Returns the transposition swapping {@code i} and {@code j} on {@code {0, 1, ..., degree - 1}}
     * (or the identity permutation if {@code i == j}).
     *
     * @param degree the number of elements permuted ({@code degree >= 1})
     * @param i the first element index in {@code {0, ..., degree - 1}}
     * @param j the second element index in {@code {0, ..., degree - 1}}
     * @return the permutation exchanging {@code i} and {@code j}
     * @throws IllegalArgumentException if {@code degree <= 0}
     * @throws IndexOutOfBoundsException if {@code i} or {@code j} is outside {@code [0, degree)}
     */
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
     *
     * @param degree the number of elements permuted ({@code degree >= 1})
     * @param cycles the list of cycles to compose from left to right
     * @return the composed permutation
     * @throws IllegalArgumentException if {@code degree <= 0} or a single cycle contains duplicate entries
     * @throws IndexOutOfBoundsException if any cycle entry lies outside {@code [0, degree)}
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

    /**
     * Returns the degree {@code n} of this permutation on {@code {0, 1, ..., n - 1}}.
     *
     * @return the permutation degree {@code n}
     */
    public int degree() {
        return mapping.length;
    }

    /**
     * Evaluates this permutation at the element {@code i}.
     *
     * @param i the point in {@code {0, 1, ..., degree() - 1}} to map
     * @return the image of {@code i} under this permutation
     * @throws IndexOutOfBoundsException if {@code i} is outside {@code [0, degree())}
     */
    public int apply(int i) {
        if (i < 0 || i >= mapping.length) {
            throw new IndexOutOfBoundsException("Index " + i + " out of bounds for degree " + mapping.length);
        }
        return mapping[i];
    }

    /**
     * Returns a defensive copy of the underlying image array {@code [sigma(0), ..., sigma(n - 1)]}.
     *
     * @return a newly allocated array representing the one-line image mapping
     */
    public int[] toArray() {
        return mapping.clone();
    }

    /**
     * Returns an unmodifiable list of the images {@code [sigma(0), ..., sigma(n - 1)]}.
     *
     * @return an unmodifiable list representing the one-line image mapping
     */
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

    /**
     * Computes the parity sign ({@code +1} for even, {@code -1} for odd) of this permutation.
     *
     * @return {@code 1} if this permutation is even, or {@code -1} if it is odd
     */
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

    /**
     * Checks whether this permutation can be written as a product of an even number of transpositions.
     *
     * @return {@code true} if {@link #sign()} is {@code 1}, {@code false} otherwise
     */
    public boolean isEven() {
        return sign() == 1;
    }

    /**
     * Checks whether this permutation can be written as a product of an odd number of transpositions.
     *
     * @return {@code true} if {@link #sign()} is {@code -1}, {@code false} otherwise
     */
    public boolean isOdd() {
        return sign() == -1;
    }

    /**
     * Decomposes this permutation into its canonical disjoint non-trivial cycles (each of length at least 2).
     *
     * @return an unmodifiable list of unmodifiable disjoint cycles
     */
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
     *
     * @return the smallest positive integer {@code k} such that {@code this^k} is the identity
     */
    public BigInteger order() {
        BigInteger ord = BigInteger.ONE;
        for (List<Integer> cycle : cycles()) {
            BigInteger len = BigInteger.valueOf(cycle.size());
            ord = ord.divide(ord.gcd(len)).multiply(len);
        }
        return ord;
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
