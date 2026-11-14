package lock14.algebra.numbers;

import java.math.BigInteger;
import java.util.Objects;
import lock14.algebra.structure.CommutativeRing;

/**
 * The Commutative Ring of Integers Modulo n (Z / nZ).
 */
public record ZModnRing(BigInteger modulus) implements CommutativeRing<ModuloInteger> {

    public ZModnRing {
        Objects.requireNonNull(modulus, "modulus cannot be null");
        if (modulus.signum() <= 0) {
            throw new IllegalArgumentException("Modulus must be positive: " + modulus);
        }
    }

    public ZModnRing(long modulus) {
        this(BigInteger.valueOf(modulus));
    }

    public ModuloInteger element(long value) {
        return ModuloInteger.of(BigInteger.valueOf(value), modulus);
    }

    public ModuloInteger element(BigInteger value) {
        return ModuloInteger.of(value, modulus);
    }

    @Override
    public ModuloInteger zero() {
        return ModuloInteger.zero(modulus);
    }

    @Override
    public ModuloInteger one() {
        return ModuloInteger.one(modulus);
    }

    @Override
    public ModuloInteger add(ModuloInteger a, ModuloInteger b) {
        checkInRing(a);
        checkInRing(b);
        return a.add(b);
    }

    @Override
    public ModuloInteger negate(ModuloInteger a) {
        checkInRing(a);
        return a.negate();
    }

    @Override
    public ModuloInteger subtract(ModuloInteger a, ModuloInteger b) {
        checkInRing(a);
        checkInRing(b);
        return a.subtract(b);
    }

    @Override
    public ModuloInteger multiply(ModuloInteger a, ModuloInteger b) {
        checkInRing(a);
        checkInRing(b);
        return a.multiply(b);
    }

    @Override
    public boolean isZero(ModuloInteger element) {
        checkInRing(element);
        return element.isZero();
    }

    @Override
    public boolean isOne(ModuloInteger element) {
        checkInRing(element);
        return element.isOne();
    }

    private void checkInRing(ModuloInteger m) {
        Objects.requireNonNull(m, "element cannot be null");
        if (!m.modulus().equals(this.modulus)) {
            throw new IllegalArgumentException(
                "Element modulus " + m.modulus() + " does not match ring modulus " + this.modulus
            );
        }
    }
}
