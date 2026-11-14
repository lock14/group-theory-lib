package lock14.algebra.polynomial;

import java.util.Objects;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.Ring;

/**
 * The Univariate Polynomial Ring {@code R[x]} (where {@code x} commutes with scalars) over a possibly
 * non-commutative {@link Ring} {@code R} (e.g., {@code M_n(R)[x]} or {@code H(Q)[x]}).
 *
 * @param <T> coefficient element type
 * @param coefficientRing the underlying (possibly non-commutative) ring of coefficients
 */
public record NonCommutativePolynomialRing<T>(Ring<T> coefficientRing) implements Ring<Polynomial<T>> {

    /**
     * Validates that the coefficient ring is non-null.
     *
     * @param coefficientRing the underlying ring of coefficients
     */
    public NonCommutativePolynomialRing {
        Objects.requireNonNull(coefficientRing, "coefficientRing cannot be null");
    }

    /**
     * Constructs a polynomial in {@code R[x]} from ascending-degree coefficients.
     *
     * @param coeffs coefficients in ascending degree order {@code (a_0, a_1, ...)}
     * @return the canonical polynomial over {@link #coefficientRing()}
     */
    @SafeVarargs
    public final Polynomial<T> of(T... coeffs) {
        return Polynomial.of(coefficientRing, coeffs);
    }

    /**
     * Returns the indeterminate polynomial {@code x} in {@code R[x]}.
     *
     * @return the linear monomial {@code x}
     */
    public Polynomial<T> x() {
        return Polynomial.x(coefficientRing);
    }

    @Override
    public Polynomial<T> zero() {
        return Polynomial.zero(coefficientRing);
    }

    @Override
    public Polynomial<T> one() {
        return Polynomial.one(coefficientRing);
    }

    @Override
    public Polynomial<T> add(Polynomial<T> a, Polynomial<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.add(b);
    }

    @Override
    public Polynomial<T> negate(Polynomial<T> a) {
        checkInRing(a);
        return a.negate();
    }

    @Override
    public Polynomial<T> subtract(Polynomial<T> a, Polynomial<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.subtract(b);
    }

    @Override
    public Polynomial<T> multiply(Polynomial<T> a, Polynomial<T> b) {
        checkInRing(a);
        checkInRing(b);
        return a.multiply(b);
    }

    @Override
    public boolean isZero(Polynomial<T> element) {
        checkInRing(element);
        return element.isZero();
    }

    @Override
    public boolean isOne(Polynomial<T> element) {
        checkInRing(element);
        return element.isOne();
    }

    private void checkInRing(Polynomial<T> p) {
        Objects.requireNonNull(p, "polynomial cannot be null");
        if (!Objects.equals(p.ring(), this.coefficientRing)) {
            throw new IncompatibleAlgebraException(
                "Polynomial coefficient ring " + p.ring() + " does not match " + this.coefficientRing
            );
        }
    }
}
