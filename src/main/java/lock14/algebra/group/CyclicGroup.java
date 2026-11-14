package lock14.algebra.group;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.AbelianGroup;
import lock14.algebra.structure.FiniteGroup;

/**
 * The Finite Cyclic Group {@code C_n} of order {@code n} ({@code n >= 1}), represented additively
 * on canonical residues {@code {0, 1, ..., n - 1}}.
 */
public final class CyclicGroup implements FiniteGroup<Integer>, AbelianGroup<Integer> {

    private final int modulus;
    private final Set<Integer> elements;

    /**
     * Constructs the finite cyclic group {@code C_n} of the specified order.
     *
     * @param order the group order {@code n} ({@code order >= 1})
     * @throws IllegalArgumentException if {@code order <= 0}
     */
    public CyclicGroup(int order) {
        if (order <= 0) {
            throw new IllegalArgumentException("Cyclic group order must be positive: " + order);
        }
        this.modulus = order;
        Set<Integer> elems = new LinkedHashSet<>(order);
        for (int i = 0; i < order; i++) {
            elems.add(i);
        }
        this.elements = Set.copyOf(elems);
    }

    /**
     * Returns the canonical additive generator of this cyclic group ({@code 0} when {@code order == 1},
     * or {@code 1} when {@code order > 1}).
     *
     * @return the canonical generator residue in {@code {0, ..., n - 1}}
     */
    public int generator() {
        return modulus == 1 ? 0 : 1;
    }

    @Override
    public Set<Integer> elements() {
        return elements;
    }

    @Override
    public long order() {
        return modulus;
    }

    @Override
    public Integer identity() {
        return 0;
    }

    @Override
    public Integer combine(Integer left, Integer right) {
        checkInGroup(left);
        checkInGroup(right);
        return (left + right) % modulus;
    }

    @Override
    public Integer inverse(Integer element) {
        checkInGroup(element);
        return element == 0 ? 0 : modulus - element;
    }

    @Override
    public boolean contains(Integer element) {
        Objects.requireNonNull(element, "element cannot be null");
        return element >= 0 && element < modulus;
    }

    @Override
    public boolean isAbelian() {
        return true;
    }

    private void checkInGroup(Integer elem) {
        Objects.requireNonNull(elem, "element cannot be null");
        if (elem < 0 || elem >= modulus) {
            throw new IncompatibleAlgebraException(
                "Element " + elem + " is outside CyclicGroup(" + modulus + ")"
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CyclicGroup other && this.modulus == other.modulus;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(modulus);
    }

    @Override
    public String toString() {
        return "C_" + modulus;
    }
}
