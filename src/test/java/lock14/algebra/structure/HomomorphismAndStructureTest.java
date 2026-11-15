package lock14.algebra.structure;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lock14.algebra.element.DivisionRingElement;
import lock14.algebra.element.ModuleElement;
import lock14.algebra.exceptions.AlgebraException;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.group.CyclicGroup;
import lock14.algebra.group.Permutation;
import lock14.algebra.group.SymmetricGroup;
import lock14.algebra.laws.ModuleLaws;
import lock14.algebra.numbers.Integers;
import lock14.algebra.numbers.Quaternion;
import lock14.algebra.numbers.QuaternionDivisionRing;
import lock14.algebra.numbers.Rational;
import lock14.algebra.numbers.Rationals;
import lock14.algebra.quotient.QuotientRing;
import lock14.algebra.quotient.Residue;
import lock14.algebra.quotient.ZMod;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HomomorphismAndStructureTest extends ModuleLaws<BigInteger, BigInteger> {

    private final LeftModule<BigInteger, BigInteger> zModule = new LeftModule<>() {
        @Override
        public AbelianGroup<BigInteger> moduleGroup() {
            return Integers.RING.asAdditiveGroup();
        }

        @Override
        public Ring<BigInteger> scalarRing() {
            return Integers.RING;
        }

        @Override
        public BigInteger scale(BigInteger scalar, BigInteger element) {
            return scalar.multiply(element);
        }
    };

    @Override
    protected LeftModule<BigInteger, BigInteger> module() {
        return zModule;
    }

    @Provide
    Arbitrary<BigInteger> vectors() {
        return Arbitraries.bigIntegers().between(BigInteger.valueOf(-200), BigInteger.valueOf(200));
    }

    @Provide
    Arbitrary<BigInteger> scalars() {
        return Arbitraries.bigIntegers().between(BigInteger.valueOf(-200), BigInteger.valueOf(200));
    }

    @Test
    public void testMagmaSemigroupAndMonoidDefaultOperations() {
        Magma<Integer> subMagma = (a, b) -> a - b;
        assertThat(subMagma.asBinaryOperator().apply(10, 3)).isEqualTo(7);

        Semigroup<String> concatSemigroup = String::concat;
        assertThat(concatSemigroup.repeat("ab", 1)).isEqualTo("ab");
        assertThat(concatSemigroup.repeat("ab", 5)).isEqualTo("ababababab");
        assertThatThrownBy(() -> concatSemigroup.repeat("ab", 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> concatSemigroup.repeat("ab", -2))
            .isInstanceOf(IllegalArgumentException.class);

        Monoid<BigInteger> multMonoid = Integers.RING.asMultiplicativeMonoid();
        assertThat(multMonoid.identity()).isEqualTo(BigInteger.ONE);
        assertThat(multMonoid.isIdentity(BigInteger.ONE)).isTrue();
        assertThat(multMonoid.isIdentity(BigInteger.TWO)).isFalse();

        assertThat(multMonoid.power(BigInteger.TWO, 0)).isEqualTo(BigInteger.ONE);
        assertThat(multMonoid.power(BigInteger.TWO, 5)).isEqualTo(BigInteger.valueOf(32));
        assertThatThrownBy(() -> multMonoid.power(BigInteger.TWO, -1))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(multMonoid.power(BigInteger.valueOf(3), BigInteger.ZERO)).isEqualTo(BigInteger.ONE);
        assertThat(multMonoid.power(BigInteger.valueOf(3), BigInteger.valueOf(5)))
            .isEqualTo(BigInteger.valueOf(243));
        assertThatThrownBy(() -> multMonoid.power(BigInteger.TWO, BigInteger.valueOf(-1)))
            .isInstanceOf(IllegalArgumentException.class);

        Monoid<BigInteger> addMonoid = new Monoid<>() {
            @Override
            public BigInteger identity() {
                return BigInteger.ZERO;
            }

            @Override
            public BigInteger combine(BigInteger left, BigInteger right) {
                return left.add(right);
            }
        };
        List<BigInteger> nums = IntStream.rangeClosed(1, 64).mapToObj(BigInteger::valueOf).toList();
        assertThat(nums.parallelStream().collect(addMonoid.collector()))
            .isEqualTo(BigInteger.valueOf(2080));
        assertThat(combineViaCollector(addMonoid.collector(), BigInteger.valueOf(7), BigInteger.valueOf(11)))
            .isEqualTo(BigInteger.valueOf(18));

        assertThat(Stream.<BigInteger>empty().gather(addMonoid.scanGatherer()).toList()).isEmpty();
        assertThat(Stream.of(BigInteger.ONE, BigInteger.TWO, BigInteger.valueOf(3))
            .gather(addMonoid.scanGatherer())
            .toList())
            .containsExactly(BigInteger.ONE, BigInteger.valueOf(3), BigInteger.valueOf(6));
    }

    @Test
    public void testGroupFiniteGroupRingEuclideanAndLeftModuleDefaults() {
        CyclicGroup c2 = new CyclicGroup(2);
        assertThat(c2.power(1, Long.MIN_VALUE)).isEqualTo(0);
        assertThat(c2.power(1, 0L)).isEqualTo(0);
        assertThat(c2.power(1, -3L)).isEqualTo(1);
        assertThat(c2.power(1, BigInteger.ZERO)).isEqualTo(0);
        assertThat(c2.power(1, BigInteger.valueOf(-3))).isEqualTo(1);
        assertThat(c2.power(1, BigInteger.valueOf(4))).isEqualTo(0);

        SymmetricGroup s3 = new SymmetricGroup(3);
        Permutation swap01 = Permutation.transposition(3, 0, 1);
        Permutation swap12 = Permutation.transposition(3, 1, 2);
        assertThat(s3.conjugate(swap01, swap12)).isEqualTo(Permutation.transposition(3, 0, 2));
        assertThat(s3.commutator(swap01, swap12)).isNotEqualTo(s3.identity());
        assertThat(c2.commutator(1, 1)).isEqualTo(c2.identity());

        FiniteGroup<Integer> unclosedGroup = new FiniteGroup<>() {
            @Override
            public Set<Integer> elements() {
                return Set.of(0, 1);
            }

            @Override
            public Integer identity() {
                return 0;
            }

            @Override
            public Integer combine(Integer left, Integer right) {
                return left + right;
            }

            @Override
            public Integer inverse(Integer element) {
                return -element;
            }
        };
        assertThatThrownBy(() -> unclosedGroup.elementOrder(1))
            .isInstanceOf(IllegalStateException.class);

        QuotientRing<BigInteger> z6 = ZMod.ring(6);
        Residue<BigInteger> twoMod6 = ZMod.element(z6, 2);
        assertThat(z6.multiplyByScalar(twoMod6, 0L)).isEqualTo(z6.zero());
        assertThat(z6.multiplyByScalar(z6.zero(), 5L)).isEqualTo(z6.zero());
        assertThat(z6.multiplyByScalar(twoMod6, Long.MIN_VALUE)).isEqualTo(ZMod.element(z6, 2));
        assertThat(z6.multiplyByScalar(twoMod6, -2L)).isEqualTo(ZMod.element(z6, 2));
        assertThat(z6.multiplyByScalar(twoMod6, 5L)).isEqualTo(ZMod.element(z6, 4));

        assertThat(z6.power(twoMod6, 0)).isEqualTo(z6.one());
        assertThat(z6.power(twoMod6, 3)).isEqualTo(ZMod.element(z6, 2));
        assertThatThrownBy(() -> z6.power(twoMod6, -1))
            .isInstanceOf(IllegalArgumentException.class);

        AbelianGroup<Residue<BigInteger>> z6Add = z6.asAdditiveGroup();
        assertThat(z6Add.identity()).isEqualTo(z6.zero());
        assertThat(z6Add.combine(twoMod6, twoMod6)).isEqualTo(ZMod.element(z6, 4));
        assertThat(z6Add.inverse(twoMod6)).isEqualTo(ZMod.element(z6, 4));

        EuclideanDomain<Rational> unnormalizedRationals = new EuclideanDomain<>() {
            @Override
            public Rational zero() {
                return Rational.ZERO;
            }

            @Override
            public Rational one() {
                return Rational.ONE;
            }

            @Override
            public Rational add(Rational a, Rational b) {
                return a.add(b);
            }

            @Override
            public Rational negate(Rational a) {
                return a.negate();
            }

            @Override
            public Rational multiply(Rational a, Rational b) {
                return a.multiply(b);
            }

            @Override
            public boolean isUnit(Rational element) {
                return !element.isZero();
            }

            @Override
            public DivisionResult<Rational> divideAndRemainder(Rational a, Rational b) {
                return new DivisionResult<>(a.divide(b), Rational.ZERO);
            }
        };
        assertThat(unnormalizedRationals.normalize(Rational.TWO)).isEqualTo(Rational.TWO);
        assertThat(unnormalizedRationals.extendedGcd(Rational.ZERO, Rational.ZERO))
            .isEqualTo(new EuclideanDomain.ExtendedGcdResult<>(Rational.ZERO, Rational.ZERO, Rational.ZERO));

        assertThat(zModule.subtract(BigInteger.TEN, BigInteger.valueOf(4)))
            .isEqualTo(BigInteger.valueOf(6));
    }

    @Test
    public void testHomomorphismsElementsAndExceptions() {
        CyclicGroup c3 = new CyclicGroup(3);
        GroupHomomorphism<Integer, Integer> badIdentity = x -> 1;
        GroupHomomorphism<Integer, Integer> badOperation = x -> x == 0 ? 0 : 1;
        assertThat(badIdentity.isValidHomomorphism(c3, c3)).isFalse();
        assertThat(badOperation.isValidHomomorphism(c3, c3)).isFalse();

        CyclicGroup c6 = new CyclicGroup(6);
        GroupHomomorphism<Integer, Integer> mod3GroupHom = x -> x % 3;
        assertThat(mod3GroupHom.isValidHomomorphism(c6, c3)).isTrue();
        assertThat(mod3GroupHom.kernelElements(c6, c3)).containsExactlyInAnyOrder(0, 3);
        assertThat(mod3GroupHom.imageElements(c6)).containsExactlyInAnyOrder(0, 1, 2);

        QuotientRing<BigInteger> z6 = ZMod.ring(6);
        QuotientRing<BigInteger> z3 = ZMod.ring(3);
        RingHomomorphism<Long, Residue<BigInteger>> char6 = RingHomomorphism.fromIntegers(z6);
        RingHomomorphism<Residue<BigInteger>, Residue<BigInteger>> mod3RingHom =
            r -> z3.of(r.representative());
        RingHomomorphism<Long, Residue<BigInteger>> composed = char6.andThen(mod3RingHom);
        assertThat(composed.apply(5L)).isEqualTo(ZMod.element(z3, 2));

        QuaternionDivisionRing<Rational> hq = QuaternionDivisionRing.overRationals();
        record QuatElement(Quaternion<Rational> q) implements DivisionRingElement<QuatElement> {
            @Override
            public QuatElement add(QuatElement other) {
                return new QuatElement(q.add(other.q));
            }

            @Override
            public QuatElement negate() {
                return new QuatElement(q.negate());
            }

            @Override
            public QuatElement multiply(QuatElement other) {
                return new QuatElement(q.multiply(other.q));
            }

            @Override
            public QuatElement reciprocal() {
                return new QuatElement(q.reciprocal(Rationals.FIELD));
            }

            @Override
            public Optional<QuatElement> tryReciprocal() {
                return q.tryReciprocal(Rationals.FIELD).map(QuatElement::new);
            }
        }

        QuatElement qi = new QuatElement(hq.i());
        QuatElement qj = new QuatElement(hq.j());
        assertThat(qi.subtract(qj)).isEqualTo(new QuatElement(hq.i().subtract(hq.j())));
        assertThat(qi.divide(qj).multiply(qj)).isEqualTo(qi);
        assertThat(qj.multiply(qi.divideLeft(qj))).isEqualTo(qi);
        assertThat(qi.divide(qj)).isNotEqualTo(qi.divideLeft(qj));

        record IntModElem(BigInteger v) implements ModuleElement<IntModElem, BigInteger> {
            @Override
            public IntModElem add(IntModElem other) {
                return new IntModElem(v.add(other.v));
            }

            @Override
            public IntModElem negate() {
                return new IntModElem(v.negate());
            }

            @Override
            public IntModElem scale(BigInteger scalar) {
                return new IntModElem(scalar.multiply(v));
            }
        }

        IntModElem u = new IntModElem(BigInteger.TEN);
        IntModElem v = new IntModElem(BigInteger.valueOf(3));
        assertThat(u.subtract(v)).isEqualTo(new IntModElem(BigInteger.valueOf(7)));

        AlgebraException algEx = new AlgebraException("domain violation");
        assertThat(algEx).hasMessage("domain violation");
        NonInvertibleElementException nonInvObj = new NonInvertibleElementException(0);
        assertThat(nonInvObj).hasMessageContaining("0");
        IncompatibleAlgebraException incompEx = new IncompatibleAlgebraException("mismatch");
        assertThat(incompEx).hasMessage("mismatch");
        DimensionMismatchException dimMsgEx = new DimensionMismatchException("bad shape");
        assertThat(dimMsgEx).hasMessage("bad shape");
        DimensionMismatchException dimGridEx = new DimensionMismatchException(2, 3, 4, 5);
        assertThat(dimGridEx).hasMessageContaining("2x3").hasMessageContaining("4x5");
    }

    private static <T, A> T combineViaCollector(Collector<T, A, T> collector, T left, T right) {
        A b1 = collector.supplier().get();
        collector.accumulator().accept(b1, left);
        A b2 = collector.supplier().get();
        collector.accumulator().accept(b2, right);
        A merged = collector.combiner().apply(b1, b2);
        return collector.finisher().apply(merged);
    }
}
