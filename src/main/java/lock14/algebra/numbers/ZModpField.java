package lock14.algebra.numbers;

import java.math.BigInteger;
import lock14.algebra.structure.Field;

/**
 * The Galois Field of integers modulo a prime p (F_p or GF(p)).
 */
public final class ZModpField extends ZModnRing implements Field<ModuloInteger> {

    public ZModpField(BigInteger prime) {
        super(prime);
        if (!prime.isProbablePrime(30)) {
            throw new IllegalArgumentException("Modulus " + prime + " is not prime; cannot form a Field");
        }
    }

    public ZModpField(long prime) {
        this(BigInteger.valueOf(prime));
    }

    @Override
    public ModuloInteger reciprocal(ModuloInteger a) {
        return a.reciprocal();
    }

    @Override
    public ModuloInteger divide(ModuloInteger a, ModuloInteger b) {
        return a.divide(b);
    }
}
