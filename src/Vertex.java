import java.util.Objects;

public class Vertex {

    public Tuple point;
    public double f;
    public double g;
    public double h;

    public Vertex(Tuple point, double h, double g) {
        this.point = point;
        this.h = h;
        this.g = g;
        this.f = h + g;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return Objects.equals(point, vertex.point);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, f, g, h);
    }
}
