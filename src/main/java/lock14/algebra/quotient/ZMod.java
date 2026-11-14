package lock14.algebra.quotient;

import java.math.BigInteger;
import java.util.Objects;
import lock14.algebra.numbers.Integers;

/**
 * Factory methods for modular integer quotient rings {@code Z/nZ} and prime Galois fields {@code F_p}.
 */
public final class ZMod {

    private ZMod() {}

    /**
     * Constructs the modular integer quotient ring {@code Z/nZ} for a positive {@code long} modulus.
     *
     * @param modulus the positive integer modulus {@code n} ({@code modulus >= 1})
     * @return the quotient ring {@code Z/nZ}
     * @throws IllegalArgumentException if {@code modulus <= 0}
     */
    public static QuotientRing<BigInteger> ring(long modulus) {
        return ring(BigInteger.valueOf(modulus));
    }

    /**
     * Constructs the modular integer quotient ring {@code Z/nZ} for a positive {@link BigInteger} modulus.
     *
     * @param modulus the positive integer modulus {@code n} ({@code modulus >= 1})
     * @return the quotient ring {@code Z/nZ}
     * @throws IllegalArgumentException if {@code modulus <= 0}
     */
    public static QuotientRing<BigInteger> ring(BigInteger modulus) {
        Objects.requireNonNull(modulus, "modulus cannot be null");
        if (modulus.signum() <= 0) {
            throw new IllegalArgumentException("Modulus must be positive: " + modulus);
        }
        return new QuotientRing<>(Integers.RING, modulus);
    }

    /**
     * Constructs the prime Galois field {@code F_p = Z/pZ} for a positive prime {@code p}.
     *
     * @param prime the prime modulus {@code p}
     * @return the prime field {@code F_p}
     * @throws IllegalArgumentException if {@code prime} is not prime
     */
    public static QuotientField<BigInteger> field(long prime) {
        return field(BigInteger.valueOf(prime));
    }

    /**
     * Constructs the prime Galois field {@code F_p = Z/pZ} for a positive probable-prime {@link BigInteger} {@code p}.
     *
     * @param prime the prime modulus {@code p}
     * @return the prime field {@code F_p}
     * @throws IllegalArgumentException if {@code prime} is not positive or fails primality testing
     */
    public static QuotientField<BigInteger> field(BigInteger prime) {
        Objects.requireNonNull(prime, "prime cannot be null");
        if (prime.signum() <= 0 || !prime.isProbablePrime(40)) {
            throw new IllegalArgumentException("Modulus " + prime + " is not prime; cannot form prime field F_p");
        }
        return new QuotientField<>(Integers.RING, prime);
    }

    /**
     * Projects a {@code long} integer into its canonical residue class inside {@code zModRing}.
     *
     * @param zModRing the target modular integer quotient ring {@code Z/nZ}
     * @param value the integer value to reduce modulo {@code n}
     * @return the canonical residue {@code [value mod n]} in {@code zModRing}
     */
    public static Residue<BigInteger> element(QuotientRing<BigInteger> zModRing, long value) {
        Objects.requireNonNull(zModRing, "zModRing cannot be null");
        return zModRing.of(BigInteger.valueOf(value));
    }
}
