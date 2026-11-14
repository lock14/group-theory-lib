package lock14.algebra.quotient;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import lock14.algebra.laws.FieldLaws;
import lock14.algebra.polynomial.Polynomial;
import lock14.algebra.structure.Field;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GaloisFieldTest extends FieldLaws<Residue<Polynomial<Residue<BigInteger>>>> {

    // GF(2^4) = F_2[x] / (x^4 + x + 1)
    private final QuotientField<BigInteger> f2 = ZMod.field(2);
    private final QuotientField<Polynomial<Residue<BigInteger>>> gf16 =
        GaloisFields.extension(2, 1, 1, 0, 0, 1);

    @Override
    protected Field<Residue<Polynomial<Residue<BigInteger>>>> field() {
        return gf16;
    }

    @Provide
    Arbitrary<Residue<Polynomial<Residue<BigInteger>>>> elements() {
        return Arbitraries.integers().between(0, 15).map(mask -> fromBitMask(gf16, f2, mask, 4));
    }

    @Provide
    Arbitrary<Residue<Polynomial<Residue<BigInteger>>>> nonZeroElements() {
        return Arbitraries.integers().between(1, 15).map(mask -> fromBitMask(gf16, f2, mask, 4));
    }

    private static Residue<Polynomial<Residue<BigInteger>>> fromBitMask(
            QuotientField<Polynomial<Residue<BigInteger>>> gf,
            QuotientField<BigInteger> f2,
            int mask,
            int bits) {
        List<Residue<BigInteger>> coeffs = new ArrayList<>(bits);
        for (int i = 0; i < bits; i++) {
            coeffs.add(((mask >> i) & 1) == 1 ? f2.one() : f2.zero());
        }
        return gf.of(Polynomial.of(f2, coeffs));
    }

    @Test
    public void testAesGaloisFieldGF256RijndaelInverseAndRabinRejection() {
        // AES Rijndael field GF(2^8) = F_2[x] / (x^8 + x^4 + x^3 + x + 1)
        var gf256 = GaloisFields.extension(2, 1, 1, 0, 1, 1, 0, 0, 0, 1);

        // Famous Rijndael S-Box test vector: inverse of 0x53 is 0xCA in GF(2^8)
        var hex53 = fromBitMask(gf256, f2, 0x53, 8);
        var hexCA = fromBitMask(gf256, f2, 0xCA, 8);
        assertThat(gf256.reciprocal(hex53)).isEqualTo(hexCA);
        assertThat(gf256.multiply(hex53, hexCA)).isEqualTo(gf256.one());

        // Reducible polynomial x^4 + x^2 + 1 = (x^2 + x + 1)^2 over F_2 must be rejected by Rabin's test
        assertThatThrownBy(() -> GaloisFields.extension(2, 1, 0, 1, 0, 1))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
