package lock14.algebra.numbers;

import java.util.Objects;
import lock14.algebra.structure.Field;

/**
 * The Field of Complex numbers (C, +, *).
 */
public final class ComplexField implements Field<Complex> {

    public static final ComplexField INSTANCE = new ComplexField();

    private ComplexField() {}

    @Override
    public Complex zero() {
        return Complex.ZERO;
    }

    @Override
    public Complex one() {
        return Complex.ONE;
    }

    @Override
    public Complex add(Complex a, Complex b) {
        return a.add(b);
    }

    @Override
    public Complex negate(Complex a) {
        return a.negate();
    }

    @Override
    public Complex subtract(Complex a, Complex b) {
        return a.subtract(b);
    }

    @Override
    public Complex multiply(Complex a, Complex b) {
        return a.multiply(b);
    }

    @Override
    public Complex reciprocal(Complex a) {
        return a.reciprocal();
    }

    @Override
    public Complex divide(Complex a, Complex b) {
        return a.divide(b);
    }

    @Override
    public boolean isZero(Complex element) {
        return element.isZero();
    }

    @Override
    public boolean isOne(Complex element) {
        return element.isOne();
    }
}
