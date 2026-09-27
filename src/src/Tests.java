import java.util.ArrayList;
import java.util.PriorityQueue;

public class Tests {
    public static void main(String[] args) {
        testDynamicArray();
        testLinkedList();
        testMinHeap();
        System.out.println("All tests passed.");
    }

    private static void testDynamicArray() {
        DynamicArray array = new DynamicArray();
        ArrayList<Integer> reference = new ArrayList<>();

        check(array.size() == 0, "DynamicArray empty size");
        array.add(10);
        reference.add(10);
        array.add(20);
        reference.add(20);
        array.add(1, 15);
        reference.add(1, 15);
        array.add(20);
        reference.add(20);

        for (int i = 0; i < reference.size(); i++) {
            check(array.get(i) == reference.get(i), "DynamicArray get");
        }

        check(array.contains(15), "DynamicArray contains existing");
        check(!array.contains(99), "DynamicArray contains missing");
        check(array.remove(1) == reference.remove(1), "DynamicArray remove");

        expectIndexError(() -> array.get(-1));
        expectIndexError(() -> array.get(array.size()));
        expectIndexError(() -> array.add(array.size() + 1, 5));

        for (int i = 0; i < 100_000; i++) {
            array.add(i);
        }
        check(array.size() == reference.size() + 100_000, "DynamicArray large input");
    }

    private static void testLinkedList() {
        LinkedList list = new LinkedList();
        java.util.LinkedList<Integer> reference = new java.util.LinkedList<>();

        check(list.size() == 0, "LinkedList empty size");
        list.add(10);
        reference.add(10);
        list.add(20);
        reference.add(20);
        list.add(1, 15);
        reference.add(1, 15);
        list.add(20);
        reference.add(20);

        for (int i = 0; i < reference.size(); i++) {
            check(list.get(i) == reference.get(i), "LinkedList get");
        }

        check(list.contains(15), "LinkedList contains existing");
        check(!list.contains(99), "LinkedList contains missing");
        check(list.remove(1) == reference.remove(1), "LinkedList remove");

        expectIndexError(() -> list.get(-1));
        expectIndexError(() -> list.get(list.size()));
        expectIndexError(() -> list.add(list.size() + 1, 5));

        for (int i = 0; i < 100_000; i++) {
            list.add(i);
        }
        check(list.size() == reference.size() + 100_000, "LinkedList large input");
    }

    private static void testMinHeap() {
        MinHeap heap = new MinHeap();
        PriorityQueue<Integer> reference = new PriorityQueue<>();
        int[] values = {5, 3, 8, 1, 1, 7, 2, 10, -4};

        for (int value : values) {
            heap.insert(value);
            reference.add(value);
            check(heap.isValidHeap(), "Heap property after insert");
            check(heap.peekMin() == reference.peek(), "Heap peekMin");
        }

        int previous = Integer.MIN_VALUE;
        while (!reference.isEmpty()) {
            int actual = heap.extractMin();
            int expected = reference.remove();
            check(actual == expected, "Heap extractMin against PriorityQueue");
            check(actual >= previous, "Heap non-decreasing extraction");
            check(heap.isValidHeap(), "Heap property after extract");
            previous = actual;
        }

        boolean thrown = false;
        try {
            heap.peekMin();
        } catch (IllegalStateException e) {
            thrown = true;
        }
        check(thrown, "Heap empty peekMin");
    }

    private static void expectIndexError(Runnable action) {
        boolean thrown = false;
        try {
            action.run();
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        check(thrown, "Expected IndexOutOfBoundsException");
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError("Failed: " + name);
        }
    }
}
