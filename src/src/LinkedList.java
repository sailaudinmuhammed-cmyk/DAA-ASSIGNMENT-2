public class LinkedList {
    private static class Node {
        int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    private Node head;
    private Node tail;
    private int size;
    private long accessCount;
    private long comparisonCount;
    private long movementCount;

    public int size() {
        return size;
    }

    public void add(int value) {
        Node node = new Node(value);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
        movementCount++;
    }

    public void add(int index, int value) {
        checkPositionIndex(index);
        if (index == size) {
            add(value);
            return;
        }
        Node node = new Node(value);
        if (index == 0) {
            node.next = head;
            head = node;
            if (tail == null) {
                tail = node;
            }
            size++;
            movementCount++;
            return;
        }
        Node previous = nodeAt(index - 1);
        node.next = previous.next;
        previous.next = node;
        size++;
        movementCount++;
    }

    public int remove(int index) {
        checkElementIndex(index);
        if (index == 0) {
            accessCount++;
            int value = head.value;
            head = head.next;
            size--;
            movementCount++;
            if (size == 0) {
                tail = null;
            }
            return value;
        }
        Node previous = nodeAt(index - 1);
        Node target = previous.next;
        accessCount++;
        previous.next = target.next;
        if (target == tail) {
            tail = previous;
        }
        size--;
        movementCount++;
        return target.value;
    }

    public int get(int index) {
        checkElementIndex(index);
        return nodeAt(index).value;
    }

    public boolean contains(int value) {
        Node current = head;
        while (current != null) {
            accessCount++;
            comparisonCount++;
            if (current.value == value) {
                return true;
            }
            current = current.next;
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

    private Node nodeAt(int index) {
        Node current = head;
        for (int i = 0; i < index; i++) {
            accessCount++;
            current = current.next;
        }
        accessCount++;
        return current;
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
