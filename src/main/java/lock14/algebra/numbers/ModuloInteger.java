package lock14.algebra.numbers;

import java.math.BigInteger;
import java.util.Objects;
import lock14.algebra.context.AlgebraicContext;
import lock14.algebra.element.RingElement;
import lock14.algebra.exceptions.NonInvertibleElementException;

/**
 * An immutable element of the quotient ring Z / nZ (Integers modulo n).
 */
public record ModuloInteger(BigInteger value, BigInteger modulus) implements RingElement<ModuloInteger>, Comparable<ModuloInteger> {

    public ModuloInteger {
        Objects.requireNonNull(value, "value cannot be null");
        Objects.requireNonNull(modulus, "modulus cannot be null");
        if (modulus.signum() <= 0) {
            throw new IllegalArgumentException("Modulus must be positive: " + modulus);
        }
        value = value.mod(modulus);
    }

    public static ModuloInteger of(long value, long modulus) {
        return new ModuloInteger(BigInteger.valueOf(value), BigInteger.valueOf(modulus));
    }

    public static ModuloInteger of(long value, BigInteger modulus) {
        return new ModuloInteger(BigInteger.valueOf(value), modulus);
    }

    public static ModuloInteger of(BigInteger value, BigInteger modulus) {
        return new ModuloInteger(value, modulus);
    }

    public static ModuloInteger of(long value) {
        BigInteger ambient = AlgebraicContext.MODULUS.orElseThrow(
            () -> new IllegalStateException("No ambient modulus bound in AlgebraicContext.MODULUS")
        );
        return of(BigInteger.valueOf(value), ambient);
    }

    public static ModuloInteger of(BigInteger value) {
        BigInteger ambient = AlgebraicContext.MODULUS.orElseThrow(
            () -> new IllegalStateException("No ambient modulus bound in AlgebraicContext.MODULUS")
        );
        return of(value, ambient);
    }

    public static ModuloInteger zero(long modulus) {
        return new ModuloInteger(BigInteger.ZERO, BigInteger.valueOf(modulus));
    }

    public static ModuloInteger one(long modulus) {
        return new ModuloInteger(BigInteger.ONE, BigInteger.valueOf(modulus));
    }

    public static ModuloInteger zero(BigInteger modulus) {
        return new ModuloInteger(BigInteger.ZERO, modulus);
    }

    public static ModuloInteger one(BigInteger modulus) {
        return new ModuloInteger(BigInteger.ONE, modulus);
    }

    public boolean isZero() {
        return value.equals(BigInteger.ZERO);
    }

    public boolean isOne() {
        return value.equals(BigInteger.ONE);
    }

    public boolean isInvertible() {
        return !isZero() && value.gcd(modulus).equals(BigInteger.ONE);
    }

    @Override
    public ModuloInteger add(ModuloInteger other) {
        checkSameModulus(other);
        return new ModuloInteger(this.value.add(other.value), this.modulus);
    }

    @Override
    public ModuloInteger negate() {
        if (isZero()) return this;
        return new ModuloInteger(this.modulus.subtract(this.value), this.modulus);
    }

    @Override
    public ModuloInteger subtract(ModuloInteger other) {
        checkSameModulus(other);
        return new ModuloInteger(this.value.subtract(other.value), this.modulus);
    }

    @Override
    public ModuloInteger multiply(ModuloInteger other) {
        checkSameModulus(other);
        return new ModuloInteger(this.value.multiply(other.value), this.modulus);
    }

    public ModuloInteger reciprocal() {
        if (isZero()) {
            throw new ArithmeticException("Cannot invert zero in Z/" + modulus + "Z");
        }
        if (!isInvertible()) {
            throw new NonInvertibleElementException(this.value + " is not coprime with modulus " + this.modulus);
        }
        return new ModuloInteger(this.value.modInverse(this.modulus), this.modulus);
    }

    public ModuloInteger divide(ModuloInteger other) {
        return multiply(other.reciprocal());
    }

    public ModuloInteger power(long exponent) {
        if (exponent == 0) {
            return one(modulus);
        }
        if (exponent < 0) {
            return reciprocal().power(-exponent);
        }
        return new ModuloInteger(this.value.modPow(BigInteger.valueOf(exponent), this.modulus), this.modulus);
    }

    @Override
    public int compareTo(ModuloInteger other) {
        checkSameModulus(other);
        return this.value.compareTo(other.value);
    }

    private void checkSameModulus(ModuloInteger other) {
        Objects.requireNonNull(other, "other cannot be null");
        if (!this.modulus.equals(other.modulus)) {
            throw new IllegalArgumentException("Modulus mismatch: " + this.modulus + " vs " + other.modulus);
        }
    }

    @Override
    public String toString() {
        return value + " (mod " + modulus + ")";
    }
}
