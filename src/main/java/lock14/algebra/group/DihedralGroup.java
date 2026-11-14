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

    public record Element(int rotation, boolean reflected) {
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

    public int polygonSides() {
        return polygonSides;
    }

    public Element rotationGenerator() {
        return new Element(1 % polygonSides, false);
    }

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
