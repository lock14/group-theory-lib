package lock14.algebra.numeric;

import java.lang.foreign.Arena;
import java.lang.module.ModuleDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lock14.algebra.exceptions.DimensionMismatchException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class Float64MatrixTest {

    @Test
    public void testSealedAdtShapeExclusivityAndFfmArenaLifecycle() {
        List<String> baseMethods = Arrays.stream(Float64Matrix.class.getDeclaredMethods())
            .map(Method::getName)
            .toList();
        List<String> rectMethods = Arrays.stream(Float64RectangularMatrix.class.getDeclaredMethods())
            .map(Method::getName)
            .toList();
        assertThat(baseMethods).doesNotContain("trace", "determinant", "solve", "inverse", "tryInverse");
        assertThat(rectMethods).doesNotContain("trace", "determinant", "solve", "inverse", "tryInverse");

        try (Float64Matrix a = Float64Matrix.of(new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}});
             Float64Matrix b = a.transpose();
             Float64Matrix prod = a.multiply(b)) {

            assertThat(a).isInstanceOf(Float64RectangularMatrix.class);
            assertThat(a.isSquare()).isFalse();
            assertThat(prod.isSquare()).isTrue();
            assertThat(classifyMatrix(a)).isEqualTo("Rectangular 2x3");
            assertThat(classifyMatrix(prod)).isEqualTo("Square 2x2");
            assertThat(prod).isInstanceOfSatisfying(Float64SquareMatrix.class, sq -> {
                assertThat(sq.get(0, 0)).isEqualTo(14.0);
                assertThat(sq.get(0, 1)).isEqualTo(32.0);
                assertThat(sq.get(1, 0)).isEqualTo(32.0);
                assertThat(sq.get(1, 1)).isEqualTo(77.0);
                assertThat(sq.trace()).isEqualTo(91.0);
            });
        }

        Float64SquareMatrix closed = Float64Matrix.identity(2);
        assertThat(closed.isOpen()).isTrue();
        closed.close();
        closed.close();
        assertThat(closed.isOpen()).isFalse();
        assertThatThrownBy(() -> closed.get(0, 0)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> closed.set(0, 0, 1.0)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(closed::toArray2D).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(closed::trace).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(closed::determinant).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> closed.solve(new double[]{1.0, 2.0})).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(closed::inverse).isInstanceOf(IllegalStateException.class);

        Float64SquareMatrix postArenaRef;
        // External Arena ownership is respected (matrix.close() does not close caller's Arena)
        try (Arena shared = Arena.ofConfined()) {
            Float64SquareMatrix ext = new Float64SquareMatrix(2, shared);
            ext.set(0, 0, 7.0);
            ext.close();
            assertThat(shared.scope().isAlive()).isTrue();
            assertThat(ext.isOpen()).isTrue();
            assertThat(ext.get(0, 0)).isEqualTo(7.0);
            postArenaRef = ext;
        }
        assertThat(postArenaRef.isOpen()).isFalse();
        assertThatThrownBy(() -> postArenaRef.get(0, 0)).isInstanceOf(IllegalStateException.class);

        // Square dimensions rejected by Float64RectangularMatrix; overflowing byte size fails cleanly
        assertThatThrownBy(() -> Float64RectangularMatrix.zeros(2, 2))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Float64SquareMatrix(Integer.MAX_VALUE))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static String classifyMatrix(Float64Matrix matrix) {
        return switch (matrix) {
            case Float64SquareMatrix sq -> "Square " + sq.dimension() + "x" + sq.dimension();
            case Float64RectangularMatrix rect -> "Rectangular " + rect.rows() + "x" + rect.cols();
        };
    }

    @Test
    public void testFactoryOverloadsAndValidationAcrossHierarchy() {
        try (Float64Matrix zSq = Float64Matrix.zeros(2, 2);
             Float64Matrix zRect = Float64Matrix.zeros(2, 3);
             Float64Matrix ofSq2D = Float64Matrix.of(new double[][]{{1.0, -0.0}, {3.0, 4.0}});
             Float64Matrix ofFlatSq = Float64Matrix.of(2, 2, 1.0, -0.0, 3.0, 4.0);
             Float64Matrix ofFlatRect = Float64Matrix.of(2, 3, 1.0, 2.0, 3.0, 4.0, 5.0, -0.0);
             Float64SquareMatrix sqZeros = Float64SquareMatrix.zeros(2);
             Float64SquareMatrix sqFlat = Float64SquareMatrix.of(2, 5.0, -0.0, 7.0, 8.0);
             Float64RectangularMatrix rectCtor = new Float64RectangularMatrix(2, 3);
             Float64RectangularMatrix rectZeros = Float64RectangularMatrix.zeros(3, 2);
             Float64RectangularMatrix rectFlat = Float64RectangularMatrix.of(2, 3, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0)) {

            assertThat(zSq).isInstanceOf(Float64SquareMatrix.class);
            assertThat(zRect).isInstanceOf(Float64RectangularMatrix.class);
            assertThat(ofSq2D).isInstanceOf(Float64SquareMatrix.class);
            assertThat(Double.doubleToRawLongBits(ofSq2D.get(0, 1))).isZero();
            assertThat(ofFlatSq).isInstanceOf(Float64SquareMatrix.class);
            assertThat(Double.doubleToRawLongBits(ofFlatSq.get(0, 1))).isZero();
            assertThat(ofFlatRect).isInstanceOf(Float64RectangularMatrix.class);
            assertThat(Double.doubleToRawLongBits(ofFlatRect.get(1, 2))).isZero();

            assertThat(sqZeros.dimension()).isEqualTo(2);
            assertThat(sqZeros.rows()).isEqualTo(2);
            assertThat(sqZeros.cols()).isEqualTo(2);
            assertThat(sqFlat.get(0, 0)).isEqualTo(5.0);
            assertThat(Double.doubleToRawLongBits(sqFlat.get(0, 1))).isZero();

            assertThat(rectCtor.rows()).isEqualTo(2);
            assertThat(rectCtor.cols()).isEqualTo(3);
            assertThat(rectCtor.isOpen()).isTrue();
            assertThat(rectZeros.rows()).isEqualTo(3);
            assertThat(rectZeros.cols()).isEqualTo(2);
            assertThat(rectFlat.get(1, 2)).isEqualTo(6.0);
        }

        try (Arena shared = Arena.ofConfined()) {
            Float64Matrix extSq = Float64Matrix.zeros(2, 2, shared);
            Float64Matrix extRect = Float64Matrix.zeros(2, 3, shared);
            Float64SquareMatrix extId = Float64Matrix.identity(3, shared);
            Float64SquareMatrix extSqZeros = Float64SquareMatrix.zeros(2, shared);
            Float64SquareMatrix extSqId = Float64SquareMatrix.identity(2, shared);
            Float64RectangularMatrix extRectCtor = new Float64RectangularMatrix(2, 3, shared);
            Float64RectangularMatrix extRectZeros = Float64RectangularMatrix.zeros(3, 2, shared);

            assertThat(extSq).isInstanceOf(Float64SquareMatrix.class);
            assertThat(extRect).isInstanceOf(Float64RectangularMatrix.class);
            assertThat(extId.trace()).isEqualTo(3.0);
            assertThat(extSqZeros.get(0, 0)).isEqualTo(0.0);
            assertThat(extSqId.get(1, 1)).isEqualTo(1.0);
            assertThat(extRectCtor.rows()).isEqualTo(2);
            assertThat(extRectZeros.cols()).isEqualTo(2);

            assertThatThrownBy(() -> Float64Matrix.zeros(0, 2, shared))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Float64Matrix.zeros(2, 0, shared))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Float64SquareMatrix(0, shared))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Float64SquareMatrix.identity(0, shared))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Float64RectangularMatrix(2, 2, shared))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Float64RectangularMatrix(0, 3, shared))
                .isInstanceOf(IllegalArgumentException.class);
        }

        double[][] nullArray2D = null;
        double[] nullRowMajor = null;

        assertThatThrownBy(() -> Float64Matrix.zeros(0, 2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Float64Matrix.zeros(2, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Float64Matrix.zeros(2, 2, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Float64Matrix.of(nullArray2D)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Float64Matrix.of(new double[0][])).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Float64Matrix.of(new double[][]{null})).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Float64Matrix.of(new double[][]{{}})).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Float64Matrix.of(new double[][]{{1.0, 2.0}, null}))
            .isInstanceOf(DimensionMismatchException.class);
        assertThatThrownBy(() -> Float64Matrix.of(new double[][]{{1.0, 2.0}, {3.0}}))
            .isInstanceOf(DimensionMismatchException.class);

        assertThatThrownBy(() -> Float64Matrix.of(0, 2, 1.0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Float64Matrix.of(2, 0, 1.0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Float64Matrix.of(2, 2, nullRowMajor)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Float64Matrix.of(2, 2, 1.0, 2.0, 3.0))
            .isInstanceOf(DimensionMismatchException.class);

        assertThatThrownBy(() -> new Float64SquareMatrix(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Float64SquareMatrix(2, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Float64SquareMatrix.identity(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Float64SquareMatrix.identity(2, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Float64SquareMatrix.of(new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}}))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Float64SquareMatrix.of(0, 1.0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Float64SquareMatrix.of(2, 1.0, 2.0))
            .isInstanceOf(DimensionMismatchException.class);

        assertThatThrownBy(() -> new Float64RectangularMatrix(2, 2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Float64RectangularMatrix(2, 3, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Float64RectangularMatrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}}))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Float64RectangularMatrix.of(2, 2, 1.0, 2.0, 3.0, 4.0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Float64RectangularMatrix.of(2, 3, 1.0, 2.0))
            .isInstanceOf(DimensionMismatchException.class);
    }

    @Test
    public void testElementAccessMutationBoundsAndToArray2D() {
        try (Float64SquareMatrix sq = Float64SquareMatrix.zeros(2);
             Float64RectangularMatrix rect = Float64RectangularMatrix.zeros(2, 3)) {

            sq.set(0, 0, -0.0);
            sq.set(0, 1, 3.5);
            sq.set(1, 0, -2.5);
            sq.set(1, 1, 4.0);
            assertThat(Double.doubleToRawLongBits(sq.get(0, 0))).isZero();

            double[][] sqCopy = sq.toArray2D();
            assertThat(sqCopy).isDeepEqualTo(new double[][]{{0.0, 3.5}, {-2.5, 4.0}});
            sqCopy[0][0] = 99.0;
            assertThat(sq.get(0, 0)).isEqualTo(0.0);

            rect.set(0, 2, -0.0);
            rect.set(1, 1, 9.25);
            assertThat(Double.doubleToRawLongBits(rect.get(0, 2))).isZero();
            assertThat(rect.get(1, 1)).isEqualTo(9.25);
            assertThat(rect.toArray2D()).isDeepEqualTo(new double[][]{{0.0, 0.0, 0.0}, {0.0, 9.25, 0.0}});

            assertThatThrownBy(() -> sq.get(-1, 0)).isInstanceOf(IndexOutOfBoundsException.class);
            assertThatThrownBy(() -> sq.get(2, 0)).isInstanceOf(IndexOutOfBoundsException.class);
            assertThatThrownBy(() -> sq.get(0, -1)).isInstanceOf(IndexOutOfBoundsException.class);
            assertThatThrownBy(() -> sq.get(0, 2)).isInstanceOf(IndexOutOfBoundsException.class);
            assertThatThrownBy(() -> rect.set(-1, 0, 1.0)).isInstanceOf(IndexOutOfBoundsException.class);
            assertThatThrownBy(() -> rect.set(0, 3, 1.0)).isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Test
    public void testSquareAndRectangularArithmeticAndPolymorphicDispatch() {
        try (Float64SquareMatrix sqA = Float64SquareMatrix.of(new double[][]{{1.0, 1.0}, {2.0, 0.0}});
             Float64SquareMatrix sqB = Float64SquareMatrix.of(new double[][]{{2.0, -1.0}, {-2.0, 3.0}});
             Float64SquareMatrix sq3 = Float64SquareMatrix.identity(3);
             Float64RectangularMatrix rectA = Float64RectangularMatrix.of(
                 new double[][]{{1.0, 0.0, -1.0}, {2.0, 3.0, 0.0}});
             Float64RectangularMatrix rectB = Float64RectangularMatrix.of(
                 new double[][]{{-1.0, 2.0, 1.0}, {2.0, -3.0, 4.0}});
             Float64RectangularMatrix rect3x2 = Float64RectangularMatrix.of(
                 new double[][]{{1.0, 2.0}, {3.0, 4.0}, {1.0, 2.0}});
             Float64RectangularMatrix rect3x4 = Float64RectangularMatrix.zeros(3, 4);
             Float64RectangularMatrix rect2x4 = Float64RectangularMatrix.zeros(2, 4);
             Float64SquareMatrix sqSum = sqA.add(sqB);
             Float64SquareMatrix sqDiff = sqA.subtract(sqA);
             Float64SquareMatrix sqScaled = sqA.scale(-2.0);
             Float64SquareMatrix sqProd = sqA.multiply(sqB);
             Float64RectangularMatrix sqTimesRect = sqA.multiply(rectA);
             Float64SquareMatrix sqTransposed = sqA.transpose();
             Float64RectangularMatrix rectSum = rectA.add(rectB);
             Float64RectangularMatrix rectDiff = rectA.subtract(rectB);
             Float64RectangularMatrix rectScaled = rectA.scale(-3.0);
             Float64Matrix rectProdSquare = rectA.multiply(rect3x2);
             Float64Matrix rectProdRect = rectA.multiply(rect3x4);
             Float64Matrix rectTimesSq = rectA.multiply(sq3)) {

            assertThat(sqSum.toArray2D()).isDeepEqualTo(new double[][]{{3.0, 0.0}, {0.0, 3.0}});
            assertThat(Double.doubleToRawLongBits(sqSum.get(0, 1))).isZero();
            assertThat(sqDiff.toArray2D()).isDeepEqualTo(new double[][]{{0.0, 0.0}, {0.0, 0.0}});
            assertThat(sqScaled.toArray2D()).isDeepEqualTo(new double[][]{{-2.0, -2.0}, {-4.0, 0.0}});
            assertThat(Double.doubleToRawLongBits(sqScaled.get(1, 1))).isZero();
            assertThat(sqProd.toArray2D()).isDeepEqualTo(new double[][]{{0.0, 2.0}, {4.0, -2.0}});
            assertThat(Double.doubleToRawLongBits(sqProd.get(0, 0))).isZero();
            assertThat(sqTimesRect.toArray2D()).isDeepEqualTo(new double[][]{{3.0, 3.0, -1.0}, {2.0, 0.0, -2.0}});
            assertThat(sqTransposed.toArray2D()).isDeepEqualTo(new double[][]{{1.0, 2.0}, {1.0, 0.0}});

            double[] sqVec = sqA.multiplyVector(new double[]{2.0, -2.0});
            assertThat(sqVec).containsExactly(0.0, 4.0);
            assertThat(Double.doubleToRawLongBits(sqVec[0])).isZero();

            assertThat(rectSum.toArray2D()).isDeepEqualTo(new double[][]{{0.0, 2.0, 0.0}, {4.0, 0.0, 4.0}});
            assertThat(rectDiff.toArray2D()).isDeepEqualTo(new double[][]{{2.0, -2.0, -2.0}, {0.0, 6.0, -4.0}});
            assertThat(rectScaled.toArray2D()).isDeepEqualTo(new double[][]{{-3.0, 0.0, 3.0}, {-6.0, -9.0, 0.0}});
            assertThat(rectProdSquare).isInstanceOf(Float64SquareMatrix.class);
            assertThat(rectProdSquare.toArray2D()).isDeepEqualTo(new double[][]{{0.0, 0.0}, {11.0, 16.0}});
            assertThat(rectProdRect).isInstanceOf(Float64RectangularMatrix.class);
            assertThat(rectProdRect.rows()).isEqualTo(2);
            assertThat(rectProdRect.cols()).isEqualTo(4);
            assertThat(rectTimesSq).isInstanceOf(Float64RectangularMatrix.class);
            assertThat(rectTimesSq.approxEquals(rectA)).isTrue();

            double[] rectVec = rectA.multiplyVector(new double[]{1.0, 2.0, 1.0});
            assertThat(rectVec).containsExactly(0.0, 8.0);
            assertThat(Double.doubleToRawLongBits(rectVec[0])).isZero();

            // Polymorphic Float64Matrix dispatch on both sealed subtypes
            Float64Matrix polySq = sqA;
            Float64Matrix polyRect = rectA;
            try (Float64Matrix pSqAdd = polySq.add(sqB);
                 Float64Matrix pSqSub = polySq.subtract(sqB);
                 Float64Matrix pSqMulSq = polySq.multiply(sqB);
                 Float64Matrix pSqMulRect = polySq.multiply(rectA);
                 Float64Matrix pRectAdd = polyRect.add(rectB);
                 Float64Matrix pRectSub = polyRect.subtract(rectB)) {

                assertThat(pSqAdd.approxEquals(sqSum)).isTrue();
                assertThat(pSqSub).isInstanceOf(Float64SquareMatrix.class);
                assertThat(pSqMulSq.approxEquals(sqProd)).isTrue();
                assertThat(pSqMulRect.approxEquals(sqTimesRect)).isTrue();
                assertThat(pRectAdd.approxEquals(rectSum)).isTrue();
                assertThat(pRectSub.approxEquals(rectDiff)).isTrue();
            }

            assertThatThrownBy(() -> polySq.add(rectA)).isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> polySq.subtract(rectA)).isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> polyRect.add(sqA)).isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> polyRect.subtract(sqA)).isInstanceOf(DimensionMismatchException.class);

            assertThatThrownBy(() -> sqA.add(sq3)).isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> sqA.subtract(sq3)).isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> sqA.multiply(sq3)).isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> sqA.multiply(rect3x2)).isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> sqA.multiplyVector(new double[]{1.0, 2.0, 3.0}))
                .isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> sqA.multiplyVector(null)).isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> rectA.add(rect3x2)).isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> rectA.add(rect2x4)).isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> rectA.subtract(rect3x2)).isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> rectA.subtract(rect2x4)).isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> rectA.multiply(rectA)).isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> rectA.multiplyVector(new double[]{1.0, 2.0}))
                .isInstanceOf(DimensionMismatchException.class);

            Float64SquareMatrix nullSq = null;
            Float64RectangularMatrix nullRect = null;
            Float64Matrix nullMat = null;
            assertThatThrownBy(() -> sqA.add(nullSq)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> sqA.subtract(nullSq)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> sqA.multiply(nullSq)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> sqA.multiply(nullRect)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> polySq.add(nullMat)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> polySq.subtract(nullMat)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> polySq.multiply(nullMat)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> rectA.add(nullRect)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> rectA.subtract(nullRect)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> polyRect.add(nullMat)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> polyRect.subtract(nullMat)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> rectA.multiply(nullMat)).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    public void testDeterminantTraceSolveInverseAndApproxEqualsEdgeCases() {
        try (Float64SquareMatrix oneByOne = Float64SquareMatrix.of(1, 5.0);
             Float64SquareMatrix oneByOneInv = oneByOne.inverse();
             Float64SquareMatrix traceless = Float64SquareMatrix.of(new double[][]{{2.0, 1.0}, {3.0, -2.0}});
             Float64SquareMatrix m3 = Float64SquareMatrix.of(new double[][]{
                 {2.0, 1.0, 0.0},
                 {0.0, 3.0, 1.0},
                 {4.0, 2.0, 5.0}
             });
             Float64SquareMatrix m3Inv = m3.inverse();
             Float64SquareMatrix id3 = Float64SquareMatrix.identity(3);
             Float64SquareMatrix id3Inv = id3.inverse();
             Float64SquareMatrix check3 = m3.multiply(m3Inv);
             Float64SquareMatrix singular = Float64SquareMatrix.of(new double[][]{{1.0, 2.0}, {2.0, 4.0}});
             Float64SquareMatrix underflow = Float64SquareMatrix.of(new double[][]{{0.0, 1e-200}, {1e-200, 0.0}})) {

            assertThat(oneByOne.trace()).isEqualTo(5.0);
            assertThat(oneByOne.determinant()).isCloseTo(5.0, within(1e-12));
            assertThat(oneByOne.solve(new double[]{15.0})).containsExactly(3.0);
            assertThat(oneByOneInv.get(0, 0)).isCloseTo(0.2, within(1e-12));

            assertThat(Double.doubleToRawLongBits(traceless.trace())).isZero();

            assertThat(m3.determinant()).isCloseTo(30.0, within(1e-12));
            double[] x3 = m3.solve(new double[]{2.0, 1.0, 9.0});
            assertThat(x3[0]).isCloseTo(1.0, within(1e-12));
            assertThat(Double.doubleToRawLongBits(x3[1])).isZero();
            assertThat(x3[2]).isCloseTo(1.0, within(1e-12));
            assertThat(check3.approxEquals(id3, 1e-12)).isTrue();
            assertThat(id3Inv.approxEquals(id3, 1e-12)).isTrue();

            assertThat(Double.doubleToRawLongBits(singular.determinant())).isZero();
            assertThat(singular.tryInverse()).isEmpty();
            assertThatThrownBy(singular::inverse).isInstanceOf(NonInvertibleElementException.class);
            assertThatThrownBy(() -> singular.solve(new double[]{1.0, 2.0}))
                .isInstanceOf(NonInvertibleElementException.class);
            assertThatThrownBy(() -> m3.solve(new double[]{1.0, 2.0}))
                .isInstanceOf(DimensionMismatchException.class);
            assertThatThrownBy(() -> m3.solve(null)).isInstanceOf(NullPointerException.class);

            double underflowDet = ScopedValue.where(NumericContext.EPSILON, 0.0)
                .call(underflow::determinant);
            assertThat(Double.doubleToRawLongBits(underflowDet)).isZero();
        }

        try (Float64RectangularMatrix rBase = Float64RectangularMatrix.of(new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}});
             Float64RectangularMatrix rClose = Float64RectangularMatrix.of(
                 new double[][]{{1.0, 2.0, 3.0 + 1e-6}, {4.0, 5.0, 6.0}});
             Float64RectangularMatrix rNan = Float64RectangularMatrix.of(
                 new double[][]{{1.0, Double.NaN, 3.0}, {4.0, 5.0, 6.0}});
             Float64RectangularMatrix r3x2 = Float64RectangularMatrix.zeros(3, 2);
             Float64RectangularMatrix r2x4 = Float64RectangularMatrix.zeros(2, 4);
             Float64SquareMatrix sq2 = Float64SquareMatrix.identity(2)) {

            assertThat(rBase.approxEquals(rClose)).isFalse();
            assertThat(ScopedValue.where(NumericContext.EPSILON, 1e-5)
                .call(() -> rBase.approxEquals(rClose))).isTrue();
            assertThat(rBase.approxEquals(rNan, 1.0)).isFalse();
            assertThat(rBase.approxEquals(r3x2, 1.0)).isFalse();
            assertThat(rBase.approxEquals(r2x4, 1.0)).isFalse();
            assertThat(sq2.approxEquals(rBase, 1.0)).isFalse();

            assertThatThrownBy(() -> rBase.approxEquals(null)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> rBase.approxEquals(rBase, Double.NaN))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> rBase.approxEquals(rBase, Double.POSITIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> rBase.approxEquals(rBase, -1e-9))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    public void testPartialPivotingWilkinsonMatrixSolveInverseAndScopedSingularity() {
        // Wilkinson matrix [[1e-20, 1], [1, 1]]: fails catastrophically without partial pivoting
        try (Float64SquareMatrix wilkinson = Float64SquareMatrix.of(new double[][]{{1e-20, 1.0}, {1.0, 1.0}});
             Float64SquareMatrix inv = wilkinson.inverse();
             Float64SquareMatrix id = Float64SquareMatrix.identity(2);
             Float64SquareMatrix check = wilkinson.multiply(inv)) {

            assertThat(wilkinson.determinant()).isCloseTo(-1.0, within(1e-12));
            assertThat(inv.get(0, 0)).isCloseTo(-1.0, within(1e-12));
            assertThat(check.approxEquals(id, 1e-12)).isTrue();

            double[] x = wilkinson.solve(new double[]{1.0, 2.0});
            assertThat(x[0]).isCloseTo(1.0, within(1e-12));
            assertThat(x[1]).isCloseTo(1.0, within(1e-12));

            Optional<Float64SquareMatrix> maybeInv = wilkinson.tryInverse();
            assertThat(maybeInv).isPresent();
            try (Float64SquareMatrix presentInv = maybeInv.orElseThrow()) {
                assertThat(presentInv.approxEquals(inv, 1e-12)).isTrue();
            }
        }

        // Near-singular matrix with second pivot 1e-8: invertible by default, singular when EPSILON = 1e-6!
        try (Float64SquareMatrix nearSingular = Float64SquareMatrix.of(new double[][]{{1.0, 0.0}, {0.0, 1e-8}})) {
            assertThat(nearSingular.solve(new double[]{2.0, 1e-8})[1]).isCloseTo(1.0, within(1e-9));

            assertThat(ScopedValue.where(NumericContext.EPSILON, 1e-6)
                .call(nearSingular::tryInverse)).isEmpty();
            assertThatThrownBy(() -> ScopedValue.where(NumericContext.EPSILON, 1e-6)
                .call(() -> nearSingular.solve(new double[]{2.0, 1e-8})))
                .isInstanceOf(NonInvertibleElementException.class);
        }
    }

    @Test
    public void testJpmsNamedModuleExportsAllCleanSlatePackages() {
        Module module = Float64Matrix.class.getModule();
        assertThat(module.isNamed()).isTrue();
        assertThat(module.getName()).isEqualTo("lock14.algebra");

        Set<String> exportedPackages = module.getDescriptor().exports().stream()
            .map(ModuleDescriptor.Exports::source)
            .collect(Collectors.toSet());

        assertThat(exportedPackages).containsExactlyInAnyOrder(
            "lock14.algebra.element",
            "lock14.algebra.exceptions",
            "lock14.algebra.group",
            "lock14.algebra.linear",
            "lock14.algebra.numbers",
            "lock14.algebra.numeric",
            "lock14.algebra.polynomial",
            "lock14.algebra.quotient",
            "lock14.algebra.structure"
        );
    }
}
