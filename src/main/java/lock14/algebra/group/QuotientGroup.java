package lock14.algebra.group;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.FiniteGroup;
import lock14.algebra.structure.GroupHomomorphism;

/**
 * The Quotient Group {@code G / N} of a {@link FiniteGroup} {@code G} modulo a normal {@link Subgroup} {@code N}.
 *
 * @param <T> the element type of the parent group {@code G}
 */
public final class QuotientGroup<T> implements FiniteGroup<Coset<T>> {

    private final FiniteGroup<T> parentGroup;
    private final Subgroup<T> normalSubgroup;
    private final Set<Coset<T>> cosets;
    private final List<Coset<T>> generators;

    private QuotientGroup(
            FiniteGroup<T> parentGroup,
            Subgroup<T> normalSubgroup,
            Set<Coset<T>> cosets,
            List<Coset<T>> generators) {
        this.parentGroup = parentGroup;
        this.normalSubgroup = normalSubgroup;
        this.cosets = cosets;
        this.generators = generators;
    }

    /**
     * Constructs the quotient group {@code G / N} of {@code parentGroup} modulo {@code normalSubgroup}.
     *
     * @param <T> the element type of the parent group {@code G}
     * @param parentGroup the finite group {@code G}
     * @param normalSubgroup a normal subgroup {@code N} of {@code parentGroup}
     * @return the quotient group {@code G / N}
     * @throws IncompatibleAlgebraException if {@code normalSubgroup} does not have {@code parentGroup} as its parent
     * @throws IllegalArgumentException if {@code normalSubgroup} is not normal in {@code parentGroup}
     */
    public static <T> QuotientGroup<T> of(FiniteGroup<T> parentGroup, Subgroup<T> normalSubgroup) {
        Objects.requireNonNull(parentGroup, "parentGroup cannot be null");
        Objects.requireNonNull(normalSubgroup, "normalSubgroup cannot be null");
        if (!Objects.equals(parentGroup, normalSubgroup.parentGroup())) {
            throw new IncompatibleAlgebraException("Subgroup parent does not match parentGroup");
        }
        if (!normalSubgroup.isNormal()) {
            throw new IllegalArgumentException("Subgroup is not normal in " + parentGroup + "; cannot form quotient group G/N");
        }
        Collection<T> parentGenerators = parentGroup.generators();
        Coset<T> idCoset = normalSubgroup.leftCoset(parentGroup.identity());
        Set<Coset<T>> allCosets = new LinkedHashSet<>();
        allCosets.add(idCoset);
        ArrayDeque<Coset<T>> queue = new ArrayDeque<>();
        queue.add(idCoset);
        while (!queue.isEmpty()) {
            Coset<T> current = queue.removeFirst();
            for (T gen : parentGenerators) {
                Coset<T> next = normalSubgroup.leftCoset(parentGroup.combine(current.representative(), gen));
                if (allCosets.add(next)) {
                    queue.addLast(next);
                }
            }
        }
        Set<Coset<T>> projectedGenerators = new LinkedHashSet<>();
        for (T gen : parentGenerators) {
            Coset<T> projected = normalSubgroup.leftCoset(gen);
            if (!projected.equals(idCoset)) {
                projectedGenerators.add(projected);
            }
        }
        return new QuotientGroup<>(
            parentGroup,
            normalSubgroup,
            Set.copyOf(allCosets),
            List.copyOf(projectedGenerators));
    }

    /**
     * Returns the ambient parent group {@code G}.
     *
     * @return the parent finite group {@code G}
     */
    public FiniteGroup<T> parentGroup() {
        return parentGroup;
    }

    /**
     * Returns the normal subgroup {@code N} by which {@code G} is factored.
     *
     * @return the normal subgroup {@code N}
     */
    public Subgroup<T> normalSubgroup() {
        return normalSubgroup;
    }

    /**
     * Returns the canonical surjective quotient homomorphism {@code pi: G -> G / N} mapping {@code g -> gN}.
     *
     * @return the canonical projection homomorphism onto {@code G / N}
     */
    public GroupHomomorphism<T, Coset<T>> canonicalProjection() {
        return normalSubgroup::leftCoset;
    }

    @Override
    public Set<Coset<T>> elements() {
        return cosets;
    }

    @Override
    public Collection<Coset<T>> generators() {
        return generators;
    }

    @Override
    public Coset<T> identity() {
        return normalSubgroup.leftCoset(parentGroup.identity());
    }

    @Override
    public Coset<T> combine(Coset<T> left, Coset<T> right) {
        checkInQuotient(left);
        checkInQuotient(right);
        return normalSubgroup.leftCoset(parentGroup.combine(left.representative(), right.representative()));
    }

    @Override
    public Coset<T> inverse(Coset<T> element) {
        checkInQuotient(element);
        return normalSubgroup.leftCoset(parentGroup.inverse(element.representative()));
    }

    private void checkInQuotient(Coset<T> coset) {
        Objects.requireNonNull(coset, "coset cannot be null");
        if (!cosets.contains(coset)) {
            throw new IncompatibleAlgebraException("Coset does not belong to quotient group G/N");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QuotientGroup<?> other
            && Objects.equals(parentGroup, other.parentGroup)
            && Objects.equals(normalSubgroup, other.normalSubgroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentGroup, normalSubgroup);
    }
}
