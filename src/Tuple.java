import java.util.Objects;

public class Tuple {

    public int x;
    public int y;

    public Tuple() {
        this.x = 0;
        this.y = 0;
    }

    public Tuple(Tuple t) {
        this.x = t.x;
        this.y = t.y;
    }

    public Tuple(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Tuple(String str) {
        String[] vals = str.split(",");
        x = Integer.parseInt(vals[0]);
        y = Integer.parseInt(vals[1]);
    }

    public boolean equals(Tuple other) {
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple tuple = (Tuple) o;
        return x == tuple.x && y == tuple.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public String toString() {
        return x + "," + y;
    }

    public static double mhn_distance(Tuple t1, Tuple t2) {
        return mhn_x(t1, t2) + mhn_y(t1, t2);
    }
    public static double mhn_x(Tuple t1, Tuple t2) { return Math.abs(t1.x - t2.x); }
    public static double mhn_y(Tuple t1, Tuple t2) { return Math.abs(t1.y - t2.y); }
}
