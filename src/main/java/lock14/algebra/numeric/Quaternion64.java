package lock14.algebra.numeric;

import java.util.Objects;
import java.util.Optional;
import lock14.algebra.exceptions.NonInvertibleElementException;

/**
 * IEEE-754 64-bit floating-point Hamilton quaternion {@code w + xi + yj + zk} with overflow-safe
 * scaling, 3D spatial vector rotation, and spherical linear interpolation ({@link #slerp}).
 *
 * <p>For exact non-commutative quaternion algebra (such as Lipschitz integers or {@code H(Q)}),
 * use {@link lock14.algebra.numbers.Quaternion}.</p>
 *
 * @param w the IEEE-754 {@code double} scalar (real) component
 * @param x the IEEE-754 {@code double} {@code i} component
 * @param y the IEEE-754 {@code double} {@code j} component
 * @param z the IEEE-754 {@code double} {@code k} component
 */
public record Quaternion64(double w, double x, double y, double z) {

    /**
     * The additive identity {@code 0 + 0i + 0j + 0k}.
     */
    public static final Quaternion64 ZERO = new Quaternion64(0.0, 0.0, 0.0, 0.0);

    /**
     * The multiplicative identity {@code 1 + 0i + 0j + 0k}.
     */
    public static final Quaternion64 ONE = new Quaternion64(1.0, 0.0, 0.0, 0.0);

    /**
     * The basis quaternion {@code i = 0 + 1i + 0j + 0k}.
     */
    public static final Quaternion64 I = new Quaternion64(0.0, 1.0, 0.0, 0.0);

    /**
     * The basis quaternion {@code j = 0 + 0i + 1j + 0k}.
     */
    public static final Quaternion64 J = new Quaternion64(0.0, 0.0, 1.0, 0.0);

    /**
     * The basis quaternion {@code k = 0 + 0i + 0j + 1k}.
     */
    public static final Quaternion64 K = new Quaternion64(0.0, 0.0, 0.0, 1.0);

    /**
     * Canonicalizes signed zero ({@code -0.0}) components to positive {@code +0.0}.
     *
     * @param w the scalar component
     * @param x the {@code i} component
     * @param y the {@code j} component
     * @param z the {@code k} component
     */
    public Quaternion64 {
        if (w == 0.0) w = 0.0;
        if (x == 0.0) x = 0.0;
        if (y == 0.0) y = 0.0;
        if (z == 0.0) z = 0.0;
    }

    /**
     * Constructs the quaternion {@code w + xi + yj + zk}.
     *
     * @param w the scalar component
     * @param x the {@code i} component
     * @param y the {@code j} component
     * @param z the {@code k} component
     * @return {@code w + xi + yj + zk}
     */
    public static Quaternion64 of(double w, double x, double y, double z) {
        return new Quaternion64(w, x, y, z);
    }

    /**
     * Constructs the scalar quaternion {@code w + 0i + 0j + 0k}.
     *
     * @param w the scalar component
     * @return {@code w + 0i + 0j + 0k}
     */
    public static Quaternion64 scalar(double w) {
        return new Quaternion64(w, 0.0, 0.0, 0.0);
    }

    /**
     * Constructs the pure (vector) quaternion {@code 0 + xi + yj + zk}.
     *
     * @param x the {@code i} component
     * @param y the {@code j} component
     * @param z the {@code k} component
     * @return {@code xi + yj + zk}
     */
    public static Quaternion64 pure(double x, double y, double z) {
        return new Quaternion64(0.0, x, y, z);
    }

    /**
     * Constructs a unit rotation quaternion representing a right-handed rotation of {@code angleRadians}
     * around the 3D axis {@code (ax, ay, az)}.
     *
     * @param ax           x-component of the non-zero rotation axis
     * @param ay           y-component of the non-zero rotation axis
     * @param az           z-component of the non-zero rotation axis
     * @param angleRadians rotation angle in radians
     * @return the unit rotation {@link Quaternion64}
     */
    public static Quaternion64 fromAxisAngle(double ax, double ay, double az, double angleRadians) {
        double axisNorm = Math.hypot(Math.hypot(ax, ay), az);
        if (axisNorm == 0.0 || Double.isNaN(axisNorm)) {
            throw new IllegalArgumentException("Rotation axis must be non-zero");
        }
        double half = 0.5 * angleRadians;
        double sinHalf = Math.sin(half) / axisNorm;
        return new Quaternion64(Math.cos(half), ax * sinHalf, ay * sinHalf, az * sinHalf);
    }

    /**
     * Returns {@code true} if all four components equal {@code 0.0}.
     *
     * @return {@code true} if this quaternion is zero
     */
    public boolean isZero() {
        return w == 0.0 && x == 0.0 && y == 0.0 && z == 0.0;
    }

    /**
     * Computes the unscaled squared norm {@code w^2 + x^2 + y^2 + z^2}.
     *
     * @return {@code w*w + x*x + y*y + z*z}
     */
    public double normSq() {
        return w * w + x * x + y * y + z * z;
    }

    /**
     * Computes the Euclidean norm {@code sqrt(w^2 + x^2 + y^2 + z^2)} scaled by the maximum component
     * to prevent intermediate overflow and underflow.
     *
     * @return the non-negative Euclidean norm {@code |this|}
     */
    public double abs() {
        double max = Math.max(Math.max(Math.abs(w), Math.abs(x)), Math.max(Math.abs(y), Math.abs(z)));
        if (max == 0.0 || Double.isInfinite(max) || Double.isNaN(max)) {
            return max;
        }
        double ws = w / max;
        double xs = x / max;
        double ys = y / max;
        double zs = z / max;
        return max * Math.sqrt(ws * ws + xs * xs + ys * ys + zs * zs);
    }

    /**
     * Returns the Hamilton conjugate {@code w - xi - yj - zk}.
     *
     * @return the conjugate {@link Quaternion64}
     */
    public Quaternion64 conjugate() {
        return new Quaternion64(w, -x, -y, -z);
    }

    /**
     * Scales all four components by {@code factor}.
     *
     * @param factor the real scalar multiplier
     * @return {@code factor * this}
     */
    public Quaternion64 scale(double factor) {
        return new Quaternion64(w * factor, x * factor, y * factor, z * factor);
    }

    /**
     * Computes the componentwise sum {@code this + other}.
     *
     * @param other the quaternion addend
     * @return {@code this + other}
     */
    public Quaternion64 add(Quaternion64 other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Quaternion64(w + other.w, x + other.x, y + other.y, z + other.z);
    }

    /**
     * Computes the additive inverse {@code -this}.
     *
     * @return {@code -w - xi - yj - zk}
     */
    public Quaternion64 negate() {
        return new Quaternion64(-w, -x, -y, -z);
    }

    /**
     * Computes the componentwise difference {@code this - other}.
     *
     * @param other the quaternion subtrahend
     * @return {@code this - other}
     */
    public Quaternion64 subtract(Quaternion64 other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Quaternion64(w - other.w, x - other.x, y - other.y, z - other.z);
    }

    /**
     * Computes the non-commutative Hamilton product {@code this * other}.
     *
     * @param other the right-hand quaternion factor
     * @return {@code this * other}
     */
    public Quaternion64 multiply(Quaternion64 other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new Quaternion64(
            w * other.w - x * other.x - y * other.y - z * other.z,
            w * other.x + x * other.w + y * other.z - z * other.y,
            w * other.y - x * other.z + y * other.w + z * other.x,
            w * other.z + x * other.y - y * other.x + z * other.w
        );
    }

    /**
     * Computes {@code q^-1 = conjugate(q) / |q|^2} using max-component scaling to avoid overflow/underflow.
     *
     * @return the multiplicative inverse {@code this^-1}
     * @throws NonInvertibleElementException if {@link #isZero()} is {@code true}
     */
    public Quaternion64 reciprocal() {
        double max = Math.max(Math.max(Math.abs(w), Math.abs(x)), Math.max(Math.abs(y), Math.abs(z)));
        if (max == 0.0) {
            throw new NonInvertibleElementException("Cannot compute reciprocal of zero Quaternion64");
        }
        if (Double.isInfinite(max)) {
            if (Double.isNaN(w) || Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
                return new Quaternion64(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
            }
            return ZERO;
        }
        double ws = w / max;
        double xs = x / max;
        double ys = y / max;
        double zs = z / max;
        double scaledNormSq = ws * ws + xs * xs + ys * ys + zs * zs;
        double denom = max * scaledNormSq;
        return new Quaternion64(ws / denom, -xs / denom, -ys / denom, -zs / denom);
    }

    /**
     * Attempts to compute the multiplicative inverse of this quaternion.
     *
     * @return an {@link Optional} containing {@code this^-1}, or {@link Optional#empty()} if {@link #isZero()}
     */
    public Optional<Quaternion64> tryReciprocal() {
        return isZero() ? Optional.empty() : Optional.of(reciprocal());
    }

    /**
     * Right division {@code this * other^-1}.
     *
     * @param other the non-zero right divisor
     * @return {@code this * other^-1}
     * @throws NonInvertibleElementException if {@code other.isZero()} is {@code true}
     */
    public Quaternion64 divide(Quaternion64 other) {
        Objects.requireNonNull(other, "other cannot be null");
        return multiply(other.reciprocal());
    }

    /**
     * Left division {@code other^-1 * this}.
     *
     * @param other the non-zero left divisor
     * @return {@code other^-1 * this}
     * @throws NonInvertibleElementException if {@code other.isZero()} is {@code true}
     */
    public Quaternion64 divideLeft(Quaternion64 other) {
        Objects.requireNonNull(other, "other cannot be null");
        return other.reciprocal().multiply(this);
    }

    /**
     * Normalizes this quaternion to unit Euclidean length {@code this / |this|}.
     *
     * @return a unit {@link Quaternion64} in the same direction
     * @throws NonInvertibleElementException if {@link #isZero()} is {@code true}
     */
    public Quaternion64 normalizeUnit() {
        double norm = abs();
        if (norm == 0.0) {
            throw new NonInvertibleElementException("Cannot normalize zero Quaternion64");
        }
        return new Quaternion64(w / norm, x / norm, y / norm, z / norm);
    }

    /**
     * Computes the 4D Euclidean inner product {@code w*other.w + x*other.x + y*other.y + z*other.z}.
     *
     * @param other the right-hand quaternion
     * @return the 4D dot product
     */
    public double dot(Quaternion64 other) {
        Objects.requireNonNull(other, "other cannot be null");
        return w * other.w + x * other.x + y * other.y + z * other.z;
    }

    /**
     * Rotates the 3D vector {@code (vx, vy, vz)} by the spatial rotation represented by {@code q (0, v) q^-1}.
     *
     * @param vx x-component of the 3D vector to rotate
     * @param vy y-component of the 3D vector to rotate
     * @param vz z-component of the 3D vector to rotate
     * @return a 3-element {@code double[]} containing the rotated {@code (x, y, z)} coordinates
     * @throws NonInvertibleElementException if this quaternion is zero
     */
    public double[] rotateVector(double vx, double vy, double vz) {
        Quaternion64 u = normalizeUnit();
        double tx = 2.0 * (u.y * vz - u.z * vy);
        double ty = 2.0 * (u.z * vx - u.x * vz);
        double tz = 2.0 * (u.x * vy - u.y * vx);
        return new double[]{
            vx + u.w * tx + (u.y * tz - u.z * ty),
            vy + u.w * ty + (u.z * tx - u.x * tz),
            vz + u.w * tz + (u.x * ty - u.y * tx)
        };
    }

    /**
     * Alias for {@link #rotateVector(double, double, double)}.
     *
     * @param vx x-component of the 3D vector to rotate
     * @param vy y-component of the 3D vector to rotate
     * @param vz z-component of the 3D vector to rotate
     * @return a 3-element {@code double[]} containing the rotated {@code (x, y, z)} coordinates
     */
    public double[] rotate(double vx, double vy, double vz) {
        return rotateVector(vx, vy, vz);
    }

    /**
     * Spherical linear interpolation between two non-zero quaternions along the shortest great-circle arc.
     *
     * @param other the target non-zero quaternion at {@code t = 1}
     * @param t     interpolation parameter (typically in {@code [0.0, 1.0]})
     * @return the unit interpolated {@link Quaternion64}
     */
    public Quaternion64 slerp(Quaternion64 other, double t) {
        Objects.requireNonNull(other, "other cannot be null");
        Quaternion64 q0 = this.normalizeUnit();
        Quaternion64 q1 = other.normalizeUnit();
        double cosTheta = q0.dot(q1);
        if (cosTheta < 0.0) {
            q1 = q1.negate();
            cosTheta = -cosTheta;
        }
        if (cosTheta > 0.9995) {
            return q0.scale(1.0 - t).add(q1.scale(t)).normalizeUnit();
        }
        double theta0 = Math.acos(Math.min(1.0, cosTheta));
        double theta = theta0 * t;
        double sinTheta0 = Math.sin(theta0);
        double s0 = Math.cos(theta) - cosTheta * Math.sin(theta) / sinTheta0;
        double s1 = Math.sin(theta) / sinTheta0;
        return q0.scale(s0).add(q1.scale(s1));
    }

    /**
     * Returns {@code true} if {@code |this - other| <= epsilon}.
     *
     * @param other   the quaternion to compare with
     * @param epsilon the non-negative finite tolerance
     * @return {@code true} if within {@code epsilon} in Euclidean 4D norm
     */
    public boolean approxEquals(Quaternion64 other, double epsilon) {
        Objects.requireNonNull(other, "other cannot be null");
        if (Double.isNaN(epsilon) || Double.isInfinite(epsilon) || epsilon < 0.0) {
            throw new IllegalArgumentException("epsilon must be finite and non-negative, got: " + epsilon);
        }
        return subtract(other).abs() <= epsilon;
    }

    /**
     * Returns {@code true} if {@code |this - other| <= NumericContext.currentEpsilon()}.
     *
     * @param other the quaternion to compare with
     * @return {@code true} if approximately equal within the active tolerance
     */
    public boolean approxEquals(Quaternion64 other) {
        return approxEquals(other, NumericContext.currentEpsilon());
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
        appendImaginary(sb, x, "i");
        appendImaginary(sb, y, "j");
        appendImaginary(sb, z, "k");
        return sb.toString();
    }

    private static void appendImaginary(StringBuilder sb, double coeff, String unit) {
        if (coeff == 0.0) {
            return;
        }
        boolean first = sb.isEmpty();
        double absCoeff = Math.abs(coeff);
        if (coeff < 0.0) {
            sb.append(first ? "-" : " - ");
        } else if (!first) {
            sb.append(" + ");
        }
        if (absCoeff != 1.0) {
            sb.append(absCoeff);
        }
        sb.append(unit);
    }
}
