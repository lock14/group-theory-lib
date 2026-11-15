package lock14.algebra.group;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lock14.algebra.structure.FiniteGroup;
import lock14.algebra.structure.Group;

/**
 * A Left Group Action {@code G x X -> X} satisfying {@code e . x = x} and {@code (g * h) . x = g . (h . x)}.
 *
 * @param <G> the acting group element type
 * @param <X> the target set element type
 */
@FunctionalInterface
public interface GroupAction<G, X> {

    /**
     * Applies the left action of {@code groupElement} to {@code point}.
     *
     * @param groupElement the acting group element {@code g}
     * @param point the target set element {@code x}
     * @return the transformed point {@code g . x}
     */
    X act(G groupElement, X point);

    /**
     * Computes the orbit {@code G . point = { g . point | g in G }}.
     *
     * @param group the finite group acting on the set
     * @param point the base point whose orbit is computed
     * @return the orbit of {@code point} under {@code group}
     */
    default Set<X> orbit(FiniteGroup<G> group, X point) {
        Objects.requireNonNull(group, "group cannot be null");
        Objects.requireNonNull(point, "point cannot be null");
        Collection<G> gens = group.generators();
        X start = act(group.identity(), point);
        Set<X> orb = new LinkedHashSet<>();
        orb.add(start);
        ArrayDeque<X> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            X current = queue.removeFirst();
            for (G gen : gens) {
                X next = act(gen, current);
                if (orb.add(next)) {
                    queue.addLast(next);
                }
            }
        }
        return Set.copyOf(orb);
    }

    /**
     * Computes the stabilizer subgroup {@code Stab_G(point) = { g in G | g . point = point }}.
     *
     * @param group the finite group acting on the set
     * @param point the point whose stabilizer is computed
     * @return the stabilizer subgroup of {@code point} in {@code group}
     */
    default Subgroup<G> stabilizer(FiniteGroup<G> group, X point) {
        Objects.requireNonNull(group, "group cannot be null");
        Objects.requireNonNull(point, "point cannot be null");
        Set<G> stab = new LinkedHashSet<>();
        for (G g : group.elements()) {
            if (Objects.equals(act(g, point), point)) {
                stab.add(g);
            }
        }
        return Subgroup.of(group, stab);
    }

    /**
     * Returns the fixed-point subset {@code X^g = { x in domain | g . x = x }}.
     *
     * @param g the group element whose fixed points are sought
     * @param domain the finite domain set {@code X}
     * @return the subset of elements in {@code domain} fixed by {@code g}
     */
    default Set<X> fixedPoints(G g, Set<X> domain) {
        Objects.requireNonNull(g, "group element cannot be null");
        Objects.requireNonNull(domain, "domain cannot be null");
        Set<X> fixed = new LinkedHashSet<>();
        for (X x : domain) {
            if (Objects.equals(act(g, x), x)) {
                fixed.add(x);
            }
        }
        return Set.copyOf(fixed);
    }

    /**
     * Partitions {@code domain} into disjoint orbits under {@code group}.
     *
     * @param group the finite acting group
     * @param domain the finite G-invariant domain set {@code X}
     * @return the set of disjoint orbits partitioning {@code domain}
     */
    default Set<Set<X>> orbits(FiniteGroup<G> group, Set<X> domain) {
        Objects.requireNonNull(group, "group cannot be null");
        Objects.requireNonNull(domain, "domain cannot be null");
        Set<X> visited = new LinkedHashSet<>();
        Set<Set<X>> partition = new LinkedHashSet<>();
        for (X x : domain) {
            if (!visited.contains(x)) {
                Set<X> orb = orbit(group, x);
                visited.addAll(orb);
                partition.add(orb);
            }
        }
        return Set.copyOf(partition);
    }

    /**
     * Counts distinct orbits on {@code domain} via Burnside's Lemma: {@code (1 / |G|) * sum_{g in G} |X^g|}.
     *
     * @param group the finite acting group
     * @param domain the finite G-invariant domain set {@code X}
     * @return the exact number of distinct orbits of {@code group} on {@code domain}
     * @throws IllegalStateException if the fixed-point sum is not divisible by {@code |G|}
     */
    default BigInteger countOrbitsBurnside(FiniteGroup<G> group, Set<X> domain) {
        Objects.requireNonNull(group, "group cannot be null");
        Objects.requireNonNull(domain, "domain cannot be null");
        BigInteger fixedSum = BigInteger.ZERO;
        for (G g : group.elements()) {
            fixedSum = fixedSum.add(BigInteger.valueOf(fixedPoints(g, domain).size()));
        }
        BigInteger order = group.order();
        BigInteger[] divRem = fixedSum.divideAndRemainder(order);
        if (divRem[1].signum() != 0) {
            throw new IllegalStateException("Action violates group action axioms on domain (Burnside sum not divisible by |G|)");
        }
        return divRem[0];
    }

    /**
     * Returns the left regular action (Cayley left translation) {@code (g, x) -> g * x} of {@code group} on itself.
     *
     * @param <G> the group element type
     * @param group the acting group
     * @return the left-translation action on {@code group}
     */
    static <G> GroupAction<G, G> leftTranslation(Group<G> group) {
        Objects.requireNonNull(group, "group cannot be null");
        return group::combine;
    }

    /**
     * Returns the conjugation action {@code (g, h) -> g * h * g^-1} of {@code group} on itself.
     *
     * @param <G> the group element type
     * @param group the acting group
     * @return the conjugation action on {@code group}
     */
    static <G> GroupAction<G, G> conjugation(Group<G> group) {
        Objects.requireNonNull(group, "group cannot be null");
        return group::conjugate;
    }

    /**
     * Returns the natural action {@code (sigma, i) -> sigma(i)} of {@link Permutation}s on integer points.
     *
     * @return the natural action of permutations on {@code {0, 1, ..., n - 1}}
     */
    static GroupAction<Permutation, Integer> permutationOnPoints() {
        return Permutation::apply;
    }
}
