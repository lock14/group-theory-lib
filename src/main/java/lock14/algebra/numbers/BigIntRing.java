package lock14.algebra.numbers;

import java.math.BigInteger;
import java.util.Objects;
import lock14.algebra.structure.EuclideanDomain;

/**
 * The Euclidean Domain of Integers (Z, +, *) over {@link BigInteger}.
 */
public final class BigIntRing implements EuclideanDomain<BigInteger> {

    public static final BigIntRing INSTANCE = new BigIntRing();

    private BigIntRing() {}

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
        if (b.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Division by zero");
        }
        BigInteger[] res = a.divideAndRemainder(b);
        return new DivisionResult<>(res[0], res[1]);
    }

    @Override
    public BigInteger gcd(BigInteger a, BigInteger b) {
        return a.gcd(b);
    }

    @Override
    public boolean isUnit(BigInteger element) {
        return element.equals(BigInteger.ONE) || element.equals(BigInteger.valueOf(-1));
    }
}
