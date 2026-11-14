package lock14.algebra.group;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.FiniteGroup;

/**
 * The Dihedral Group {@code D_n} of symmetries of a regular {@code n}-gon, of order {@code 2n} ({@code n >= 1}).
 * Presentation: {@code <r, s | r^n = s^2 = e, s r s = r^-1>}, with canonical elements {@code r^k s^eps}.
 */
public final class DihedralGroup implements FiniteGroup<DihedralGroup.Element> {

    /**
     * Canonical element {@code r^rotation * s^eps} of a {@link DihedralGroup}, where {@code eps = 1}
     * if {@code reflected} is {@code true} and {@code eps = 0} otherwise.
     *
     * @param rotation the non-negative rotation exponent {@code k}
     * @param reflected {@code true} if composed with the reflection {@code s}, {@code false} for a pure rotation
     */
    public record Element(int rotation, boolean reflected) {
        /**
         * Validates that the canonical rotation exponent is non-negative.
         *
         * @param rotation the non-negative rotation exponent {@code k}
         * @param reflected {@code true} if composed with the reflection {@code s}, {@code false} otherwise
         * @throws IllegalArgumentException if {@code rotation < 0}
         */
        public Element {
            if (rotation < 0) {
                throw new IllegalArgumentException("Canonical rotation exponent must be non-negative: " + rotation);
            }
        }

        @Override
        public String toString() {
            if (rotation == 0 && !reflected) return "e";
            if (rotation == 0) return "s";
            String rot = rotation == 1 ? "r" : "r^" + rotation;
            return reflected ? rot + "s" : rot;
        }
    }

    private final int polygonSides;
    private final Set<Element> elements;

    /**
     * Constructs the dihedral group {@code D_n} of symmetries of a regular {@code n}-gon (having order {@code 2n}).
     *
     * @param polygonSides the number of polygon sides {@code n} ({@code polygonSides >= 1})
     * @throws IllegalArgumentException if {@code polygonSides <= 0}
     */
    public DihedralGroup(int polygonSides) {
        if (polygonSides <= 0) {
            throw new IllegalArgumentException("Polygon sides n must be positive: " + polygonSides);
        }
        this.polygonSides = polygonSides;
        Set<Element> elems = new LinkedHashSet<>(2 * polygonSides);
        for (int k = 0; k < polygonSides; k++) {
            elems.add(new Element(k, false));
        }
        for (int k = 0; k < polygonSides; k++) {
            elems.add(new Element(k, true));
        }
        this.elements = Set.copyOf(elems);
    }

    /**
     * Returns the parameter {@code n} (number of polygon sides) of this dihedral group {@code D_n}.
     *
     * @return the number of polygon sides {@code n}
     */
    public int polygonSides() {
        return polygonSides;
    }

    /**
     * Returns the canonical rotation generator {@code r} of order {@code n}.
     *
     * @return the rotation generator element {@code r}
     */
    public Element rotationGenerator() {
        return new Element(1 % polygonSides, false);
    }

    /**
     * Returns the canonical reflection generator {@code s} of order {@code 2}.
     *
     * @return the reflection generator element {@code s}
     */
    public Element reflectionGenerator() {
        return new Element(0, true);
    }

    @Override
    public Set<Element> elements() {
        return elements;
    }

    @Override
    public long order() {
        return 2L * polygonSides;
    }

    @Override
    public Element identity() {
        return new Element(0, false);
    }

    @Override
    public Element combine(Element left, Element right) {
        checkInGroup(left);
        checkInGroup(right);
        int rot = left.reflected()
            ? Math.floorMod(left.rotation() - right.rotation(), polygonSides)
            : Math.floorMod(left.rotation() + right.rotation(), polygonSides);
        boolean refl = left.reflected() ^ right.reflected();
        return new Element(rot, refl);
    }

    @Override
    public Element inverse(Element element) {
        checkInGroup(element);
        if (element.reflected()) {
            return element;
        }
        return new Element(Math.floorMod(-element.rotation(), polygonSides), false);
    }

    @Override
    public boolean contains(Element element) {
        Objects.requireNonNull(element, "element cannot be null");
        return element.rotation() >= 0 && element.rotation() < polygonSides;
    }

    private void checkInGroup(Element e) {
        Objects.requireNonNull(e, "element cannot be null");
        if (e.rotation() < 0 || e.rotation() >= polygonSides) {
            throw new IncompatibleAlgebraException(
                "Element " + e + " does not belong to D_" + polygonSides
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DihedralGroup other && this.polygonSides == other.polygonSides;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(polygonSides);
    }

    @Override
    public String toString() {
        return "D_" + polygonSides;
    }
}
