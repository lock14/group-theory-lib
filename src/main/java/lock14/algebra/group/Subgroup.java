package lock14.algebra.group;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.FiniteGroup;
import lock14.algebra.structure.Group;
import lock14.algebra.structure.GroupHomomorphism;

/**
 * A validated Subgroup {@code H <= G} of a {@link FiniteGroup} {@code G}.
 *
 * @param <T> the group element type
 */
public final class Subgroup<T> implements FiniteGroup<T> {

    private final FiniteGroup<T> parentGroup;
    private final Set<T> carrier;

    private Subgroup(FiniteGroup<T> parentGroup, Set<T> trustedCarrier) {
        this.parentGroup = parentGroup;
        this.carrier = trustedCarrier;
    }

    /**
     * Validates that {@code carrier} is non-empty, contained in {@code parentGroup}, and closed
     * under multiplication and inversion.
     *
     * @param <T> the group element type
     * @param parentGroup the enclosing finite group {@code G}
     * @param carrier the candidate subset of {@code G}
     * @return the validated subgroup {@code H <= G}
     * @throws IllegalArgumentException if {@code carrier} is empty, outside {@code parentGroup},
     *     or not closed under multiplication and inversion
     */
    public static <T> Subgroup<T> of(FiniteGroup<T> parentGroup, Set<T> carrier) {
        Objects.requireNonNull(parentGroup, "parentGroup cannot be null");
        Objects.requireNonNull(carrier, "carrier cannot be null");
        if (carrier.isEmpty()) {
            throw new IllegalArgumentException("Subgroup carrier must be non-empty");
        }
        Set<T> copy = Set.copyOf(carrier);
        for (T a : copy) {
            if (!parentGroup.contains(a)) {
                throw new IllegalArgumentException("Element " + a + " does not belong to parent group " + parentGroup);
            }
            if (!copy.contains(parentGroup.inverse(a))) {
                throw new IllegalArgumentException("Carrier is not closed under inverses for element: " + a);
            }
            for (T b : copy) {
                T prod = parentGroup.combine(a, b);
                if (!copy.contains(prod)) {
                    throw new IllegalArgumentException("Carrier is not closed under multiplication: " + a + " * " + b + " = " + prod);
                }
            }
        }
        return new Subgroup<>(parentGroup, copy);
    }

    /**
     * Generates the subgroup {@code <generators>} inside {@code parentGroup}.
     *
     * @param <T> the group element type
     * @param parentGroup the enclosing finite group {@code G}
     * @param generators the generating elements in {@code G}
     * @return the smallest subgroup of {@code parentGroup} containing {@code generators}
     */
    public static <T> Subgroup<T> generate(FiniteGroup<T> parentGroup, Collection<T> generators) {
        Objects.requireNonNull(parentGroup, "parentGroup cannot be null");
        return new Subgroup<>(parentGroup, parentGroup.generateClosure(generators));
    }

    /**
     * Constructs the normal kernel subgroup {@code ker(hom) <= domain}.
     *
     * @param <A> the domain group element type
     * @param <B> the codomain group element type
     * @param hom the group homomorphism {@code domain -> codomain}
     * @param domain the finite domain group
     * @param codomain the codomain group
     * @return the kernel subgroup {@code ker(hom)} in {@code domain}
     */
    public static <A, B> Subgroup<A> kernel(GroupHomomorphism<A, B> hom, FiniteGroup<A> domain, Group<B> codomain) {
        Objects.requireNonNull(hom, "hom cannot be null");
        return of(domain, hom.kernelElements(domain, codomain));
    }

    /**
     * Constructs the image subgroup {@code im(hom) <= codomain}.
     *
     * @param <A> the domain group element type
     * @param <B> the codomain group element type
     * @param hom the group homomorphism {@code domain -> codomain}
     * @param domain the finite domain group
     * @param codomain the finite codomain group
     * @return the image subgroup {@code im(hom)} in {@code codomain}
     */
    public static <A, B> Subgroup<B> image(GroupHomomorphism<A, B> hom, FiniteGroup<A> domain, FiniteGroup<B> codomain) {
        Objects.requireNonNull(hom, "hom cannot be null");
        return of(codomain, hom.imageElements(domain));
    }

    /**
     * Returns the ambient parent group {@code G} in which this subgroup {@code H <= G} is embedded.
     *
     * @return the parent finite group {@code G}
     */
    public FiniteGroup<T> parentGroup() {
        return parentGroup;
    }

    @Override
    public Set<T> elements() {
        return carrier;
    }

    /**
     * Returns the exact index {@code [G : H] = |G| / |H|} by Lagrange's theorem.
     *
     * @return the index of this subgroup in its parent group
     */
    public BigInteger index() {
        return parentGroup.order().divide(order());
    }

    /**
     * Checks whether {@code H} is a normal subgroup of {@code G} ({@code g h g^-1 in H} for all {@code g in G, h in H}).
     *
     * @return {@code true} if {@code H} is normal in {@code G}, {@code false} otherwise
     */
    public boolean isNormal() {
        for (T g : parentGroup.generators()) {
            T gInv = parentGroup.inverse(g);
            for (T h : carrier) {
                if (!carrier.contains(parentGroup.conjugate(g, h))
                        || !carrier.contains(parentGroup.conjugate(gInv, h))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Constructs the left coset {@code gH = { g * h | h in H }} of this subgroup in its parent group.
     *
     * @param g the coset representative in the parent group {@code G}
     * @return the left coset {@code gH}
     * @throws IncompatibleAlgebraException if {@code g} does not belong to {@link #parentGroup()}
     */
    public Coset<T> leftCoset(T g) {
        Objects.requireNonNull(g, "g cannot be null");
        if (!parentGroup.contains(g)) {
            throw new IncompatibleAlgebraException("Element " + g + " does not belong to parent group " + parentGroup);
        }
        Set<T> cosetElems = new LinkedHashSet<>(carrier.size());
        for (T h : carrier) {
            cosetElems.add(parentGroup.combine(g, h));
        }
        return new Coset<>(parentGroup, g, cosetElems);
    }

    /**
     * Constructs the right coset {@code Hg = { h * g | h in H }} of this subgroup in its parent group.
     *
     * @param g the coset representative in the parent group {@code G}
     * @return the right coset {@code Hg}
     * @throws IncompatibleAlgebraException if {@code g} does not belong to {@link #parentGroup()}
     */
    public Coset<T> rightCoset(T g) {
        Objects.requireNonNull(g, "g cannot be null");
        if (!parentGroup.contains(g)) {
            throw new IncompatibleAlgebraException("Element " + g + " does not belong to parent group " + parentGroup);
        }
        Set<T> cosetElems = new LinkedHashSet<>(carrier.size());
        for (T h : carrier) {
            cosetElems.add(parentGroup.combine(h, g));
        }
        return new Coset<>(parentGroup, g, cosetElems);
    }

    @Override
    public T identity() {
        return parentGroup.identity();
    }

    @Override
    public T combine(T left, T right) {
        checkInSubgroup(left);
        checkInSubgroup(right);
        return parentGroup.combine(left, right);
    }

    @Override
    public T inverse(T element) {
        checkInSubgroup(element);
        return parentGroup.inverse(element);
    }

    private void checkInSubgroup(T element) {
        Objects.requireNonNull(element, "element cannot be null");
        if (!carrier.contains(element)) {
            throw new IncompatibleAlgebraException("Element " + element + " does not belong to this subgroup");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Subgroup<?> other
            && Objects.equals(parentGroup, other.parentGroup)
            && Objects.equals(carrier, other.carrier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentGroup, carrier);
    }
}
