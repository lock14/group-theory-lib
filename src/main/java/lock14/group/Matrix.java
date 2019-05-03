package lock14.group;

import java.util.Arrays;
import java.util.Objects;

public class Matrix<T extends Ring<T>> {

    protected Object[][] matrix;

    protected Matrix(int n, int m) {
        matrix = new Object[n][m];
    }

    public Matrix(int n, int m, T initVal) {
        Objects.requireNonNull(initVal);
        matrix = new Object[n][m];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = initVal;
            }
        }
    }

    public Matrix(T[][] matrix) {
        this.matrix = new Object[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                this.matrix[i][j] = Objects.requireNonNull(matrix[i][j]);
            }
        }
    }

    public static <T extends Ring<T>> Matrix<T> of(T[][] matrix) {
        return new Matrix<>(matrix);
    }

    public T get(int i, int j) {
        return (T) matrix[i][j];
    }

    public Matrix<T> times(Matrix<T> other) {
        Matrix<T> result = new Matrix<>(this.matrix.length, other.matrix[0].length, get(0, 0).additiveIdentity());
        for(int i = 0; i < this.matrix.length; i++) {
            for (int j = 0; j < other.matrix[i].length; j++) {
                for (int k = 0; k < this.matrix[i].length; k++) {
                    result.matrix[i][j] = result.get(i, j).plus(this.get(i, k).times(other.get(k, j)));
                }
            }
        }
        return result;
    }

    public Matrix<T> additiveIdentity() {
        return new Matrix<>(matrix.length, matrix[0].length, get(0, 0).additiveIdentity());
    }

    public Matrix<T> additiveInverse() {
        Matrix<T> result = new Matrix<>(matrix.length, matrix[0].length);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                result.matrix[i][j] = this.get(i, j).additiveInverse();
            }
        }
        return result;
    }

    public Matrix<T> minus(Matrix<T> other) {
        Matrix<T> result = new Matrix<>(matrix.length, matrix[0].length);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                result.matrix[i][j] = this.get(i, j).minus(other.get(i, j));
            }
        }
        return result;
    }

    public Matrix<T> plus(Matrix<T> other) {
        Matrix<T> result = new Matrix<>(matrix.length, matrix[0].length);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                result.matrix[i][j] = this.get(i, j).plus(other.get(i, j));
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Matrix)) {
            return false;
        }
        Matrix<?> other = (Matrix<?>) o;
        return Arrays.deepEquals(matrix, other.matrix);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(matrix);
    }

    @Override
    public String toString() {
        return Arrays.deepToString(matrix);
    }
}
