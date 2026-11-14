package lock14.algebra.linear;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import lock14.algebra.laws.VectorSpaceLaws;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.RationalField;
import lock14.algebra.structure.VectorSpace;

public class CoordinateVectorSpaceTest extends VectorSpaceLaws<Vector<Rational>, Rational> {

    private final CoordinateVectorSpace<Rational> space = new CoordinateVectorSpace<>(3, RationalField.INSTANCE);

    @Override
    protected VectorSpace<Vector<Rational>, Rational> vectorSpace() {
        return space;
    }

    @Provide
    Arbitrary<Rational> scalars() {
        return Arbitraries.integers().between(-20, 20).map(Rational::of);
    }

    @Provide
    Arbitrary<Rational> nonZeroScalars() {
        return Arbitraries.integers().between(-20, 20)
                .filter(n -> n != 0)
                .map(Rational::of);
    }

    @Provide
    Arbitrary<Vector<Rational>> vectors() {
        return scalars().list().ofSize(3)
                .map(coords -> Vector.of(RationalField.INSTANCE, coords));
    }
}
