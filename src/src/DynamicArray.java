import java.util.Arrays;

public class DynamicArray {
    private int[] data;
    private int size;
    private long accessCount;
    private long comparisonCount;
    private long movementCount;

    public DynamicArray() {
        data = new int[10];
    }

    public int size() {
        return size;
    }

    public void add(int value) {
        ensureCapacity(size + 1);
        data[size++] = value;
        movementCount++;
    }

    public void add(int index, int value) {
        checkPositionIndex(index);
        ensureCapacity(size + 1);
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            movementCount++;
        }
        data[index] = value;
        movementCount++;
        size++;
    }

    public int remove(int index) {
        checkElementIndex(index);
        int removed = data[index];
        accessCount++;
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
            movementCount++;
        }
        size--;
        return removed;
    }

    public int get(int index) {
        checkElementIndex(index);
        accessCount++;
        return data[index];
    }

    public boolean contains(int value) {
        for (int i = 0; i < size; i++) {
            accessCount++;
            comparisonCount++;
            if (data[i] == value) {
                return true;
            }
        }
        return false;
    }

    public void resetMetrics() {
        accessCount = 0;
        comparisonCount = 0;
        movementCount = 0;
    }

    public long getAccessCount() {
        return accessCount;
    }

    public long getComparisonCount() {
        return comparisonCount;
    }

    public long getMovementCount() {
        return movementCount;
    }

    public int[] toArray() {
        return Arrays.copyOf(data, size);
    }

    private void ensureCapacity(int required) {
        if (required <= data.length) {
            return;
        }
        int newCapacity = Math.max(required, data.length * 2);
        data = Arrays.copyOf(data, newCapacity);
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }

    private void checkPositionIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }
}
