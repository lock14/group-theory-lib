package lock14.algebra.numeric;

import lock14.algebra.exceptions.NonInvertibleElementException;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class Quaternion64Test {

    @Provide
    Arbitrary<Quaternion64> quaternions() {
        Arbitrary<Double> comp = Arbitraries.doubles().between(-20.0, 20.0);
        Arbitrary<Quaternion64> random = comp.flatMap(w ->
            comp.flatMap(x ->
                comp.flatMap(y ->
                    comp.map(z -> Quaternion64.of(w, x, y, z)))));
        return Arbitraries.oneOf(
            Arbitraries.of(
                Quaternion64.ZERO,
                Quaternion64.ONE,
                Quaternion64.I,
                Quaternion64.J,
                Quaternion64.K,
                Quaternion64.ONE.negate(),
                Quaternion64.I.negate(),
                Quaternion64.J.negate(),
                Quaternion64.K.negate()
            ),
            random
        );
    }

    @Provide
    Arbitrary<Quaternion64> nonZeroQuaternions() {
        return quaternions().filter(q -> q.abs() > 1e-3);
    }

    @Property
    public void multiplicativeAssociativityAndNormHomomorphism(
            @ForAll("quaternions") Quaternion64 p,
            @ForAll("quaternions") Quaternion64 q,
            @ForAll("quaternions") Quaternion64 r) {
        Quaternion64 lhs = p.multiply(q).multiply(r);
        Quaternion64 rhs = p.multiply(q.multiply(r));
        double scale = Math.max(1.0, Math.max(lhs.abs(), rhs.abs()));
        assertThat(lhs.subtract(rhs).abs() / scale).isLessThanOrEqualTo(1e-9);

        double prodNorm = p.multiply(q).abs();
        double expectedNorm = p.abs() * q.abs();
        double normScale = Math.max(1.0, expectedNorm);
        assertThat(Math.abs(prodNorm - expectedNorm) / normScale).isLessThanOrEqualTo(1e-10);

        // Conjugation is an anti-involution: conjugate(p * q) = conjugate(q) * conjugate(p)
        Quaternion64 conjProd = p.multiply(q).conjugate();
        Quaternion64 revConj = q.conjugate().multiply(p.conjugate());
        assertThat(conjProd.subtract(revConj).abs() / normScale).isLessThanOrEqualTo(1e-10);
    }

    @Property
    public void leftRightDivisionAndRotationComposition(
            @ForAll("quaternions") Quaternion64 p,
            @ForAll("nonZeroQuaternions") Quaternion64 q,
            @ForAll("nonZeroQuaternions") Quaternion64 r) {
        double scale = Math.max(1.0, p.abs());
        assertThat(p.multiply(q).divide(q).subtract(p).abs() / scale).isLessThanOrEqualTo(1e-8);
        assertThat(q.multiply(p).divideLeft(q).subtract(p).abs() / scale).isLessThanOrEqualTo(1e-8);

        double[] composed = q.multiply(r).rotate(1.5, -2.0, 0.75);
        double[] step1 = r.rotate(1.5, -2.0, 0.75);
        double[] sequential = q.rotate(step1[0], step1[1], step1[2]);
        assertThat(composed[0]).isCloseTo(sequential[0], within(1e-9));
        assertThat(composed[1]).isCloseTo(sequential[1], within(1e-9));
        assertThat(composed[2]).isCloseTo(sequential[2], within(1e-9));
    }

    @Test
    public void testHamiltonBasisIdentitiesAndFactories() {
        Quaternion64 canonicalZero = new Quaternion64(-0.0, -0.0, -0.0, -0.0);
        assertThat(Double.doubleToRawLongBits(canonicalZero.w())).isZero();
        assertThat(Double.doubleToRawLongBits(canonicalZero.x())).isZero();
        assertThat(Double.doubleToRawLongBits(canonicalZero.y())).isZero();
        assertThat(Double.doubleToRawLongBits(canonicalZero.z())).isZero();

        assertThat(Quaternion64.ZERO.isZero()).isTrue();
        assertThat(Quaternion64.ONE.isZero()).isFalse();
        assertThat(Quaternion64.I.isZero()).isFalse();
        assertThat(Quaternion64.J.isZero()).isFalse();
        assertThat(Quaternion64.K.isZero()).isFalse();

        Quaternion64 minusOne = Quaternion64.scalar(-1.0);
        assertThat(Quaternion64.I.multiply(Quaternion64.I)).isEqualTo(minusOne);
        assertThat(Quaternion64.J.multiply(Quaternion64.J)).isEqualTo(minusOne);
        assertThat(Quaternion64.K.multiply(Quaternion64.K)).isEqualTo(minusOne);
        assertThat(Quaternion64.I.multiply(Quaternion64.J).multiply(Quaternion64.K)).isEqualTo(minusOne);

        assertThat(Quaternion64.I.multiply(Quaternion64.J)).isEqualTo(Quaternion64.K);
        assertThat(Quaternion64.J.multiply(Quaternion64.I)).isEqualTo(Quaternion64.K.negate());
        assertThat(Quaternion64.J.multiply(Quaternion64.K)).isEqualTo(Quaternion64.I);
        assertThat(Quaternion64.K.multiply(Quaternion64.J)).isEqualTo(Quaternion64.I.negate());
        assertThat(Quaternion64.K.multiply(Quaternion64.I)).isEqualTo(Quaternion64.J);
        assertThat(Quaternion64.I.multiply(Quaternion64.K)).isEqualTo(Quaternion64.J.negate());

        Quaternion64 p = Quaternion64.pure(1.0, -2.0, 3.0);
        assertThat(p).isEqualTo(Quaternion64.of(0.0, 1.0, -2.0, 3.0));
        assertThat(Quaternion64.of(1.0, 2.0, 3.0, 4.0).normSq()).isEqualTo(30.0);
        assertThat(p.conjugate()).isEqualTo(Quaternion64.of(0.0, -1.0, 2.0, -3.0));
        assertThat(p.scale(2.0)).isEqualTo(Quaternion64.of(0.0, 2.0, -4.0, 6.0));
        assertThat(p.add(Quaternion64.ONE)).isEqualTo(Quaternion64.of(1.0, 1.0, -2.0, 3.0));
        assertThat(p.dot(Quaternion64.of(5.0, 2.0, 3.0, 4.0))).isEqualTo(8.0);
    }

    @Test
    public void testSpatialAxisAngleRotationAndSlerp() {
        // 90-degree counterclockwise rotation around +Z maps (1, 0, 0) -> (0, 1, 0)
        Quaternion64 rotZ90 = Quaternion64.fromAxisAngle(0.0, 0.0, 1.0, Math.PI / 2.0);
        double[] rotated = rotZ90.rotateVector(1.0, 0.0, 0.0);
        assertThat(rotated[0]).isCloseTo(0.0, within(1e-12));
        assertThat(rotated[1]).isCloseTo(1.0, within(1e-12));
        assertThat(rotated[2]).isCloseTo(0.0, within(1e-12));

        // Non-unit axis is normalized automatically
        Quaternion64 nonUnitAxis = Quaternion64.fromAxisAngle(0.0, 0.0, 5.0, Math.PI / 2.0);
        assertThat(nonUnitAxis.approxEquals(rotZ90, 1e-12)).isTrue();
        assertThatThrownBy(() -> Quaternion64.fromAxisAngle(0.0, 0.0, 0.0, 1.0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Quaternion64.fromAxisAngle(Double.NaN, 1.0, 0.0, 1.0))
            .isInstanceOf(IllegalArgumentException.class);

        // Halfway SLERP between identity (0 deg) and 90 deg around +Z is 45 deg around +Z
        Quaternion64 mid = Quaternion64.ONE.slerp(rotZ90, 0.5);
        double[] midVec = mid.rotate(1.0, 0.0, 0.0);
        double invSqrt2 = Math.sqrt(0.5);
        assertThat(midVec[0]).isCloseTo(invSqrt2, within(1e-12));
        assertThat(midVec[1]).isCloseTo(invSqrt2, within(1e-12));
        assertThat(midVec[2]).isCloseTo(0.0, within(1e-12));

        // Boundary parameters t = 0 and t = 1 on orthogonal quaternions
        assertThat(Quaternion64.ONE.slerp(Quaternion64.I, 0.0).approxEquals(Quaternion64.ONE, 1e-12)).isTrue();
        assertThat(Quaternion64.ONE.slerp(Quaternion64.I, 1.0).approxEquals(Quaternion64.I, 1e-12)).isTrue();

        // Negative dot product (-rotZ90 represents the identical 3D rotation): shortest-arc negation path
        Quaternion64 negDotMid = Quaternion64.ONE.slerp(rotZ90.negate(), 0.5);
        assertThat(negDotMid.approxEquals(mid, 1e-12)).isTrue();
        assertThat(Quaternion64.ONE.slerp(Quaternion64.ONE.negate(), 0.5)
            .approxEquals(Quaternion64.ONE, 1e-12)).isTrue();

        // Nearly identical quaternions (cosTheta > 0.9995): normalized linear fallback path
        Quaternion64 tinyRot = Quaternion64.fromAxisAngle(0.0, 0.0, 1.0, 1e-4);
        Quaternion64 tinyMid = Quaternion64.ONE.slerp(tinyRot, 0.5);
        Quaternion64 expectedTinyMid = Quaternion64.fromAxisAngle(0.0, 0.0, 1.0, 0.5e-4);
        assertThat(tinyMid.approxEquals(expectedTinyMid, 1e-12)).isTrue();

        assertThatThrownBy(Quaternion64.ZERO::normalizeUnit)
            .isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> Quaternion64.ZERO.rotate(1.0, 0.0, 0.0))
            .isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> Quaternion64.ZERO.slerp(Quaternion64.ONE, 0.5))
            .isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> Quaternion64.ONE.slerp(Quaternion64.ZERO, 0.5))
            .isInstanceOf(NonInvertibleElementException.class);
    }

    @Test
    public void testExtremeScaleNormReciprocalAndDivision() {
        Quaternion64 huge = Quaternion64.of(1e200, 2e200, -1e200, 2e200);
        assertThat(huge.abs()).isCloseTo(Math.sqrt(10.0) * 1e200, within(1e186));
        assertThat(huge.multiply(huge.reciprocal()).approxEquals(Quaternion64.ONE, 1e-12)).isTrue();

        Quaternion64 tiny = Quaternion64.of(1e-200, 2e-200, -1e-200, 2e-200);
        assertThat(tiny.abs()).isCloseTo(Math.sqrt(10.0) * 1e-200, within(1e-214));
        assertThat(tiny.multiply(tiny.reciprocal()).approxEquals(Quaternion64.ONE, 1e-12)).isTrue();

        assertThat(Quaternion64.ZERO.abs()).isEqualTo(0.0);
        assertThat(Quaternion64.of(1.0, Double.NEGATIVE_INFINITY, 0.0, 0.0).abs())
            .isEqualTo(Double.POSITIVE_INFINITY);
        assertThat(Quaternion64.of(Double.NaN, 1.0, 0.0, 0.0).abs()).isNaN();

        assertThat(Quaternion64.scalar(Double.POSITIVE_INFINITY).reciprocal()).isEqualTo(Quaternion64.ZERO);
        assertThat(Quaternion64.ZERO.tryReciprocal()).isEmpty();
        assertThat(Quaternion64.I.tryReciprocal()).contains(Quaternion64.I.negate());
        assertThatThrownBy(Quaternion64.ZERO::reciprocal).isInstanceOf(NonInvertibleElementException.class);

        // Non-commutative right vs left division: i * j^-1 = -k whereas j^-1 * i = +k
        assertThat(Quaternion64.I.divide(Quaternion64.J)).isEqualTo(Quaternion64.K.negate());
        assertThat(Quaternion64.I.divideLeft(Quaternion64.J)).isEqualTo(Quaternion64.K);
        assertThatThrownBy(() -> Quaternion64.ONE.divide(Quaternion64.ZERO))
            .isInstanceOf(NonInvertibleElementException.class);
        assertThatThrownBy(() -> Quaternion64.ONE.divideLeft(Quaternion64.ZERO))
            .isInstanceOf(NonInvertibleElementException.class);
    }

    @Test
    public void testApproxEqualsEqualityHashCodeFormattingAndNullGuards() {
        Quaternion64 q = Quaternion64.of(1.0, -2.0, 3.0, -4.0);
        assertThat(q.approxEquals(Quaternion64.of(1.0, -2.0, 3.0, -4.0 + 1e-12))).isTrue();
        assertThat(q.approxEquals(Quaternion64.of(1.0, -2.0, 3.0, -4.0 + 1e-5))).isFalse();
        assertThat(ScopedValue.where(NumericContext.EPSILON, 1e-4)
            .call(() -> q.approxEquals(Quaternion64.of(1.0, -2.0, 3.0, -4.0 + 1e-5)))).isTrue();

        assertThat(q.approxEquals(q, 0.0)).isTrue();
        assertThat(q.approxEquals(Quaternion64.ONE, 0.1)).isFalse();
        assertThatThrownBy(() -> q.approxEquals(q, Double.NaN))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> q.approxEquals(q, Double.POSITIVE_INFINITY))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> q.approxEquals(q, -0.1))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(Quaternion64.of(1.0, -0.0, -0.0, 0.0)).isEqualTo(Quaternion64.ONE);
        assertThat(Quaternion64.of(1.0, -0.0, -0.0, 0.0).hashCode()).isEqualTo(Quaternion64.ONE.hashCode());
        assertThat(Quaternion64.ONE).isNotEqualTo(Quaternion64.I);
        assertThat(Quaternion64.ONE).isNotEqualTo(null);
        assertThat(Quaternion64.ONE).isNotEqualTo("1.0");

        assertThat(Quaternion64.ZERO.toString()).isEqualTo("0.0");
        assertThat(Quaternion64.scalar(2.5).toString()).isEqualTo("2.5");
        assertThat(Quaternion64.I.toString()).isEqualTo("i");
        assertThat(Quaternion64.I.negate().toString()).isEqualTo("-i");
        assertThat(Quaternion64.pure(-2.5, 1.0, -1.0).toString()).isEqualTo("-2.5i + j - k");
        assertThat(Quaternion64.pure(2.5, 0.0, 3.0).toString()).isEqualTo("2.5i + 3.0k");
        assertThat(Quaternion64.of(1.0, 1.0, -1.0, 1.0).toString()).isEqualTo("1.0 + i - j + k");
        assertThat(q.toString()).isEqualTo("1.0 - 2.0i + 3.0j - 4.0k");

        assertThatThrownBy(() -> q.add(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> q.subtract(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> q.multiply(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> q.divide(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> q.divideLeft(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> q.dot(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> q.slerp(null, 0.5)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> q.approxEquals(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> q.approxEquals(null, 1e-6)).isInstanceOf(NullPointerException.class);
    }
}
