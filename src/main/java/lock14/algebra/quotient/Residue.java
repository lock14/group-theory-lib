package lock14.algebra.quotient;

import java.util.Objects;
import java.util.Optional;
import lock14.algebra.element.RingElement;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;

/**
 * A canonical residue class {@code [representative] mod modulus} inside a {@link QuotientRing} {@code E / (m)}.
 *
 * @param <T> underlying Euclidean domain element type
 * @param representative the canonical remainder representative in the underlying Euclidean domain
 * @param parent the enclosing quotient ring {@code E / (m)}
 */
public record Residue<T>(T representative, QuotientRing<T> parent) implements RingElement<Residue<T>> {

    /**
     * Validates that both the representative and parent quotient ring are non-null.
     *
     * @param representative the canonical remainder representative
     * @param parent the enclosing quotient ring {@code E / (m)}
     */
    public Residue {
        Objects.requireNonNull(representative, "representative cannot be null");
        Objects.requireNonNull(parent, "parent cannot be null");
    }

    /**
     * Checks whether this residue class is the additive identity {@code [0]} in {@link #parent()}.
     *
     * @return {@code true} if this residue is zero modulo {@code parent.modulus()}, {@code false} otherwise
     */
    public boolean isZero() {
        return parent.domain().isZero(representative);
    }

    /**
     * Checks whether this residue class is the multiplicative identity {@code [1]} in {@link #parent()}.
     *
     * @return {@code true} if this residue equals {@code parent.one()}, {@code false} otherwise
     */
    public boolean isOne() {
        return Objects.equals(representative, parent.one().representative());
    }

    /**
     * Checks whether this residue class is a multiplicative unit in {@link #parent()}.
     *
     * @return {@code true} if {@code gcd(representative, modulus)} is a unit, {@code false} otherwise
     */
    public boolean isUnit() {
        return tryReciprocal().isPresent();
    }

    @Override
    public Residue<T> add(Residue<T> other) {
        checkSameParent(other);
        return parent.of(parent.domain().add(this.representative, other.representative));
    }

    @Override
    public Residue<T> negate() {
        return parent.of(parent.domain().negate(this.representative));
    }

    @Override
    public Residue<T> subtract(Residue<T> other) {
        checkSameParent(other);
        return parent.of(parent.domain().subtract(this.representative, other.representative));
    }

    @Override
    public Residue<T> multiply(Residue<T> other) {
        checkSameParent(other);
        return parent.of(parent.domain().multiply(this.representative, other.representative));
    }

    /**
     * Attempts to compute the multiplicative inverse of this residue class via the extended Euclidean algorithm.
     *
     * @return an {@link Optional} containing the inverse residue if invertible, or {@link Optional#empty()} otherwise
     */
    public Optional<Residue<T>> tryReciprocal() {
        return parent.tryReciprocal(this);
    }

    /**
     * Returns the multiplicative inverse of this residue class in {@link #parent()}.
     *
     * @return the multiplicative inverse {@code this^-1}
     * @throws NonInvertibleElementException if this residue is not a unit in {@link #parent()}
     */
    public Residue<T> reciprocal() {
        return tryReciprocal().orElseThrow(
            () -> new NonInvertibleElementException(this + " is not invertible in " + parent)
        );
    }

    /**
     * Divides this residue class by an invertible residue {@code other} in the same {@link QuotientRing}.
     *
     * @param other the divisor residue class
     * @return the quotient {@code this * other^-1}
     * @throws IncompatibleAlgebraException if {@code other} belongs to a different quotient ring
     * @throws NonInvertibleElementException if {@code other} is not invertible in {@link #parent()}
     */
    public Residue<T> divide(Residue<T> other) {
        checkSameParent(other);
        return multiply(other.reciprocal());
    }

    private void checkSameParent(Residue<T> other) {
        Objects.requireNonNull(other, "other residue cannot be null");
        if (!Objects.equals(this.parent, other.parent)) {
            throw new IncompatibleAlgebraException(
                "Residues belong to incompatible quotient rings: " + this.parent + " vs " + other.parent
            );
        }
    }

    @Override
    public String toString() {
        return representative + " (mod " + parent.modulus() + ")";
    }
}
