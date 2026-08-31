package lock14.algebra.discrete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lock14.algebra.element.RingElement;
import lock14.algebra.structure.Ring;

/**
 * An immutable univariate polynomial P(x) = a0 + a1*x + a2*x^2 + ... + an*x^n over a Ring R.
 * Coefficients are stored in ascending order of degree.
 */
public record Polynomial<T>(List<T> coefficients, Ring<T> ring) implements RingElement<Polynomial<T>> {

    public Polynomial {
        Objects.requireNonNull(coefficients, "coefficients cannot be null");
        Objects.requireNonNull(ring, "ring cannot be null");

        // Trim trailing zeros to maintain canonical polynomial form
        List<T> trimmed = new ArrayList<>(coefficients);
        while (trimmed.size() > 1 && ring.isZero(trimmed.getLast())) {
            trimmed.removeLast();
        }
        if (trimmed.isEmpty()) {
            trimmed.add(ring.zero());
        }
        coefficients = Collections.unmodifiableList(trimmed);
    }

    @SafeVarargs
    public static <T> Polynomial<T> of(Ring<T> ring, T... coeffs) {
        return new Polynomial<>(List.of(coeffs), ring);
    }

    public static <T> Polynomial<T> of(Ring<T> ring, List<T> coeffs) {
        return new Polynomial<>(coeffs, ring);
    }

    public static <T> Polynomial<T> zero(Ring<T> ring) {
        return new Polynomial<>(List.of(ring.zero()), ring);
    }

    public static <T> Polynomial<T> one(Ring<T> ring) {
        return new Polynomial<>(List.of(ring.one()), ring);
    }

    public static <T> Polynomial<T> x(Ring<T> ring) {
        return new Polynomial<>(List.of(ring.zero(), ring.one()), ring);
    }

    public static <T> Polynomial<T> constant(Ring<T> ring, T constant) {
        return new Polynomial<>(List.of(constant), ring);
    }

    public boolean isZero() {
        return coefficients.size() == 1 && ring.isZero(coefficients.getFirst());
    }

    public boolean isOne() {
        return coefficients.size() == 1 && ring.isOne(coefficients.getFirst());
    }

    public int degree() {
        if (isZero()) {
            return -1; // Standard degree of zero polynomial
        }
        return coefficients.size() - 1;
    }

    public T coefficient(int deg) {
        if (deg < 0 || deg >= coefficients.size()) {
            return ring.zero();
        }
        return coefficients.get(deg);
    }

    public T leadingCoefficient() {
        return coefficients.getLast();
    }

    /**
     * Evaluates the polynomial at {@code x} using Horner's method.
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

    @Override
    public Polynomial<T> add(Polynomial<T> other) {
        checkSameRing(other);
        int maxLen = Math.max(this.coefficients.size(), other.coefficients.size());
        List<T> res = new ArrayList<>(maxLen);
        for (int i = 0; i < maxLen; i++) {
            T a = this.coefficient(i);
            T b = other.coefficient(i);
            res.add(ring.add(a, b));
        }
        return new Polynomial<>(res, ring);
    }

    @Override
    public Polynomial<T> negate() {
        List<T> res = new ArrayList<>(coefficients.size());
        for (T c : coefficients) {
            res.add(ring.negate(c));
        }
        return new Polynomial<>(res, ring);
    }

    @Override
    public Polynomial<T> subtract(Polynomial<T> other) {
        checkSameRing(other);
        return add(other.negate());
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
            T a = this.coefficient(i);
            if (ring.isZero(a)) continue;
            for (int j = 0; j <= other.degree(); j++) {
                T b = other.coefficient(j);
                if (ring.isZero(b)) continue;
                int k = i + j;
                T term = ring.multiply(a, b);
                res.set(k, ring.add(res.get(k), term));
            }
        }
        return new Polynomial<>(res, ring);
    }

    public Polynomial<T> derivative() {
        if (degree() <= 0) {
            return zero(ring);
        }
        List<T> res = new ArrayList<>(coefficients.size() - 1);
        for (int i = 1; i < coefficients.size(); i++) {
            T c = coefficients.get(i);
            res.add(ring.multiplyByScalar(c, i));
        }
        return new Polynomial<>(res, ring);
    }

    private void checkSameRing(Polynomial<T> other) {
        Objects.requireNonNull(other, "other cannot be null");
        if (!Objects.equals(this.ring, other.ring)) {
            throw new IllegalArgumentException("Cannot operate on polynomials over different rings");
        }
    }

    @Override
    public String toString() {
        if (isZero()) return "0";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < coefficients.size(); i++) {
            T c = coefficients.get(i);
            if (ring.isZero(c)) continue;
            if (!sb.isEmpty()) sb.append(" + ");
            if (i == 0) {
                sb.append(c);
            } else if (i == 1) {
                sb.append(c).append("x");
            } else {
                sb.append(c).append("x^").append(i);
            }
        }
        return sb.toString();
    }
}
