package lock14.algebra.numbers;

import java.math.BigInteger;
import java.util.Objects;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.structure.EuclideanDomain;

/**
 * The Exact Euclidean Domain of Integers {@code (Z, +, *)} backed by {@link BigInteger},
 * using canonical least-non-negative Euclidean division ({@code a = q * b + r} with {@code 0 <= r < |b|}).
 */
public final class Integers implements EuclideanDomain<BigInteger> {

    public static final Integers RING = new Integers();

    private Integers() {}

    @Override
    public BigInteger zero() {
        return BigInteger.ZERO;
    }

    @Override
    public BigInteger one() {
        return BigInteger.ONE;
    }

    @Override
    public BigInteger add(BigInteger a, BigInteger b) {
        return a.add(b);
    }

    @Override
    public BigInteger negate(BigInteger a) {
        return a.negate();
    }

    @Override
    public BigInteger subtract(BigInteger a, BigInteger b) {
        return a.subtract(b);
    }

    @Override
    public BigInteger multiply(BigInteger a, BigInteger b) {
        return a.multiply(b);
    }

    @Override
    public DivisionResult<BigInteger> divideAndRemainder(BigInteger a, BigInteger b) {
        Objects.requireNonNull(a, "dividend cannot be null");
        Objects.requireNonNull(b, "divisor cannot be null");
        if (b.signum() == 0) {
            throw new NonInvertibleElementException("Integer division by zero");
        }
        BigInteger[] qr = a.divideAndRemainder(b);
        BigInteger q = qr[0];
        BigInteger r = qr[1];
        if (r.signum() < 0) {
            if (b.signum() > 0) {
                q = q.subtract(BigInteger.ONE);
                r = r.add(b);
            } else {
                q = q.add(BigInteger.ONE);
                r = r.subtract(b);
            }
        }
        return new DivisionResult<>(q, r);
    }

    @Override
    public BigInteger normalize(BigInteger element) {
        Objects.requireNonNull(element, "element cannot be null");
        return element.abs();
    }

    @Override
    public BigInteger gcd(BigInteger a, BigInteger b) {
        Objects.requireNonNull(a, "a cannot be null");
        Objects.requireNonNull(b, "b cannot be null");
        return a.gcd(b);
    }

    @Override
    public BigInteger lcm(BigInteger a, BigInteger b) {
        Objects.requireNonNull(a, "a cannot be null");
        Objects.requireNonNull(b, "b cannot be null");
        if (a.signum() == 0 || b.signum() == 0) {
            return BigInteger.ZERO;
        }
        return a.divide(a.gcd(b)).multiply(b).abs();
    }

    @Override
    public boolean isUnit(BigInteger element) {
        Objects.requireNonNull(element, "element cannot be null");
        return element.abs().equals(BigInteger.ONE);
    }

    @Override
    public String toString() {
        return "Z";
    }
}
