package lock14.group;

public class SquareMatrix<T extends Ring<T>> extends Matrix<T> implements Ring<Matrix<T>> {

    private SquareMatrix(int n) {
        super(n, n);
    }

    public SquareMatrix(int n, T initVal) {
        super(n, n, initVal);
    }

    public SquareMatrix(T[][] matrix) {
        super(matrix);
        if (matrix.length != matrix[0].length) {
            throw new IllegalArgumentException();
        }
    }

    public static <T extends Ring<T>> SquareMatrix<T> of(T[][] matrix) {
        return new SquareMatrix<>(matrix);
    }

    @Override
    public Matrix<T> multiplicativeIdentity() {
        Matrix<T> result = new SquareMatrix<>(matrix.length);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (i == j) {
                    result.matrix[i][j] = this.get(i, j).multiplicativeIdentity();
                } else {
                    result.matrix[i][j] = this.get(i, j).additiveIdentity();
                }
            }
        }
        return result;
    }
}
