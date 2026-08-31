package lock14.algebra.linear;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;
import lock14.algebra.exceptions.DimensionMismatchException;

/**
 * A High-Performance 2D Matrix backed by Foreign Function & Memory (FFM) API off-heap memory.
 */
public final class NativeDoubleMatrix implements AutoCloseable {

    private final int rows;
    private final int cols;
    private final Arena arena;
    private final MemorySegment segment;

    public NativeDoubleMatrix(int rows, int cols) {
        this(rows, cols, Arena.ofConfined());
    }

    public NativeDoubleMatrix(int rows, int cols, Arena arena) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive: " + rows + "x" + cols);
        }
        this.rows = rows;
        this.cols = cols;
        this.arena = Objects.requireNonNull(arena, "arena cannot be null");
        this.segment = arena.allocate(ValueLayout.JAVA_DOUBLE, (long) rows * cols);
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public double get(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) {
            throw new IndexOutOfBoundsException("Index (" + r + ", " + c + ") out of bounds for " + rows + "x" + cols);
        }
        return segment.getAtIndex(ValueLayout.JAVA_DOUBLE, (long) r * cols + c);
    }

    public void set(int r, int c, double val) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) {
            throw new IndexOutOfBoundsException("Index (" + r + ", " + c + ") out of bounds for " + rows + "x" + cols);
        }
        segment.setAtIndex(ValueLayout.JAVA_DOUBLE, (long) r * cols + c, val);
    }

    public NativeDoubleMatrix add(NativeDoubleMatrix other) {
        if (this.rows != other.rows || this.cols != other.cols) {
            throw new DimensionMismatchException(this.rows, this.cols, other.rows, other.cols);
        }
        NativeDoubleMatrix result = new NativeDoubleMatrix(this.rows, this.cols, this.arena);
        long len = (long) rows * cols;
        for (long i = 0; i < len; i++) {
            double a = this.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, i);
            double b = other.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, i);
            result.segment.setAtIndex(ValueLayout.JAVA_DOUBLE, i, a + b);
        }
        return result;
    }

    public NativeDoubleMatrix multiply(NativeDoubleMatrix other) {
        if (this.cols != other.rows) {
            throw new DimensionMismatchException("Cannot multiply " + rows + "x" + cols + " with " + other.rows + "x" + other.cols);
        }
        NativeDoubleMatrix result = new NativeDoubleMatrix(this.rows, other.cols, this.arena);
        int resRows = this.rows;
        int resCols = other.cols;
        int inner = this.cols;

        // Cache-friendly i-k-j loop order
        for (int i = 0; i < resRows; i++) {
            long outRowOffset = (long) i * resCols;
            long thisRowOffset = (long) i * inner;
            for (int k = 0; k < inner; k++) {
                double a = this.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, thisRowOffset + k);
                if (a == 0.0) continue;
                long otherRowOffset = (long) k * resCols;
                for (int j = 0; j < resCols; j++) {
                    double b = other.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, otherRowOffset + j);
                    if (b == 0.0) continue;
                    long idx = outRowOffset + j;
                    double current = result.segment.getAtIndex(ValueLayout.JAVA_DOUBLE, idx);
                    result.segment.setAtIndex(ValueLayout.JAVA_DOUBLE, idx, current + a * b);
                }
            }
        }
        return result;
    }

    @Override
    public void close() {
        arena.close();
    }
}
