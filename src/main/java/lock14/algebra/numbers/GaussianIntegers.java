package lock14.algebra.numbers;

import java.math.BigInteger;
import java.util.Objects;
import lock14.algebra.exceptions.IncompatibleAlgebraException;
import lock14.algebra.exceptions.NonInvertibleElementException;
import lock14.algebra.structure.EuclideanDomain;

/**
 * The Exact Euclidean Domain of Gaussian Integers {@code Z[i]} with Euclidean norm
 * {@code N(a + bi) = a^2 + b^2} and first-quadrant canonical associate normalization.
 */
public final class GaussianIntegers implements EuclideanDomain<Complex<BigInteger>> {

    /**
     * Singleton instance of the Euclidean domain of Gaussian integers {@code Z[i]}.
     */
    public static final GaussianIntegers DOMAIN = new GaussianIntegers();

    private GaussianIntegers() {}

    /**
     * Constructs a Gaussian integer {@code real + imaginary * i} from {@code long} coordinates.
     *
     * @param real      the integer real part
     * @param imaginary the integer imaginary part
     * @return the Gaussian integer {@code real + imaginary * i}
     */
    public Complex<BigInteger> of(long real, long imaginary) {
        return Complex.gaussian(real, imaginary);
    }

    /**
     * Constructs a Gaussian integer {@code real + imaginary * i} from {@link BigInteger} coordinates.
     *
     * @param real      the arbitrary-precision integer real part
     * @param imaginary the arbitrary-precision integer imaginary part
     * @return the Gaussian integer {@code real + imaginary * i}
     */
    public Complex<BigInteger> of(BigInteger real, BigInteger imaginary) {
        return Complex.gaussian(real, imaginary);
    }

    @Override
    public Complex<BigInteger> zero() {
        return Complex.gaussian(0, 0);
    }

    @Override
    public Complex<BigInteger> one() {
        return Complex.gaussian(1, 0);
    }

    @Override
    public Complex<BigInteger> add(Complex<BigInteger> a, Complex<BigInteger> b) {
        checkInDomain(a);
        checkInDomain(b);
        return a.add(b);
    }

    @Override
    public Complex<BigInteger> negate(Complex<BigInteger> a) {
        checkInDomain(a);
        return a.negate();
    }

    @Override
    public Complex<BigInteger> subtract(Complex<BigInteger> a, Complex<BigInteger> b) {
        checkInDomain(a);
        checkInDomain(b);
        return a.subtract(b);
    }

    @Override
    public Complex<BigInteger> multiply(Complex<BigInteger> a, Complex<BigInteger> b) {
        checkInDomain(a);
        checkInDomain(b);
        return a.multiply(b);
    }

    @Override
    public boolean isZero(Complex<BigInteger> element) {
        checkInDomain(element);
        return element.isZero();
    }

    @Override
    public boolean isOne(Complex<BigInteger> element) {
        checkInDomain(element);
        return element.isOne();
    }

    @Override
    public boolean isUnit(Complex<BigInteger> element) {
        checkInDomain(element);
        return element.normSq().equals(BigInteger.ONE);
    }

    /**
     * Rotates any non-zero Gaussian integer into its unique first-quadrant associate {@code {re > 0, im >= 0}}.
     *
     * @param element the Gaussian integer to normalize
     * @return {@code 0} if {@code element} is zero, otherwise its canonical first-quadrant associate
     */
    @Override
    public Complex<BigInteger> normalize(Complex<BigInteger> element) {
        checkInDomain(element);
        if (element.isZero()) {
            return zero();
        }
        BigInteger re = element.real();
        BigInteger im = element.imaginary();
        if (re.signum() > 0 && im.signum() >= 0) {
            return element;
        }
        if (re.signum() <= 0 && im.signum() > 0) {
            return Complex.gaussian(im, re.negate());
        }
        if (re.signum() < 0 && im.signum() <= 0) {
            return Complex.gaussian(re.negate(), im.negate());
        }
        return Complex.gaussian(im.negate(), re);
    }

    @Override
    public DivisionResult<Complex<BigInteger>> divideAndRemainder(Complex<BigInteger> a, Complex<BigInteger> b) {
        checkInDomain(a);
        checkInDomain(b);
        if (b.isZero()) {
            throw new NonInvertibleElementException("Gaussian integer division by zero");
        }
        Complex<BigInteger> prod = a.multiply(b.conjugate());
        BigInteger normB = b.normSq();
        BigInteger qRe = roundNearest(prod.real(), normB);
        BigInteger qIm = roundNearest(prod.imaginary(), normB);
        Complex<BigInteger> quotient = Complex.gaussian(qRe, qIm);
        Complex<BigInteger> remainder = a.subtract(quotient.multiply(b));
        return new DivisionResult<>(quotient, remainder);
    }

    private static BigInteger roundNearest(BigInteger num, BigInteger posDen) {
        DivisionResult<BigInteger> qr = Integers.RING.divideAndRemainder(num, posDen);
        BigInteger q = qr.quotient();
        BigInteger r = qr.remainder();
        return r.shiftLeft(1).compareTo(posDen) >= 0 ? q.add(BigInteger.ONE) : q;
    }

    private void checkInDomain(Complex<BigInteger> z) {
        Objects.requireNonNull(z, "element cannot be null");
        if (!Objects.equals(z.scalarRing(), Integers.RING)) {
            throw new IncompatibleAlgebraException("Element does not belong to GaussianIntegers Z[i]");
        }
    }
}
