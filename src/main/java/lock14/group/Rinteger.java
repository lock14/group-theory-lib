package lock14.group;

public class Rinteger implements CommutativeRing<Rinteger>, Comparable<Rinteger> {
    public static final Rinteger ZERO = Rinteger.of(0);
    public static final Rinteger ONE = Rinteger.of(1);

    private final int n;

    private Rinteger(int n) {
        this.n = n;
    }

    public static Rinteger of(int n) {
        if (n >= RintegerCache.low && n <= RintegerCache.high) {
            return RintegerCache.cache[n + (-RintegerCache.low)];
        }
        return new Rinteger(n);
    }

    @Override
    public Rinteger additiveIdentity() {
        return ZERO;
    }

    @Override
    public Rinteger additiveInverse() {
        return Rinteger.of(-n);
    }

    @Override
    public Rinteger multiplicativeIdentity() {
        return ONE;
    }

    @Override
    public Rinteger minus(Rinteger other) {
        return Rinteger.of(this.n - other.n);
    }

    @Override
    public Rinteger plus(Rinteger other) {
        return Rinteger.of(this.n + other.n);
    }

    @Override
    public Rinteger times(Rinteger other) {
        return Rinteger.of(this.n * other.n);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Rinteger)) {
            return false;
        }
        Rinteger other = (Rinteger) o;
        return this.n == other.n;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(n);
    }

    @Override
    public String toString() {
        return Integer.toString(n);
    }

    @Override
    public int compareTo(Rinteger other) {
        return Integer.compare(this.n, other.n);
    }

    private static final class RintegerCache {
        static final int low = -128;
        static final int high =  127;
        static final Rinteger[] cache;

        static {
            cache = new Rinteger[(high - low) + 1];
            int j = low;
            for (int k = 0; k < cache.length; k++) {
                cache[k] = new Rinteger(j++);
            }
        }

        private RintegerCache() {
        }
    }
}
