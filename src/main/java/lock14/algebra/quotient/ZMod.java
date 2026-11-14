package lock14.algebra.quotient;

import java.math.BigInteger;
import java.util.Objects;
import lock14.algebra.numbers.Integers;

/**
 * Factory methods for modular integer quotient rings {@code Z/nZ} and prime Galois fields {@code F_p}.
 */
public final class ZMod {

    private ZMod() {}

    public static QuotientRing<BigInteger> ring(long modulus) {
        return ring(BigInteger.valueOf(modulus));
    }

    public static QuotientRing<BigInteger> ring(BigInteger modulus) {
        Objects.requireNonNull(modulus, "modulus cannot be null");
        if (modulus.signum() <= 0) {
            throw new IllegalArgumentException("Modulus must be positive: " + modulus);
        }
        return new QuotientRing<>(Integers.RING, modulus);
    }

    public static QuotientField<BigInteger> field(long prime) {
        return field(BigInteger.valueOf(prime));
    }

    public static QuotientField<BigInteger> field(BigInteger prime) {
        Objects.requireNonNull(prime, "prime cannot be null");
        if (prime.signum() <= 0 || !prime.isProbablePrime(40)) {
            throw new IllegalArgumentException("Modulus " + prime + " is not prime; cannot form prime field F_p");
        }
        return new QuotientField<>(Integers.RING, prime);
    }

    public static Residue<BigInteger> element(QuotientRing<BigInteger> zModRing, long value) {
        Objects.requireNonNull(zModRing, "zModRing cannot be null");
        return zModRing.of(BigInteger.valueOf(value));
    }
}
