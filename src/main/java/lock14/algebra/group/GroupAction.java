package lock14.algebra.group;

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

    X act(G groupElement, X point);

    /**
     * Computes the orbit {@code G . point = { g . point | g in G }}.
     */
    default Set<X> orbit(FiniteGroup<G> group, X point) {
        Objects.requireNonNull(group, "group cannot be null");
        Objects.requireNonNull(point, "point cannot be null");
        Set<X> orb = new LinkedHashSet<>();
        for (G g : group.elements()) {
            orb.add(act(g, point));
        }
        return Set.copyOf(orb);
    }

    /**
     * Computes the stabilizer subgroup {@code Stab_G(point) = { g in G | g . point = point }}.
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
     */
    default long countOrbitsBurnside(FiniteGroup<G> group, Set<X> domain) {
        Objects.requireNonNull(group, "group cannot be null");
        Objects.requireNonNull(domain, "domain cannot be null");
        long fixedSum = 0;
        for (G g : group.elements()) {
            fixedSum = Math.addExact(fixedSum, fixedPoints(g, domain).size());
        }
        long order = group.order();
        if (fixedSum % order != 0) {
            throw new IllegalStateException("Action violates group action axioms on domain (Burnside sum not divisible by |G|)");
        }
        return fixedSum / order;
    }

    static <G> GroupAction<G, G> leftTranslation(Group<G> group) {
        Objects.requireNonNull(group, "group cannot be null");
        return group::combine;
    }

    static <G> GroupAction<G, G> conjugation(Group<G> group) {
        Objects.requireNonNull(group, "group cannot be null");
        return group::conjugate;
    }

    static GroupAction<Permutation, Integer> permutationOnPoints() {
        return Permutation::apply;
    }
}
