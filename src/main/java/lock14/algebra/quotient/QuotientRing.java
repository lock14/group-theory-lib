package lock14.algebra.quotient;

import java.util.Objects;
import java.util.Optional;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.CommutativeRing;
import lock14.algebra.structure.EuclideanDomain;
import lock14.algebra.structure.RingHomomorphism;

/**
 * Universal Quotient Ring {@code E / (m)} modulo a non-zero principal ideal {@code (m)} in any
 * {@link EuclideanDomain} {@code E} with unique canonical remainders (such as {@code Z/nZ} or {@code F[x]/(f(x))}).
 *
 * @param <T> underlying Euclidean domain element type
 */
public sealed class QuotientRing<T> implements CommutativeRing<Residue<T>> permits QuotientField {

    private final EuclideanDomain<T> domain;
    private final T modulus;

    public QuotientRing(EuclideanDomain<T> domain, T modulus) {
        this.domain = Objects.requireNonNull(domain, "domain cannot be null");
        Objects.requireNonNull(modulus, "modulus cannot be null");
        if (domain.isZero(modulus)) {
            throw new IllegalArgumentException("Quotient modulus cannot be zero");
        }
        this.modulus = domain.normalize(modulus);
    }

    public final EuclideanDomain<T> domain() {
        return domain;
    }

    public final T modulus() {
        return modulus;
    }

    /**
     * Projects {@code element} into its canonical residue class {@code [element mod m]}.
     */
    public final Residue<T> of(T element) {
        Objects.requireNonNull(element, "element cannot be null");
        T canonical = domain.remainder(element, modulus);
        return new Residue<>(canonical, this);
    }

    /**
     * Canonical quotient ring projection {@code pi: E -> E / (m)}.
     */
    public final RingHomomorphism<T, Residue<T>> projection() {
        return this::of;
    }

    public final Optional<Residue<T>> tryReciprocal(Residue<T> element) {
        checkInQuotient(element);
        if (domain.isOne(modulus)) {
            return Optional.of(element);
        }
        if (element.isZero()) {
            return Optional.empty();
        }
        EuclideanDomain.ExtendedGcdResult<T> bezout = domain.extendedGcd(element.representative(), modulus);
        if (!domain.isOne(bezout.gcd())) {
            return Optional.empty();
        }
        return Optional.of(of(bezout.x()));
    }

    @Override
    public final Residue<T> zero() {
        return of(domain.zero());
    }

    @Override
    public final Residue<T> one() {
        return of(domain.one());
    }

    @Override
    public final Residue<T> add(Residue<T> a, Residue<T> b) {
        checkInQuotient(a);
        checkInQuotient(b);
        return a.add(b);
    }

    @Override
    public final Residue<T> negate(Residue<T> a) {
        checkInQuotient(a);
        return a.negate();
    }

    @Override
    public final Residue<T> subtract(Residue<T> a, Residue<T> b) {
        checkInQuotient(a);
        checkInQuotient(b);
        return a.subtract(b);
    }

    @Override
    public final Residue<T> multiply(Residue<T> a, Residue<T> b) {
        checkInQuotient(a);
        checkInQuotient(b);
        return a.multiply(b);
    }

    @Override
    public final boolean isZero(Residue<T> element) {
        checkInQuotient(element);
        return element.isZero();
    }

    @Override
    public final boolean isOne(Residue<T> element) {
        checkInQuotient(element);
        return element.isOne();
    }

    protected final void checkInQuotient(Residue<T> r) {
        Objects.requireNonNull(r, "residue cannot be null");
        if (!Objects.equals(r.parent(), this)) {
            throw new IncompatibleAlgebraException(
                "Residue parent " + r.parent() + " does not match " + this
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QuotientRing<?> other
            && Objects.equals(domain, other.domain)
            && Objects.equals(modulus, other.modulus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, modulus);
    }

    @Override
    public String toString() {
        return domain + "/(" + modulus + ")";
    }
}
