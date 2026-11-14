package lock14.algebra.discrete;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import lock14.algebra.laws.RingLaws;
import lock14.algebra.numbers.Quaternion;
import lock14.algebra.structure.Ring;

public class GeneralPolynomialRingTest extends RingLaws<Polynomial<Quaternion>> {

    private final GeneralPolynomialRing<Quaternion> genPolyRing = new GeneralPolynomialRing<>(Quaternion.ring());

    @Override
    protected Ring<Polynomial<Quaternion>> ring() {
        return genPolyRing;
    }

    @Provide
    Arbitrary<Polynomial<Quaternion>> elements() {
        Arbitrary<Integer> ints = Arbitraries.integers().between(-5, 5);
        Arbitrary<Quaternion> quats = ints.list().ofSize(4)
                .map(c -> Quaternion.of(c.get(0), c.get(1), c.get(2), c.get(3)));
        return quats.list().ofMinSize(1).ofMaxSize(4)
                .map(coeffs -> Polynomial.of(Quaternion.ring(), coeffs));
    }
}
