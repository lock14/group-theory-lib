package lock14.algebra.structure;

import java.math.BigInteger;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Universal Direct Product constructions ({@code A x B}) lifting algebraic structures component-wise onto {@link Pair}.
 */
public final class DirectProduct {

    private DirectProduct() {}

    /**
     * Constructs the direct product {@link Monoid} {@code left x right} with component-wise combination and identity.
     *
     * @param <A>   left carrier type
     * @param <B>   right carrier type
     * @param left  the left factor monoid
     * @param right the right factor monoid
     * @return the direct product monoid over {@code Pair<A, B>}
     */
    public static <A, B> Monoid<Pair<A, B>> monoid(Monoid<A> left, Monoid<B> right) {
        Objects.requireNonNull(left, "left monoid cannot be null");
        Objects.requireNonNull(right, "right monoid cannot be null");
        record ProductMonoid<L, R>(Monoid<L> m1, Monoid<R> m2) implements Monoid<Pair<L, R>> {
            @Override
            public Pair<L, R> identity() {
                return new Pair<>(m1.identity(), m2.identity());
            }

            @Override
            public Pair<L, R> combine(Pair<L, R> a, Pair<L, R> b) {
                return new Pair<>(m1.combine(a.first(), b.first()), m2.combine(a.second(), b.second()));
            }
        }
        return new ProductMonoid<>(left, right);
    }

    /**
     * Constructs the direct product {@link Group} {@code left x right} with component-wise group operations.
     *
     * @param <A>   left carrier type
     * @param <B>   right carrier type
     * @param left  the left factor group
     * @param right the right factor group
     * @return the direct product group over {@code Pair<A, B>}
     */
    public static <A, B> Group<Pair<A, B>> group(Group<A> left, Group<B> right) {
        Objects.requireNonNull(left, "left group cannot be null");
        Objects.requireNonNull(right, "right group cannot be null");
        record ProductGroup<L, R>(Group<L> g1, Group<R> g2) implements Group<Pair<L, R>> {
            @Override
            public Pair<L, R> identity() {
                return new Pair<>(g1.identity(), g2.identity());
            }

            @Override
            public Pair<L, R> combine(Pair<L, R> a, Pair<L, R> b) {
                return new Pair<>(g1.combine(a.first(), b.first()), g2.combine(a.second(), b.second()));
            }

            @Override
            public Pair<L, R> inverse(Pair<L, R> element) {
                return new Pair<>(g1.inverse(element.first()), g2.inverse(element.second()));
            }
        }
        return new ProductGroup<>(left, right);
    }

    /**
     * Constructs the direct product {@link AbelianGroup} {@code left x right} of two abelian groups.
     *
     * @param <A>   left carrier type
     * @param <B>   right carrier type
     * @param left  the left factor abelian group
     * @param right the right factor abelian group
     * @return the direct product abelian group over {@code Pair<A, B>}
     */
    public static <A, B> AbelianGroup<Pair<A, B>> abelianGroup(AbelianGroup<A> left, AbelianGroup<B> right) {
        Objects.requireNonNull(left, "left abelian group cannot be null");
        Objects.requireNonNull(right, "right abelian group cannot be null");
        record ProductAbelianGroup<L, R>(AbelianGroup<L> g1, AbelianGroup<R> g2) implements AbelianGroup<Pair<L, R>> {
            @Override
            public Pair<L, R> identity() {
                return new Pair<>(g1.identity(), g2.identity());
            }

            @Override
            public Pair<L, R> combine(Pair<L, R> a, Pair<L, R> b) {
                return new Pair<>(g1.combine(a.first(), b.first()), g2.combine(a.second(), b.second()));
            }

            @Override
            public Pair<L, R> inverse(Pair<L, R> element) {
                return new Pair<>(g1.inverse(element.first()), g2.inverse(element.second()));
            }
        }
        return new ProductAbelianGroup<>(left, right);
    }

    /**
     * Constructs the direct product {@link FiniteGroup} {@code left x right} whose carrier set is the Cartesian product.
     *
     * @param <A>   left carrier type
     * @param <B>   right carrier type
     * @param left  the left factor finite group
     * @param right the right factor finite group
     * @return the direct product finite group over {@code Pair<A, B>} of order {@code left.order() * right.order()}
     */
    public static <A, B> FiniteGroup<Pair<A, B>> finiteGroup(FiniteGroup<A> left, FiniteGroup<B> right) {
        Objects.requireNonNull(left, "left finite group cannot be null");
        Objects.requireNonNull(right, "right finite group cannot be null");
        Set<Pair<A, B>> lazyElements = new AbstractSet<>() {
            private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);

            @Override
            public int size() {
                return left.order().multiply(right.order()).min(MAX_INT).intValue();
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return o instanceof Pair<?, ?> p
                    && left.elements().contains(p.first())
                    && right.elements().contains(p.second());
            }

            @Override
            public Iterator<Pair<A, B>> iterator() {
                return new Iterator<>() {
                    private final Iterator<A> outer = left.elements().iterator();
                    private Iterator<B> inner = Collections.emptyIterator();
                    private A currentLeft;

                    @Override
                    public boolean hasNext() {
                        while (!inner.hasNext() && outer.hasNext()) {
                            currentLeft = outer.next();
                            inner = right.elements().iterator();
                        }
                        return inner.hasNext();
                    }

                    @Override
                    public Pair<A, B> next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        return new Pair<>(currentLeft, inner.next());
                    }
                };
            }
        };
        record ProductFiniteGroup<L, R>(FiniteGroup<L> g1, FiniteGroup<R> g2, Set<Pair<L, R>> elements)
            implements FiniteGroup<Pair<L, R>> {
            @Override
            public Collection<Pair<L, R>> generators() {
                List<Pair<L, R>> gens = new ArrayList<>();
                R id2 = g2.identity();
                for (L x : g1.generators()) {
                    gens.add(new Pair<>(x, id2));
                }
                L id1 = g1.identity();
                for (R y : g2.generators()) {
                    gens.add(new Pair<>(id1, y));
                }
                return List.copyOf(gens);
            }

            @Override
            public BigInteger order() {
                return g1.order().multiply(g2.order());
            }

            @Override
            public boolean contains(Pair<L, R> element) {
                Objects.requireNonNull(element, "element cannot be null");
                return g1.contains(element.first()) && g2.contains(element.second());
            }

            @Override
            public BigInteger elementOrder(Pair<L, R> element) {
                Objects.requireNonNull(element, "element cannot be null");
                BigInteger o1 = g1.elementOrder(element.first());
                BigInteger o2 = g2.elementOrder(element.second());
                return o1.divide(o1.gcd(o2)).multiply(o2);
            }

            @Override
            public boolean isAbelian() {
                return g1.isAbelian() && g2.isAbelian();
            }

            @Override
            public Pair<L, R> identity() {
                return new Pair<>(g1.identity(), g2.identity());
            }

            @Override
            public Pair<L, R> combine(Pair<L, R> a, Pair<L, R> b) {
                return new Pair<>(g1.combine(a.first(), b.first()), g2.combine(a.second(), b.second()));
            }

            @Override
            public Pair<L, R> inverse(Pair<L, R> element) {
                return new Pair<>(g1.inverse(element.first()), g2.inverse(element.second()));
            }
        }
        return new ProductFiniteGroup<>(left, right, lazyElements);
    }

    /**
     * Constructs the direct product {@link Ring} {@code left x right} with component-wise ring arithmetic.
     *
     * @param <A>   left carrier type
     * @param <B>   right carrier type
     * @param left  the left factor ring
     * @param right the right factor ring
     * @return the direct product ring over {@code Pair<A, B>}
     */
    public static <A, B> Ring<Pair<A, B>> ring(Ring<A> left, Ring<B> right) {
        Objects.requireNonNull(left, "left ring cannot be null");
        Objects.requireNonNull(right, "right ring cannot be null");
        record ProductRing<L, R>(Ring<L> r1, Ring<R> r2) implements Ring<Pair<L, R>> {
            @Override
            public Pair<L, R> zero() {
                return new Pair<>(r1.zero(), r2.zero());
            }

            @Override
            public Pair<L, R> one() {
                return new Pair<>(r1.one(), r2.one());
            }

            @Override
            public Pair<L, R> add(Pair<L, R> a, Pair<L, R> b) {
                return new Pair<>(r1.add(a.first(), b.first()), r2.add(a.second(), b.second()));
            }

            @Override
            public Pair<L, R> negate(Pair<L, R> a) {
                return new Pair<>(r1.negate(a.first()), r2.negate(a.second()));
            }

            @Override
            public Pair<L, R> multiply(Pair<L, R> a, Pair<L, R> b) {
                return new Pair<>(r1.multiply(a.first(), b.first()), r2.multiply(a.second(), b.second()));
            }

            @Override
            public boolean isZero(Pair<L, R> element) {
                return r1.isZero(element.first()) && r2.isZero(element.second());
            }

            @Override
            public boolean isOne(Pair<L, R> element) {
                return r1.isOne(element.first()) && r2.isOne(element.second());
            }
        }
        return new ProductRing<>(left, right);
    }

    /**
     * Constructs the direct product {@link CommutativeRing} {@code left x right} of two commutative rings.
     *
     * @param <A>   left carrier type
     * @param <B>   right carrier type
     * @param left  the left factor commutative ring
     * @param right the right factor commutative ring
     * @return the direct product commutative ring over {@code Pair<A, B>}
     */
    public static <A, B> CommutativeRing<Pair<A, B>> commutativeRing(CommutativeRing<A> left, CommutativeRing<B> right) {
        Objects.requireNonNull(left, "left commutative ring cannot be null");
        Objects.requireNonNull(right, "right commutative ring cannot be null");
        record ProductCommutativeRing<L, R>(CommutativeRing<L> r1, CommutativeRing<R> r2)
            implements CommutativeRing<Pair<L, R>> {
            @Override
            public Pair<L, R> zero() {
                return new Pair<>(r1.zero(), r2.zero());
            }

            @Override
            public Pair<L, R> one() {
                return new Pair<>(r1.one(), r2.one());
            }

            @Override
            public Pair<L, R> add(Pair<L, R> a, Pair<L, R> b) {
                return new Pair<>(r1.add(a.first(), b.first()), r2.add(a.second(), b.second()));
            }

            @Override
            public Pair<L, R> negate(Pair<L, R> a) {
                return new Pair<>(r1.negate(a.first()), r2.negate(a.second()));
            }

            @Override
            public Pair<L, R> multiply(Pair<L, R> a, Pair<L, R> b) {
                return new Pair<>(r1.multiply(a.first(), b.first()), r2.multiply(a.second(), b.second()));
            }

            @Override
            public boolean isZero(Pair<L, R> element) {
                return r1.isZero(element.first()) && r2.isZero(element.second());
            }

            @Override
            public boolean isOne(Pair<L, R> element) {
                return r1.isOne(element.first()) && r2.isOne(element.second());
            }
        }
        return new ProductCommutativeRing<>(left, right);
    }
}
