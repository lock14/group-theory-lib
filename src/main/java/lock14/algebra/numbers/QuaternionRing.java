package lock14.algebra.numbers;

import lock14.algebra.structure.Ring;

/**
 * The Non-Commutative Division Ring (Skew-Field) of Hamilton Quaternions (H, +, *).
 */
public final class QuaternionRing implements Ring<Quaternion> {

    public static final QuaternionRing INSTANCE = new QuaternionRing();

    private QuaternionRing() {}

    @Override
    public Quaternion zero() {
        return Quaternion.ZERO;
    }

    @Override
    public Quaternion one() {
        return Quaternion.ONE;
    }

    @Override
    public Quaternion add(Quaternion a, Quaternion b) {
        return a.add(b);
    }

    @Override
    public Quaternion negate(Quaternion a) {
        return a.negate();
    }

    @Override
    public Quaternion subtract(Quaternion a, Quaternion b) {
        return a.subtract(b);
    }

    @Override
    public Quaternion multiply(Quaternion a, Quaternion b) {
        return a.multiply(b);
    }

    @Override
    public boolean isZero(Quaternion element) {
        return element.isZero();
    }

    @Override
    public boolean isOne(Quaternion element) {
        return element.isOne();
    }
}
