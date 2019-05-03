package lock14.group;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ComplexTest {

    @Test
    public void testMultiply() {
        Complex a = Complex.of(2, 3);
        Complex b = Complex.of(4, 5);
        assertEquals(Complex.of(-7, 22), a.times(b));
    }

    @Test
    public void testDivide() {
        Complex a = Complex.of(2, 3);
        Complex b = Complex.of(4, 5);
        assertEquals(Complex.of(23.0 / 41.0, 2.0 / 41.0), a.divide(b));
    }
}
