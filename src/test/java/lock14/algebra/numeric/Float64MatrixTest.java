package lock14.algebra.numeric;

import java.lang.foreign.Arena;
import java.lang.module.ModuleDescriptor;
import java.util.Set;
import java.util.stream.Collectors;
import lock14.algebra.exceptions.NonInvertibleElementException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class Float64MatrixTest {

    @Test
    public void testFfmArenaLifecycleMultiplyAndTranspose() {
        try (Float64Matrix a = Float64Matrix.of(new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}});
             Float64Matrix b = a.transpose();
             Float64Matrix prod = a.multiply(b)) {

            assertThat(prod.rows()).isEqualTo(2);
            assertThat(prod.cols()).isEqualTo(2);
            assertThat(prod.get(0, 0)).isEqualTo(14.0);
            assertThat(prod.get(0, 1)).isEqualTo(32.0);
            assertThat(prod.get(1, 0)).isEqualTo(32.0);
            assertThat(prod.get(1, 1)).isEqualTo(77.0);
        }

        Float64Matrix closed = Float64Matrix.identity(2);
        closed.close();
        assertThat(closed.isOpen()).isFalse();
        assertThatThrownBy(() -> closed.get(0, 0)).isInstanceOf(IllegalStateException.class);

        // External Arena ownership is respected (matrix.close() does not close caller's Arena)
        try (Arena shared = Arena.ofConfined()) {
            Float64Matrix ext = new Float64Matrix(2, 2, shared);
            ext.set(0, 0, 7.0);
            ext.close();
            assertThat(shared.scope().isAlive()).isTrue();
            assertThat(ext.get(0, 0)).isEqualTo(7.0);
        }

        // Overflowing byte size fails cleanly without leaking native memory
        assertThatThrownBy(() -> new Float64Matrix(Integer.MAX_VALUE, Integer.MAX_VALUE))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testPartialPivotingWilkinsonMatrixSolveInverseAndScopedSingularity() {
        // Wilkinson matrix [[1e-20, 1], [1, 1]]: fails catastrophically without partial pivoting
        try (Float64Matrix wilkinson = Float64Matrix.of(new double[][]{{1e-20, 1.0}, {1.0, 1.0}});
             Float64Matrix inv = wilkinson.inverse();
             Float64Matrix id = Float64Matrix.identity(2);
             Float64Matrix check = wilkinson.multiply(inv)) {

            assertThat(wilkinson.determinant()).isCloseTo(-1.0, within(1e-12));
            assertThat(inv.get(0, 0)).isCloseTo(-1.0, within(1e-12));
            assertThat(check.approxEquals(id, 1e-12)).isTrue();

            double[] x = wilkinson.solve(new double[]{1.0, 2.0});
            assertThat(x[0]).isCloseTo(1.0, within(1e-12));
            assertThat(x[1]).isCloseTo(1.0, within(1e-12));
        }

        // Near-singular matrix with second pivot 1e-8: invertible by default, singular when EPSILON = 1e-6!
        try (Float64Matrix nearSingular = Float64Matrix.of(new double[][]{{1.0, 0.0}, {0.0, 1e-8}})) {
            assertThat(nearSingular.solve(new double[]{2.0, 1e-8})[1]).isCloseTo(1.0, within(1e-9));

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
