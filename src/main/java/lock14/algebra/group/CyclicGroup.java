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
import lock14.algebra.structure.AbelianGroup;
import lock14.algebra.structure.FiniteGroup;

/**
 * The Finite Cyclic Group {@code C_n} of order {@code n} ({@code n >= 1}), represented additively
 * on canonical residues {@code {0, 1, ..., n - 1}}.
 */
public final class CyclicGroup implements FiniteGroup<Integer>, AbelianGroup<Integer> {

    private final int modulus;
    private final Set<Integer> elementsView;

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
        this.elementsView = new ElementsSet();
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
        return elementsView;
    }

    @Override
    public Collection<Integer> generators() {
        return List.of(generator());
    }

    @Override
    public BigInteger order() {
        return BigInteger.valueOf(modulus);
    }

    @Override
    public BigInteger elementOrder(Integer element) {
        checkInGroup(element);
        if (element == 0) {
            return BigInteger.ONE;
        }
        return BigInteger.valueOf(modulus / gcd(modulus, element));
    }

    @Override
    public Integer identity() {
        return 0;
    }

    @Override
    public Integer combine(Integer left, Integer right) {
        checkInGroup(left);
        checkInGroup(right);
        return (int) (((long) left + right) % modulus);
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

    private void checkInGroup(Integer elem) {
        Objects.requireNonNull(elem, "element cannot be null");
        if (elem < 0 || elem >= modulus) {
            throw new IncompatibleAlgebraException(
                "Element " + elem + " is outside CyclicGroup(" + modulus + ")"
            );
        }
    }

    private static int gcd(int a, int b) {
        int x = a;
        int y = b;
        while (y != 0) {
            int t = x % y;
            x = y;
            y = t;
        }
        return x;
    }

    private final class ElementsSet extends AbstractSet<Integer> {
        @Override
        public int size() {
            return modulus;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return o instanceof Integer val && val >= 0 && val < modulus;
        }

        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<>() {
                private int nextVal = 0;

                @Override
                public boolean hasNext() {
                    return nextVal < modulus;
                }

                @Override
                public Integer next() {
                    if (nextVal >= modulus) {
                        throw new NoSuchElementException();
                    }
                    return nextVal++;
                }
            };
        }
    }
}
