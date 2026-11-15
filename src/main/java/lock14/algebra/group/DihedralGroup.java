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
            if (rotation == 0 && !reflected) {
                return "e";
            }
            if (rotation == 0) {
                return "s";
            }
            String rot = rotation == 1 ? "r" : "r^" + rotation;
            return reflected ? rot + "s" : rot;
        }
    }

    private final int polygonSides;
    private final Set<Element> elementsView;

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
        this.elementsView = new ElementsSet();
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
        return elementsView;
    }

    @Override
    public Collection<Element> generators() {
        if (polygonSides == 1) {
            return List.of(reflectionGenerator());
        }
        return List.of(rotationGenerator(), reflectionGenerator());
    }

    @Override
    public BigInteger order() {
        return BigInteger.valueOf(2L * polygonSides);
    }

    @Override
    public boolean isAbelian() {
        return polygonSides <= 2;
    }

    @Override
    public Element identity() {
        return new Element(0, false);
    }

    @Override
    public Element combine(Element left, Element right) {
        checkInGroup(left);
        checkInGroup(right);
        long diff = left.reflected()
            ? (long) left.rotation() - right.rotation()
            : (long) left.rotation() + right.rotation();
        int rot = Math.floorMod(diff, polygonSides);
        boolean refl = left.reflected() ^ right.reflected();
        return new Element(rot, refl);
    }

    @Override
    public Element inverse(Element element) {
        checkInGroup(element);
        if (element.reflected()) {
            return element;
        }
        return new Element(Math.floorMod(-(long) element.rotation(), polygonSides), false);
    }

    @Override
    public boolean contains(Element element) {
        Objects.requireNonNull(element, "element cannot be null");
        return element.rotation() >= 0 && element.rotation() < polygonSides;
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

    private void checkInGroup(Element e) {
        Objects.requireNonNull(e, "element cannot be null");
        if (e.rotation() < 0 || e.rotation() >= polygonSides) {
            throw new IncompatibleAlgebraException(
                "Element " + e + " does not belong to D_" + polygonSides
            );
        }
    }

    private final class ElementsSet extends AbstractSet<Element> {
        @Override
        public int size() {
            return (int) Math.min(2L * polygonSides, Integer.MAX_VALUE);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return o instanceof Element e && DihedralGroup.this.contains(e);
        }

        @Override
        public Iterator<Element> iterator() {
            return new Iterator<>() {
                private long cursor = 0;
                private final long total = 2L * polygonSides;

                @Override
                public boolean hasNext() {
                    return cursor < total;
                }

                @Override
                public Element next() {
                    if (cursor >= total) {
                        throw new NoSuchElementException();
                    }
                    long idx = cursor++;
                    if (idx < polygonSides) {
                        return new Element((int) idx, false);
                    }
                    return new Element((int) (idx - polygonSides), true);
                }
            };
        }
    }
}
