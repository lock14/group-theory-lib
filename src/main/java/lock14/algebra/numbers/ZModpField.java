package lock14.algebra.numbers;

import java.math.BigInteger;
import java.util.Objects;
import lock14.algebra.structure.Field;

/**
 * The Galois Field of integers modulo a prime p (F_p or GF(p)).
 */
public record ZModpField(BigInteger prime) implements Field<ModuloInteger> {

    public ZModpField {
        Objects.requireNonNull(prime, "prime cannot be null");
        if (prime.signum() <= 0) {
            throw new IllegalArgumentException("Modulus must be positive: " + prime);
        }
        if (!prime.isProbablePrime(30)) {
            throw new IllegalArgumentException("Modulus " + prime + " is not prime; cannot form a Field");
        }
    }

    public ZModpField(long prime) {
        this(BigInteger.valueOf(prime));
    }

    public ModuloInteger element(long value) {
        return ModuloInteger.of(BigInteger.valueOf(value), prime);
    }

    public ModuloInteger element(BigInteger value) {
        return ModuloInteger.of(value, prime);
    }

    @Override
    public ModuloInteger zero() {
        return ModuloInteger.zero(prime);
    }

    @Override
    public ModuloInteger one() {
        return ModuloInteger.one(prime);
    }

    @Override
    public ModuloInteger add(ModuloInteger a, ModuloInteger b) {
        return a.add(b);
    }

    @Override
    public ModuloInteger negate(ModuloInteger a) {
        return a.negate();
    }

    @Override
    public ModuloInteger subtract(ModuloInteger a, ModuloInteger b) {
        return a.subtract(b);
    }

    @Override
    public ModuloInteger multiply(ModuloInteger a, ModuloInteger b) {
        return a.multiply(b);
    }

    @Override
    public ModuloInteger reciprocal(ModuloInteger a) {
        return a.reciprocal();
    }

    @Override
    public ModuloInteger divide(ModuloInteger a, ModuloInteger b) {
        return a.divide(b);
    }

    @Override
    public boolean isZero(ModuloInteger element) {
        return element.isZero();
    }

    @Override
    public boolean isOne(ModuloInteger element) {
        return element.isOne();
    }
}
