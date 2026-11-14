package lock14.algebra.group;

import java.util.Objects;
import java.util.Set;
import lock14.algebra.structure.FiniteGroup;

/**
 * An immutable left or right coset ({@code gH} or {@code Hg}) of a {@link Subgroup} inside a {@link FiniteGroup}.
 * Equality and hashing are strictly mathematical (determined by the coset's subset of elements and parent group),
 * independent of which representative {@code g} was chosen.
 *
 * @param <T> the group element type
 */
public final class Coset<T> {

    private final FiniteGroup<T> parentGroup;
    private final T representative;
    private final Set<T> elements;

    Coset(FiniteGroup<T> parentGroup, T representative, Set<T> elements) {
        this.parentGroup = Objects.requireNonNull(parentGroup, "parentGroup cannot be null");
        this.representative = Objects.requireNonNull(representative, "representative cannot be null");
        this.elements = Set.copyOf(elements);
    }

    /**
     * Returns the ambient finite group {@code G} containing this coset.
     *
     * @return the parent finite group {@code G}
     */
    public FiniteGroup<T> parentGroup() {
        return parentGroup;
    }

    /**
     * Returns the representative element {@code g} used when constructing this coset.
     *
     * @return a representative element of this coset
     */
    public T representative() {
        return representative;
    }

    /**
     * Returns the unmodifiable set of elements belonging to this coset.
     *
     * @return the underlying element subset of {@code G}
     */
    public Set<T> elements() {
        return elements;
    }

    /**
     * Checks whether the specified element belongs to this coset.
     *
     * @param element the group element to test
     * @return {@code true} if {@code element} is a member of this coset, {@code false} otherwise
     */
    public boolean contains(T element) {
        Objects.requireNonNull(element, "element cannot be null");
        return elements.contains(element);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Coset<?> other
            && Objects.equals(parentGroup, other.parentGroup)
            && Objects.equals(elements, other.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentGroup, elements);
    }

    @Override
    public String toString() {
        return representative + "H" + elements;
    }
}
