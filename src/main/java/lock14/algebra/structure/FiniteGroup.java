package lock14.algebra.structure;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * A Finite Group is a {@link Group} with a finite, enumerable carrier set.
 *
 * @param <T> the carrier type of elements
 */
public interface FiniteGroup<T> extends Group<T> {

    /**
     * Returns an unmodifiable view of all elements in this finite group.
     *
     * @return an unmodifiable set containing all elements of this group
     */
    Set<T> elements();

    /**
     * Returns the cardinality (order) {@code |G|} of this finite group.
     *
     * @return the number of elements in this group
     */
    default long order() {
        return elements().size();
    }

    /**
     * Checks whether {@code element} belongs to this finite group.
     *
     * @param element the candidate element to check
     * @return {@code true} if {@code element} belongs to this group, {@code false} otherwise
     */
    default boolean contains(T element) {
        Objects.requireNonNull(element, "element cannot be null");
        return elements().contains(element);
    }

    /**
     * Computes the multiplicative order of {@code element}: the smallest positive integer {@code k}
     * such that {@code power(element, k)} equals {@link #identity()}.
     *
     * @param element the group element whose order is computed
     * @return the smallest positive integer {@code k} such that {@code power(element, k)} equals {@link #identity()}
     * @throws IllegalArgumentException if {@code element} does not belong to this group
     */
    default long elementOrder(T element) {
        Objects.requireNonNull(element, "element cannot be null");
        if (!contains(element)) {
            throw new IllegalArgumentException("Element does not belong to this group: " + element);
        }
        T id = identity();
        T current = element;
        long k = 1;
        long max = order();
        while (!Objects.equals(current, id)) {
            current = combine(current, element);
            k++;
            if (k > max) {
                throw new IllegalStateException("Group closure violated while computing element order");
            }
        }
        return k;
    }

    /**
     * Checks whether this finite group is Abelian ({@code ab == ba} for all {@code a, b}).
     *
     * @return {@code true} if the group operation is commutative for all element pairs, {@code false} otherwise
     */
    default boolean isAbelian() {
        Set<T> elems = elements();
        for (T a : elems) {
            for (T b : elems) {
                if (!Objects.equals(combine(a, b), combine(b, a))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Generates the subgroup carrier {@code <S>} closed under group multiplication and inversion.
     *
     * @param generators the generating elements (all must belong to this group)
     * @return an unmodifiable set containing the generated subgroup carrier {@code <S>}
     * @throws IllegalArgumentException if any generator does not belong to this group
     */
    default Set<T> generateClosure(Collection<T> generators) {
        Objects.requireNonNull(generators, "generators cannot be null");
        Set<T> closure = new LinkedHashSet<>();
        Queue<T> queue = new ArrayDeque<>();
        T id = identity();
        closure.add(id);
        queue.add(id);

        for (T gen : generators) {
            Objects.requireNonNull(gen, "generator cannot be null");
            if (!contains(gen)) {
                throw new IllegalArgumentException("Generator does not belong to this group: " + gen);
            }
            if (closure.add(gen)) {
                queue.add(gen);
            }
            T inv = inverse(gen);
            if (closure.add(inv)) {
                queue.add(inv);
            }
        }

        Set<T> gensWithInverses = Set.copyOf(closure);
        while (!queue.isEmpty()) {
            T curr = queue.poll();
            for (T g : gensWithInverses) {
                T rightProd = combine(curr, g);
                if (closure.add(rightProd)) {
                    queue.add(rightProd);
                }
                T leftProd = combine(g, curr);
                if (closure.add(leftProd)) {
                    queue.add(leftProd);
                }
            }
        }
        return Set.copyOf(closure);
    }
}
