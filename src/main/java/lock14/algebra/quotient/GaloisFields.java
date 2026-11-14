package lock14.algebra.quotient;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lock14.algebra.polynomial.Polynomial;
import lock14.algebra.polynomial.PolynomialEuclideanDomain;
import lock14.algebra.polynomial.PolynomialRing;

/**
 * Exact constructions and Rabin irreducibility verification for Finite Extension Fields
 * {@code GF(p^k) = F_p[x] / (f(x))}.
 */
public final class GaloisFields {

    private GaloisFields() {}

    /**
     * Constructs {@code GF(p^k) = F_p[x] / (modulus)} after verifying that {@code modulus} is irreducible over {@code F_p}.
     */
    public static QuotientField<Polynomial<Residue<BigInteger>>> extension(
            QuotientField<BigInteger> primeField,
            Polynomial<Residue<BigInteger>> irreducibleModulus) {
        Objects.requireNonNull(primeField, "primeField cannot be null");
        Objects.requireNonNull(irreducibleModulus, "irreducibleModulus cannot be null");
        PolynomialEuclideanDomain<Residue<BigInteger>> fpx = PolynomialRing.overField(primeField);
        if (!Objects.equals(irreducibleModulus.ring(), primeField)) {
            throw new IllegalArgumentException("Modulus polynomial does not have coefficients over " + primeField);
        }
        if (irreducibleModulus.degree() < 1) {
            throw new IllegalArgumentException("Extension modulus degree must be >= 1");
        }
        if (!isIrreducible(primeField, irreducibleModulus)) {
            throw new IllegalArgumentException("Polynomial " + irreducibleModulus + " is reducible over " + primeField);
        }
        return new QuotientField<>(fpx, irreducibleModulus);
    }

    /**
     * Convenience factory defining {@code GF(p^k)} from integer polynomial coefficients in ascending degree order
     * (e.g., {@code GaloisFields.extension(2, 1, 1, 0, 1, 1, 0, 0, 0, 1)} for AES {@code GF(2^8)}).
     */
    public static QuotientField<Polynomial<Residue<BigInteger>>> extension(long prime, long... ascendingCoeffs) {
        QuotientField<BigInteger> fp = ZMod.field(prime);
        Objects.requireNonNull(ascendingCoeffs, "ascendingCoeffs cannot be null");
        List<Residue<BigInteger>> coeffs = new ArrayList<>(ascendingCoeffs.length);
        for (long c : ascendingCoeffs) {
            coeffs.add(fp.of(BigInteger.valueOf(c)));
        }
        Polynomial<Residue<BigInteger>> modPoly = Polynomial.of(fp, coeffs);
        return extension(fp, modPoly);
    }

    /**
     * Rabin's deterministic polynomial-time irreducibility test over {@code F_p[x]}.
     */
    public static boolean isIrreducible(
            QuotientField<BigInteger> primeField,
            Polynomial<Residue<BigInteger>> f) {
        Objects.requireNonNull(primeField, "primeField cannot be null");
        Objects.requireNonNull(f, "polynomial cannot be null");
        PolynomialEuclideanDomain<Residue<BigInteger>> fpx = PolynomialRing.overField(primeField);
        int n = f.degree();
        if (n < 1) {
            return false;
        }
        if (n == 1) {
            return true;
        }

        Polynomial<Residue<BigInteger>> monicF = fpx.normalize(f);
        BigInteger p = primeField.modulus();
        Polynomial<Residue<BigInteger>> x = fpx.x();

        for (int q : distinctPrimeDivisors(n)) {
            BigInteger exp = p.pow(n / q);
            Polynomial<Residue<BigInteger>> xPowMod = modPow(x, exp, monicF, fpx);
            Polynomial<Residue<BigInteger>> diff = fpx.subtract(xPowMod, x);
            Polynomial<Residue<BigInteger>> g = fpx.gcd(monicF, diff);
            if (!fpx.isOne(g)) {
                return false;
            }
        }

        BigInteger fullExp = p.pow(n);
        Polynomial<Residue<BigInteger>> xFull = modPow(x, fullExp, monicF, fpx);
        return fpx.isZero(fpx.subtract(xFull, x));
    }

    private static Polynomial<Residue<BigInteger>> modPow(
            Polynomial<Residue<BigInteger>> base,
            BigInteger exponent,
            Polynomial<Residue<BigInteger>> modulus,
            PolynomialEuclideanDomain<Residue<BigInteger>> fpx) {
        Polynomial<Residue<BigInteger>> result = fpx.one();
        Polynomial<Residue<BigInteger>> current = fpx.remainder(base, modulus);
        for (int bit = 0; bit < exponent.bitLength(); bit++) {
            if (exponent.testBit(bit)) {
                result = fpx.remainder(fpx.multiply(result, current), modulus);
            }
            if (bit + 1 < exponent.bitLength()) {
                current = fpx.remainder(fpx.multiply(current, current), modulus);
            }
        }
        return result;
    }

    private static List<Integer> distinctPrimeDivisors(int n) {
        List<Integer> primes = new ArrayList<>();
        int rem = n;
        for (int d = 2; d * d <= rem; d++) {
            if (rem % d == 0) {
                primes.add(d);
                while (rem % d == 0) {
                    rem /= d;
                }
            }
        }
        if (rem > 1) {
            primes.add(rem);
        }
        return primes;
    }
}
