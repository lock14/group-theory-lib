package lock14.group;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MatrixTest {

    @Test
    public void testMult() {
        Rinteger[][] m1 = {{Rinteger.of(3), Rinteger.of(-2), Rinteger.of(5)},
                           {Rinteger.of(3), Rinteger.of(0), Rinteger.of(4)}};
        Rinteger[][] m2 = {{Rinteger.of(2), Rinteger.of(3)},
                           {Rinteger.of(-9), Rinteger.of(0)},
                           {Rinteger.of(0), Rinteger.of(4)}};
        Rinteger[][] m3 = {{Rinteger.of(24), Rinteger.of(29)},
                           {Rinteger.of(6), Rinteger.of(25)}};
        Matrix<Rinteger> matrix1 = Matrix.of(m1);
        Matrix<Rinteger> matrix2 = Matrix.of(m2);
        assertEquals(Matrix.of(m3), matrix1.times(matrix2));
    }

    @Test
    public void testIdentity() {
        Rinteger[][] m1 = {{Rinteger.of(1), Rinteger.of(2), Rinteger.of(3)},
                           {Rinteger.of(4), Rinteger.of(5), Rinteger.of(6)},
                           {Rinteger.of(7), Rinteger.of(8), Rinteger.of(9)}};
        SquareMatrix<Rinteger> matrix = SquareMatrix.of(m1);
        assertEquals(matrix, matrix.times(matrix.multiplicativeIdentity()));
        assertEquals(matrix, matrix.multiplicativeIdentity().times(matrix));
    }
}
