package lock14.algebra.numbers;

import java.util.Objects;
import lock14.algebra.element.RingElement;
import lock14.algebra.structure.Ring;

/**
 * An immutable Hamilton Quaternion (w + xi + yj + zk) over {@code double}.
 * Quaternions form a non-commutative division ring (skew-field) H.
 */
public record Quaternion(double w, double x, double y, double z) implements RingElement<Quaternion> {

    public Quaternion {
        w = (w == 0.0) ? 0.0 : w;
        x = (x == 0.0) ? 0.0 : x;
        y = (y == 0.0) ? 0.0 : y;
        z = (z == 0.0) ? 0.0 : z;
    }

    public static final Quaternion ZERO = new Quaternion(0.0, 0.0, 0.0, 0.0);
    public static final Quaternion ONE = new Quaternion(1.0, 0.0, 0.0, 0.0);
    public static final Quaternion I = new Quaternion(0.0, 1.0, 0.0, 0.0);
    public static final Quaternion J = new Quaternion(0.0, 0.0, 1.0, 0.0);
    public static final Quaternion K = new Quaternion(0.0, 0.0, 0.0, 1.0);

    public static Quaternion of(double w, double x, double y, double z) {
        return new Quaternion(w, x, y, z);
    }

    public static Quaternion scalar(double w) {
        return new Quaternion(w, 0.0, 0.0, 0.0);
    }

    public static Quaternion vector(double x, double y, double z) {
        return new Quaternion(0.0, x, y, z);
    }

    public static Ring<Quaternion> ring() {
        return QuaternionRing.INSTANCE;
    }

    public boolean isZero() {
        return w == 0.0 && x == 0.0 && y == 0.0 && z == 0.0;
    }

    public boolean isOne() {
        return w == 1.0 && x == 0.0 && y == 0.0 && z == 0.0;
    }

    public double normSq() {
        return w * w + x * x + y * y + z * z;
    }

    public double abs() {
        if (Double.isInfinite(w) || Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z)) {
            return Double.POSITIVE_INFINITY;
        }
        double max = maxAbsComponent();
        if (Double.isNaN(max) || max == 0.0) {
            return max;
        }
        double nw = w / max;
        double nx = x / max;
        double ny = y / max;
        double nz = z / max;
        return max * Math.sqrt(nw * nw + nx * nx + ny * ny + nz * nz);
    }

    private double maxAbsComponent() {
        return Math.max(Math.max(Math.abs(w), Math.abs(x)), Math.max(Math.abs(y), Math.abs(z)));
    }

    public Quaternion conjugate() {
        return new Quaternion(
            w,
            x == 0.0 ? 0.0 : -x,
            y == 0.0 ? 0.0 : -y,
            z == 0.0 ? 0.0 : -z
        );
    }

    public Quaternion scale(double s) {
        return new Quaternion(w * s, x * s, y * s, z * s);
    }

    public double dot(Quaternion o) {
        return w * o.w + x * o.x + y * o.y + z * o.z;
    }

    @Override
    public Quaternion add(Quaternion o) {
        Objects.requireNonNull(o, "other quaternion cannot be null");
        return new Quaternion(this.w + o.w, this.x + o.x, this.y + o.y, this.z + o.z);
    }

    @Override
    public Quaternion negate() {
        return new Quaternion(
            w == 0.0 ? 0.0 : -w,
            x == 0.0 ? 0.0 : -x,
            y == 0.0 ? 0.0 : -y,
            z == 0.0 ? 0.0 : -z
        );
    }

    @Override
    public Quaternion subtract(Quaternion o) {
        Objects.requireNonNull(o, "other quaternion cannot be null");
        return new Quaternion(this.w - o.w, this.x - o.x, this.y - o.y, this.z - o.z);
    }

    @Override
    public Quaternion multiply(Quaternion o) {
        Objects.requireNonNull(o, "other quaternion cannot be null");
        double newW = this.w * o.w - this.x * o.x - this.y * o.y - this.z * o.z;
        double newX = this.w * o.x + this.x * o.w + this.y * o.z - this.z * o.y;
        double newY = this.w * o.y - this.x * o.z + this.y * o.w + this.z * o.x;
        double newZ = this.w * o.z + this.x * o.y - this.y * o.x + this.z * o.w;
        return new Quaternion(newW, newX, newY, newZ);
    }

    public Quaternion reciprocal() {
        if (isZero()) {
            throw new ArithmeticException("Cannot compute reciprocal of zero quaternion");
        }
        double max = maxAbsComponent();
        if (Double.isNaN(max)) {
            return new Quaternion(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        }
        if (Double.isInfinite(max)) {
            return ZERO;
        }
        double nw = w / max;
        double nx = x / max;
        double ny = y / max;
        double nz = z / max;
        double scaledNormSq = nw * nw + nx * nx + ny * ny + nz * nz;
        return new Quaternion(
            (nw / scaledNormSq) / max,
            (-nx / scaledNormSq) / max,
            (-ny / scaledNormSq) / max,
            (-nz / scaledNormSq) / max
        );
    }

    public Quaternion divide(Quaternion o) {
        Objects.requireNonNull(o, "other quaternion cannot be null");
        return this.multiply(o.reciprocal());
    }

    @Override
    public String toString() {
        if (isZero()) {
            return "0.0";
        }
        StringBuilder sb = new StringBuilder();
        if (w != 0.0) {
            sb.append(w);
        }
        appendImaginaryTerm(sb, x, "i");
        appendImaginaryTerm(sb, y, "j");
        appendImaginaryTerm(sb, z, "k");
        return sb.toString();
    }

    private static void appendImaginaryTerm(StringBuilder sb, double coeff, String basis) {
        if (coeff == 0.0) {
            return;
        }
        if (sb.isEmpty()) {
            if (coeff == 1.0) {
                sb.append(basis);
            } else if (coeff == -1.0) {
                sb.append('-').append(basis);
            } else {
                sb.append(coeff).append(basis);
            }
        } else if (coeff < 0.0) {
            double abs = Math.abs(coeff);
            sb.append(" - ");
            if (abs != 1.0) {
                sb.append(abs);
            }
            sb.append(basis);
        } else {
            sb.append(" + ");
            if (coeff != 1.0) {
                sb.append(coeff);
            }
            sb.append(basis);
        }
    }
}
