package lock14.algebra.quotient;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import lock14.algebra.polynomial.Polynomial;
import org.junit.jupiter.api.Test;

class QuotientExamplesTest {

    @Test
    void primeFieldAndAesGaloisFieldExample() {
        // @start region="quotient-and-galois"
        QuotientField<BigInteger> f17 = ZMod.field(17);
        Residue<BigInteger> invThree = ZMod.element(f17, 3).reciprocal();

        // AES Rijndael extension field GF(2^8) = F_2[x] / (1 + x + x^3 + x^4 + x^8)
        QuotientField<Polynomial<Residue<BigInteger>>> gf256 =
                GaloisFields.extension(2, 1, 1, 0, 1, 1, 0, 0, 0, 1);
        Residue<Polynomial<Residue<BigInteger>>> alpha =
                gf256.of(Polynomial.x(ZMod.field(2)));
        Residue<Polynomial<Residue<BigInteger>>> alphaInv = alpha.reciprocal();
        // @end

        assertThat(invThree).isEqualTo(ZMod.element(f17, 6));
        assertThat(alpha.multiply(alphaInv)).isEqualTo(gf256.one());
    }
}
