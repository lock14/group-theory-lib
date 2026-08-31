package lock14.algebra.numbers;

import java.util.Objects;
import lock14.algebra.element.RingElement;

/**
 * An immutable Hamilton Quaternion (w + xi + yj + zk) over {@code double}.
 * Quaternions form a non-commutative division ring (skew-field) H.
 */
public record Quaternion(double w, double x, double y, double z) implements RingElement<Quaternion> {

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
        return Math.sqrt(normSq());
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
        double d = normSq();
        if (d == 0.0) {
            throw new ArithmeticException("Cannot compute reciprocal of zero quaternion");
        }
        return new Quaternion(w / d, -x / d, -y / d, -z / d);
    }

    public Quaternion divide(Quaternion o) {
        Objects.requireNonNull(o, "other quaternion cannot be null");
        return this.multiply(o.reciprocal());
    }

    @Override
    public String toString() {
        return String.format("%.4f + %.4fi + %.4fj + %.4fk", w, x, y, z);
    }
}
