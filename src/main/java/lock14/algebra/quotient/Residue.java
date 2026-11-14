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
 */
public record Residue<T>(T representative, QuotientRing<T> parent) implements RingElement<Residue<T>> {

    public Residue {
        Objects.requireNonNull(representative, "representative cannot be null");
        Objects.requireNonNull(parent, "parent cannot be null");
    }

    public boolean isZero() {
        return parent.domain().isZero(representative);
    }

    public boolean isOne() {
        return Objects.equals(representative, parent.one().representative());
    }

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

    public Optional<Residue<T>> tryReciprocal() {
        return parent.tryReciprocal(this);
    }

    public Residue<T> reciprocal() {
        return tryReciprocal().orElseThrow(
            () -> new NonInvertibleElementException(this + " is not invertible in " + parent)
        );
    }

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
