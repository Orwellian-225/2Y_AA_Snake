import java.util.Objects;

public class Tuple {

    public int x = -1;
    public int y = -1;

    public Tuple() {
        this.x = 0;
        this.y = 0;
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

    public void update(String str) {
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
}
