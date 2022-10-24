public class MinHeap {

    private Tuple[] Heap;
    private int size;
    private int maxsize;

    private static final int FRONT = 1;

    public MinHeap(int maxsize)
    {
        this.maxsize = maxsize;
        this.size = 0;

        Heap = new Tuple[this.maxsize + 1];
        Heap[0] = new Tuple(0, 0);
    }

    private int parent(int pos) { return pos / 2; }
    private int leftChild(int pos) { return (2 * pos); }
    private int rightChild(int pos) { return (2 * pos) + 1; }
    private boolean isLeaf(int pos) { return pos > (size / 2); }

    private void swap(int fpos, int spos) {

        Tuple tmp;
        tmp = new Tuple(Heap[fpos]);

        Heap[fpos] = Heap[spos];
        Heap[spos] = tmp;
    }
    private void minHeapify(int pos, Double[][] f, Double[][] h) {
        if(!isLeaf(pos)){
            int swapPos = pos;

            if(rightChild(pos) <= size) {
                swapPos = compare_tuples(leftChild(pos), rightChild(pos), f, h) ? leftChild(pos) : rightChild(pos);
            } else {
                swapPos = leftChild(pos);
            }

            if(compare_tuples(pos, leftChild(pos), f, h)) {
                swap(pos,swapPos);
                minHeapify(swapPos, f, h);
            }

        }
    }

    public void insert(Tuple element, Double[][] f, Double[][] h) {

        if (size >= maxsize) {
            return;
        }

        Heap[++size] = element;
        int current = size;

        while (compare_tuples(current, parent(current), f, h)) {
            swap(current, parent(current));
            current = parent(current);
        }
    }

    public Tuple remove(Double[][] f, Double[][] h) {
        Tuple popped = Heap[FRONT];
        Heap[FRONT] = Heap[size--];
        minHeapify(FRONT, f, h);

        return popped;
    }

    private double read_map(Double[][] map, Tuple t) {

        if(t.y >= map.length || t.y < 0 || t.x >= map[0].length || t.x < 0 || map[t.y][t.x] == null) {
            return 0.0;
        }

        return map[t.y][t.x];
    }

    private boolean compare_tuples(int i, int j, Double[][] f, Double[][] h) {
        return read_map(f, Heap[i]) < read_map(f, Heap[parent(j)]) ||
                (read_map(f, Heap[i]) == read_map(f, Heap[parent(j)]) &&
                read_map(h, Heap[i]) < read_map(h, Heap[parent(j)]));
    }

    public boolean contains(Tuple t) {
        for(Tuple tuple : Heap) {
            if(t.equals(tuple)) { return true; }
        }
        return false;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
