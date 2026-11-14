package lock14.algebra.polynomial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lock14.algebra.element.RingElement;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.structure.Ring;

/**
 * An immutable univariate polynomial {@code P(x) = a_0 + a_1*x + ... + a_n*x^n} over an exact {@link Ring} {@code R},
 * stored in canonical trimmed ascending-degree order with zero-copy internal construction.
 *
 * @param <T> coefficient element type
 */
public final class Polynomial<T> implements RingElement<Polynomial<T>> {

    private final List<T> coefficients;
    private final Ring<T> ring;

    private Polynomial(List<T> canonicalCoefficients, Ring<T> ring) {
        this.coefficients = canonicalCoefficients;
        this.ring = ring;
    }

    static <T> Polynomial<T> canonicalize(List<T> rawCoeffs, Ring<T> ring) {
        Objects.requireNonNull(rawCoeffs, "coefficients cannot be null");
        Objects.requireNonNull(ring, "ring cannot be null");
        int lastNonZero = -1;
        for (int i = 0; i < rawCoeffs.size(); i++) {
            T coeff = Objects.requireNonNull(rawCoeffs.get(i), "Polynomial coefficient cannot be null at index " + i);
            if (!ring.isZero(coeff)) {
                lastNonZero = i;
            }
        }
        if (lastNonZero < 0) {
            return new Polynomial<>(List.of(ring.zero()), ring);
        }
        return new Polynomial<>(List.copyOf(rawCoeffs.subList(0, lastNonZero + 1)), ring);
    }

    /**
     * Constructs a polynomial {@code a_0 + a_1*x + ... + a_k*x^k} from ascending-degree coefficients.
     *
     * @param <T> the coefficient element type
     * @param ring the coefficient ring {@code R}
     * @param coeffs coefficients in ascending degree order {@code (a_0, a_1, ...)}
     * @return the canonical polynomial over {@code ring}
     */
    @SafeVarargs
    public static <T> Polynomial<T> of(Ring<T> ring, T... coeffs) {
        Objects.requireNonNull(coeffs, "coeffs cannot be null");
        List<T> list = new ArrayList<>(coeffs.length);
        for (int i = 0; i < coeffs.length; i++) {
            list.add(Objects.requireNonNull(coeffs[i], "Polynomial coefficient cannot be null at index " + i));
        }
        return canonicalize(list, ring);
    }

    /**
     * Constructs a polynomial {@code a_0 + a_1*x + ... + a_k*x^k} from a list of ascending-degree coefficients.
     *
     * @param <T> the coefficient element type
     * @param ring the coefficient ring {@code R}
     * @param coeffs coefficients in ascending degree order {@code [a_0, a_1, ...]}
     * @return the canonical polynomial over {@code ring}
     */
    public static <T> Polynomial<T> of(Ring<T> ring, List<T> coeffs) {
        return canonicalize(coeffs, ring);
    }

    /**
     * Returns the zero polynomial {@code 0} over {@code ring}.
     *
     * @param <T> the coefficient element type
     * @param ring the coefficient ring {@code R}
     * @return the zero polynomial in {@code R[x]}
     */
    public static <T> Polynomial<T> zero(Ring<T> ring) {
        Objects.requireNonNull(ring, "ring cannot be null");
        return new Polynomial<>(List.of(ring.zero()), ring);
    }

    /**
     * Returns the multiplicative identity polynomial {@code 1} over {@code ring}.
     *
     * @param <T> the coefficient element type
     * @param ring the coefficient ring {@code R}
     * @return the unit polynomial {@code 1} in {@code R[x]}
     */
    public static <T> Polynomial<T> one(Ring<T> ring) {
        Objects.requireNonNull(ring, "ring cannot be null");
        T one = ring.one();
        if (ring.isZero(one)) {
            return zero(ring);
        }
        return new Polynomial<>(List.of(one), ring);
    }

    /**
     * Returns the indeterminate polynomial {@code x} over {@code ring}.
     *
     * @param <T> the coefficient element type
     * @param ring the coefficient ring {@code R}
     * @return the linear monomial {@code x} in {@code R[x]}
     */
    public static <T> Polynomial<T> x(Ring<T> ring) {
        Objects.requireNonNull(ring, "ring cannot be null");
        T one = ring.one();
        if (ring.isZero(one)) {
            return zero(ring);
        }
        return new Polynomial<>(List.of(ring.zero(), one), ring);
    }

    /**
     * Constructs the monomial {@code coeff * x^degree} over {@code ring}.
     *
     * @param <T> the coefficient element type
     * @param ring the coefficient ring {@code R}
     * @param degree the non-negative exponent of {@code x}
     * @param coeff the monomial coefficient
     * @return the canonical monomial {@code coeff * x^degree}
     * @throws IllegalArgumentException if {@code degree < 0}
     */
    public static <T> Polynomial<T> monomial(Ring<T> ring, int degree, T coeff) {
        Objects.requireNonNull(ring, "ring cannot be null");
        Objects.requireNonNull(coeff, "coeff cannot be null");
        if (degree < 0) {
            throw new IllegalArgumentException("Monomial degree must be non-negative: " + degree);
        }
        if (ring.isZero(coeff)) {
            return zero(ring);
        }
        List<T> list = new ArrayList<>(Collections.nCopies(degree + 1, ring.zero()));
        list.set(degree, coeff);
        return new Polynomial<>(List.copyOf(list), ring);
    }

    /**
     * Constructs the constant polynomial {@code constant} of degree at most {@code 0} over {@code ring}.
     *
     * @param <T> the coefficient element type
     * @param ring the coefficient ring {@code R}
     * @param constant the scalar constant term
     * @return the constant polynomial over {@code ring}
     */
    public static <T> Polynomial<T> constant(Ring<T> ring, T constant) {
        return monomial(ring, 0, constant);
    }

    /**
     * Returns the canonical trimmed ascending-degree coefficient list {@code [a_0, a_1, ..., a_n]}.
     *
     * @return an unmodifiable list of coefficients in ascending degree order
     */
    public List<T> coefficients() {
        return coefficients;
    }

    /**
     * Returns the underlying coefficient ring {@code R} of this polynomial.
     *
     * @return the coefficient ring {@code R}
     */
    public Ring<T> ring() {
        return ring;
    }

    /**
     * Checks whether this polynomial is the zero polynomial {@code 0}.
     *
     * @return {@code true} if all coefficients are zero, {@code false} otherwise
     */
    public boolean isZero() {
        return coefficients.size() == 1 && ring.isZero(coefficients.getFirst());
    }

    /**
     * Checks whether this polynomial is the multiplicative identity {@code 1}.
     *
     * @return {@code true} if this polynomial equals {@code 1} in {@code R[x]}, {@code false} otherwise
     */
    public boolean isOne() {
        return coefficients.size() == 1 && ring.isOne(coefficients.getFirst());
    }

    /**
     * Returns the degree of this polynomial, or {@code -1} if this is the zero polynomial.
     *
     * @return the highest degree with a non-zero coefficient, or {@code -1} for {@code 0}
     */
    public int degree() {
        return isZero() ? -1 : coefficients.size() - 1;
    }

    /**
     * Returns the coefficient {@code a_deg} of {@code x^deg} (returning {@code ring().zero()} if out of range).
     *
     * @param deg the exponent index
     * @return the coefficient of {@code x^deg}
     */
    public T coefficient(int deg) {
        if (deg < 0 || deg >= coefficients.size()) {
            return ring.zero();
        }
        return coefficients.get(deg);
    }

    /**
     * Returns the leading (highest-degree) coefficient of this polynomial ({@code 0} for the zero polynomial).
     *
     * @return the leading coefficient
     */
    public T leadingCoefficient() {
        return coefficients.getLast();
    }

    /**
     * Evaluates this polynomial at {@code x} via Horner's method ({@code a_0 + a_1*x + ... + a_n*x^n}).
     *
     * @param x the point in {@code R} at which to evaluate
     * @return the value {@code P(x)} in {@code R}
     */
    public T evaluate(T x) {
        Objects.requireNonNull(x, "x cannot be null");
        if (isZero()) {
            return ring.zero();
        }
        T result = coefficients.getLast();
        for (int i = coefficients.size() - 2; i >= 0; i--) {
            result = ring.add(ring.multiply(result, x), coefficients.get(i));
        }
        return result;
    }

    /**
     * Left-multiplies every coefficient of this polynomial by {@code scalar}.
     *
     * @param scalar the scalar factor in {@code R}
     * @return the scaled polynomial {@code scalar * P(x)}
     */
    public Polynomial<T> scale(T scalar) {
        Objects.requireNonNull(scalar, "scalar cannot be null");
        if (isZero() || ring.isZero(scalar)) {
            return zero(ring);
        }
        List<T> scaled = new ArrayList<>(coefficients.size());
        for (T c : coefficients) {
            scaled.add(ring.multiply(scalar, c));
        }
        return canonicalize(scaled, ring);
    }

    @Override
    public Polynomial<T> add(Polynomial<T> other) {
        checkSameRing(other);
        int maxLen = Math.max(this.coefficients.size(), other.coefficients.size());
        List<T> res = new ArrayList<>(maxLen);
        for (int i = 0; i < maxLen; i++) {
            res.add(ring.add(this.coefficient(i), other.coefficient(i)));
        }
        return canonicalize(res, ring);
    }

    @Override
    public Polynomial<T> negate() {
        if (isZero()) {
            return this;
        }
        List<T> res = new ArrayList<>(coefficients.size());
        for (T c : coefficients) {
            res.add(ring.negate(c));
        }
        return canonicalize(res, ring);
    }

    @Override
    public Polynomial<T> subtract(Polynomial<T> other) {
        checkSameRing(other);
        int maxLen = Math.max(this.coefficients.size(), other.coefficients.size());
        List<T> res = new ArrayList<>(maxLen);
        for (int i = 0; i < maxLen; i++) {
            res.add(ring.subtract(this.coefficient(i), other.coefficient(i)));
        }
        return canonicalize(res, ring);
    }

    @Override
    public Polynomial<T> multiply(Polynomial<T> other) {
        checkSameRing(other);
        if (this.isZero() || other.isZero()) {
            return zero(ring);
        }
        int newDeg = this.degree() + other.degree();
        List<T> res = new ArrayList<>(Collections.nCopies(newDeg + 1, ring.zero()));
        for (int i = 0; i <= this.degree(); i++) {
            T a = this.coefficients.get(i);
            if (ring.isZero(a)) continue;
            for (int j = 0; j <= other.degree(); j++) {
                T b = other.coefficients.get(j);
                if (ring.isZero(b)) continue;
                int k = i + j;
                res.set(k, ring.add(res.get(k), ring.multiply(a, b)));
            }
        }
        return canonicalize(res, ring);
    }

    /**
     * Computes the formal algebraic derivative {@code P'(x) = sum_{i=1}^n (i * a_i) x^{i-1}}.
     *
     * @return the formal derivative polynomial {@code P'(x)}
     */
    public Polynomial<T> derivative() {
        if (degree() <= 0) {
            return zero(ring);
        }
        List<T> res = new ArrayList<>(coefficients.size() - 1);
        for (int i = 1; i < coefficients.size(); i++) {
            res.add(ring.multiplyByScalar(coefficients.get(i), i));
        }
        return canonicalize(res, ring);
    }

    private void checkSameRing(Polynomial<T> other) {
        Objects.requireNonNull(other, "other cannot be null");
        if (!Objects.equals(this.ring, other.ring)) {
            throw new IncompatibleAlgebraException(
                "Cannot operate on polynomials over different coefficient rings: " + this.ring + " vs " + other.ring
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Polynomial<?> other
            && Objects.equals(ring, other.ring)
            && Objects.equals(coefficients, other.coefficients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ring, coefficients);
    }

    @Override
    public String toString() {
        if (isZero()) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < coefficients.size(); i++) {
            T c = coefficients.get(i);
            if (ring.isZero(c)) continue;
            if (!sb.isEmpty()) sb.append(" + ");
            if (i == 0) {
                sb.append(c);
            } else if (i == 1) {
                sb.append(c).append('x');
            } else {
                sb.append(c).append("x^").append(i);
            }
        }
        return sb.toString();
    }
}
