import java.util.Arrays;

public class MinHeap {
    private int[] heap;
    private int size;
    private long comparisonCount;
    private long movementCount;

    public MinHeap() {
        heap = new int[10];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void insert(int value) {
        ensureCapacity(size + 1);
        heap[size] = value;
        movementCount++;
        siftUp(size);
        size++;
    }

    public int peekMin() {
        if (size == 0) {
            throw new IllegalStateException("Heap is empty");
        }
        return heap[0];
    }

    public int extractMin() {
        if (size == 0) {
            throw new IllegalStateException("Heap is empty");
        }
        int min = heap[0];
        size--;
        if (size > 0) {
            heap[0] = heap[size];
            movementCount++;
            siftDown(0);
        }
        return min;
    }

    public boolean isValidHeap() {
        for (int i = 0; i < size; i++) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            if (left < size && heap[i] > heap[left]) {
                return false;
            }
            if (right < size && heap[i] > heap[right]) {
                return false;
            }
        }
        return true;
    }

    public void resetMetrics() {
        comparisonCount = 0;
        movementCount = 0;
    }

    public long getComparisonCount() {
        return comparisonCount;
    }

    public long getMovementCount() {
        return movementCount;
    }

    private void siftUp(int index) {
        int current = index;
        while (current > 0) {
            int parent = (current - 1) / 2;
            comparisonCount++;
            if (heap[parent] <= heap[current]) {
                break;
            }
            swap(parent, current);
            current = parent;
        }
    }

    private void siftDown(int index) {
        int current = index;
        while (true) {
            int left = 2 * current + 1;
            int right = 2 * current + 2;
            int smallest = current;

            if (left < size) {
                comparisonCount++;
                if (heap[left] < heap[smallest]) {
                    smallest = left;
                }
            }

            if (right < size) {
                comparisonCount++;
                if (heap[right] < heap[smallest]) {
                    smallest = right;
                }
            }

            if (smallest == current) {
                break;
            }

            swap(current, smallest);
            current = smallest;
        }
    }

    private void swap(int a, int b) {
        int temp = heap[a];
        heap[a] = heap[b];
        heap[b] = temp;
        movementCount += 3;
    }

    private void ensureCapacity(int required) {
        if (required <= heap.length) {
            return;
        }
        int newCapacity = Math.max(required, heap.length * 2);
        heap = Arrays.copyOf(heap, newCapacity);
    }
}
