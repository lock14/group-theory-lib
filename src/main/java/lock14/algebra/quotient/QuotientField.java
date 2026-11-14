package lock14.algebra.quotient;

import java.util.Optional;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.structure.EuclideanDomain;
import lock14.algebra.structure.Field;

/**
 * Universal Quotient Field {@code E / (p)} modulo an irreducible non-unit element {@code p}
 * in a {@link EuclideanDomain} {@code E} (unifying prime fields {@code F_p = Z/pZ},
 * Galois extension fields {@code GF(p^k) = F_p[x]/(f(x))}, and number fields {@code Q[x]/(f(x))}).
 *
 * @param <T> underlying Euclidean domain element type
 */
public final class QuotientField<T> extends QuotientRing<T> implements Field<Residue<T>> {

    /**
     * Constructs the quotient field {@code E / (p)} over {@code domain} modulo an irreducible non-unit
     * element {@code irreducibleModulus}.
     *
     * @param domain the underlying Euclidean domain {@code E}
     * @param irreducibleModulus a non-zero, non-unit irreducible modulus {@code p} in {@code domain}
     * @throws IllegalArgumentException if {@code irreducibleModulus} is zero or a unit in {@code domain}
     */
    public QuotientField(EuclideanDomain<T> domain, T irreducibleModulus) {
        super(domain, irreducibleModulus);
        if (domain.isUnit(modulus())) {
            throw new IllegalArgumentException("QuotientField modulus cannot be a unit (0 != 1 required in a Field): " + modulus());
        }
    }

    @Override
    public Residue<T> reciprocal(Residue<T> a) {
        checkInQuotient(a);
        if (a.isZero()) {
            throw new NonInvertibleElementException("Cannot invert zero in QuotientField " + this);
        }
        Optional<Residue<T>> inv = tryReciprocal(a);
        return inv.orElseThrow(
            () -> new NonInvertibleElementException(
                "Modulus " + modulus() + " is reducible; non-zero element " + a + " is a zero divisor"
            )
        );
    }
}
