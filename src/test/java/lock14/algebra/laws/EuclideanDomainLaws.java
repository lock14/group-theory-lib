package lock14.algebra.laws;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;
import lock14.algebra.structure.EuclideanDomain;
import lock14.algebra.structure.IntegralDomain;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class EuclideanDomainLaws<T> extends IntegralDomainLaws<T> {

    protected abstract EuclideanDomain<T> euclideanDomain();

    @Override
    protected IntegralDomain<T> integralDomain() {
        return euclideanDomain();
    }

    @Property
    public void euclideanDivisionIdentity(@ForAll("elements") T a, @ForAll("nonZeroElements") T b) {
        EuclideanDomain<T> d = euclideanDomain();
        EuclideanDomain.DivisionResult<T> res = d.divideAndRemainder(a, b);
        T reconstructed = d.add(d.multiply(res.quotient(), b), res.remainder());
        assertThat(reconstructed).isEqualTo(a);
        assertThat(d.divide(a, b)).isEqualTo(res.quotient());
        assertThat(d.remainder(a, b)).isEqualTo(res.remainder());
    }

    @Property
    public void gcdDividesBothOperands(@ForAll("elements") T a, @ForAll("nonZeroElements") T b) {
        EuclideanDomain<T> d = euclideanDomain();
        T g = d.gcd(a, b);
        assertThat(d.isZero(g)).isFalse();
        assertThat(d.isZero(d.remainder(a, g))).isTrue();
        assertThat(d.isZero(d.remainder(b, g))).isTrue();
    }

    @Property
    public void bezoutExtendedGcdIdentity(@ForAll("elements") T a, @ForAll("elements") T b) {
        EuclideanDomain<T> d = euclideanDomain();
        EuclideanDomain.ExtendedGcdResult<T> ext = d.extendedGcd(a, b);
        T linearCombination = d.add(d.multiply(a, ext.x()), d.multiply(b, ext.y()));
        assertThat(linearCombination).isEqualTo(ext.gcd());
    }

    @Property
    public void lcmIsCommonMultiple(@ForAll("nonZeroElements") T a, @ForAll("nonZeroElements") T b) {
        EuclideanDomain<T> d = euclideanDomain();
        T l = d.lcm(a, b);
        assertThat(d.isZero(l)).isFalse();
        assertThat(d.isZero(d.remainder(l, a))).isTrue();
        assertThat(d.isZero(d.remainder(l, b))).isTrue();
    }

    @Test
    public void divideAndRemainderByZeroThrows() {
        EuclideanDomain<T> d = euclideanDomain();
        assertThatThrownBy(() -> d.divideAndRemainder(d.one(), d.zero()))
                .isInstanceOf(ArithmeticException.class);
    }
}
