package lock14.algebra.numeric;

import java.lang.foreign.Arena;
import java.lang.module.ModuleDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
        closed.close();
        assertThat(closed.isOpen()).isFalse();
        assertThatThrownBy(() -> closed.get(0, 0)).isInstanceOf(IllegalStateException.class);

        // External Arena ownership is respected (matrix.close() does not close caller's Arena)
        try (Arena shared = Arena.ofConfined()) {
            Float64SquareMatrix ext = new Float64SquareMatrix(2, shared);
            ext.set(0, 0, 7.0);
            ext.close();
            assertThat(shared.scope().isAlive()).isTrue();
            assertThat(ext.get(0, 0)).isEqualTo(7.0);
        }

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
