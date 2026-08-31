package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;
import lock14.algebra.structure.Field;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class ApproximateFieldLaws<T> {

    protected abstract Field<T> field();
    protected abstract double distance(T a, T b);
    protected abstract double tolerance();

    protected void assertClose(T actual, T expected) {
        double dist = distance(actual, expected);
        assertThat(dist)
            .as("Expected %s to be close to %s (distance %e <= %e)", actual, expected, dist, tolerance())
            .isLessThanOrEqualTo(tolerance());
    }

    @Property
    public void approximateAdditiveAssociativity(@ForAll("elements") T a, @ForAll("elements") T b, @ForAll("elements") T c) {
        Field<T> f = field();
        T left = f.add(f.add(a, b), c);
        T right = f.add(a, f.add(b, c));
        assertClose(left, right);
    }

    @Property
    public void approximateAdditiveIdentity(@ForAll("elements") T a) {
        Field<T> f = field();
        assertClose(f.add(a, f.zero()), a);
        assertClose(f.add(f.zero(), a), a);
    }

    @Property
    public void approximateAdditiveInverse(@ForAll("elements") T a) {
        Field<T> f = field();
        assertClose(f.add(a, f.negate(a)), f.zero());
        assertClose(f.add(f.negate(a), a), f.zero());
    }

    @Property
    public void approximateAdditiveCommutativity(@ForAll("elements") T a, @ForAll("elements") T b) {
        Field<T> f = field();
        assertClose(f.add(a, b), f.add(b, a));
    }

    @Property
    public void approximateMultiplicativeAssociativity(@ForAll("elements") T a, @ForAll("elements") T b, @ForAll("elements") T c) {
        Field<T> f = field();
        T left = f.multiply(f.multiply(a, b), c);
        T right = f.multiply(a, f.multiply(b, c));
        assertClose(left, right);
    }

    @Property
    public void approximateMultiplicativeIdentity(@ForAll("elements") T a) {
        Field<T> f = field();
        assertClose(f.multiply(a, f.one()), a);
        assertClose(f.multiply(f.one(), a), a);
    }

    @Property
    public void approximateMultiplicativeCommutativity(@ForAll("elements") T a, @ForAll("elements") T b) {
        Field<T> f = field();
        assertClose(f.multiply(a, b), f.multiply(b, a));
    }

    @Property
    public void approximateLeftDistributivity(@ForAll("elements") T a, @ForAll("elements") T b, @ForAll("elements") T c) {
        Field<T> f = field();
        T left = f.multiply(a, f.add(b, c));
        T right = f.add(f.multiply(a, b), f.multiply(a, c));
        assertClose(left, right);
    }

    @Property
    public void approximateNonZeroMultiplicativeInverse(@ForAll("nonZeroElements") T a) {
        Field<T> f = field();
        assertClose(f.multiply(a, f.reciprocal(a)), f.one());
        assertClose(f.multiply(f.reciprocal(a), a), f.one());
    }

    @Property
    public void approximateDivisionConsistency(@ForAll("elements") T a, @ForAll("nonZeroElements") T b) {
        Field<T> f = field();
        assertClose(f.divide(a, b), f.multiply(a, f.reciprocal(b)));
    }

    @Test
    public void zeroReciprocalThrows() {
        Field<T> f = field();
        assertThatThrownBy(() -> f.reciprocal(f.zero()))
                .isInstanceOf(ArithmeticException.class);
    }
}
