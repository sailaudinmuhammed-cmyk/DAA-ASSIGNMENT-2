import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Benchmark {
    private static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    private static final int REPETITIONS = 5;
    private static final long SEED = 42L;
    private static final Path TABLES = Path.of("results", "tables");

    public static void main(String[] args) throws Exception {
        Files.createDirectories(TABLES);
        runRandomAccess();
        runSearch();
        runInsertionRemoval();
        runHeap();
        System.out.println("Benchmarks completed. CSV files are in results/tables.");
    }

    private static void runRandomAccess() throws IOException {
        List<String> rows = new ArrayList<>();
        rows.add("structure,n,operations,avg_time_ns,accesses,theoretical_complexity");

        for (int n : SIZES) {
            int[] values = randomValues(n, SEED + n);
            int[] indices = randomIndices(10_000, n, SEED + 1000 + n);

            Result dynamic = averageRandomAccessDynamic(values, indices);
            Result linked = averageRandomAccessLinked(values, indices);

            rows.add(csv("DynamicArray", n, indices.length, dynamic, "Theta(1) per get"));
            rows.add(csv("LinkedList", n, indices.length, linked, "Theta(n) average/worst per get"));
        }

        write("workload1_random_access.csv", rows);
    }

    private static void runSearch() throws IOException {
        List<String> rows = new ArrayList<>();
        rows.add("structure,n,operations,avg_time_ns,comparisons,theoretical_complexity");

        for (int n : SIZES) {
            int[] values = randomValues(n, SEED + 2000 + n);
            int[] searches = searchValues(values, 1_000, SEED + 3000 + n);

            Result dynamic = averageSearchDynamic(values, searches);
            Result linked = averageSearchLinked(values, searches);

            rows.add(csv("DynamicArray", n, searches.length, dynamic, "O(n) per contains"));
            rows.add(csv("LinkedList", n, searches.length, linked, "O(n) per contains"));
        }

        write("workload2_search.csv", rows);
    }

    private static void runInsertionRemoval() throws IOException {
        List<String> rows = new ArrayList<>();
        rows.add("structure,operation,position,n,operations,avg_time_ns,metric_count,theoretical_complexity");

        for (int n : SIZES) {
            int[] values = randomValues(n, SEED + 4000 + n);
            int middle = n / 2;

            Result daInsertStart = averageInsertDynamic(values, 0);
            Result llInsertStart = averageInsertLinked(values, 0);
            Result daInsertMiddle = averageInsertDynamic(values, middle);
            Result llInsertMiddle = averageInsertLinked(values, middle);
            Result daRemoveStart = averageRemoveDynamic(values, 0);
            Result llRemoveStart = averageRemoveLinked(values, 0);
            Result daRemoveMiddle = averageRemoveDynamic(values, middle);
            Result llRemoveMiddle = averageRemoveLinked(values, middle);

            rows.add(csvIR("DynamicArray", "insert", "start", n, daInsertStart, "Theta(n)"));
            rows.add(csvIR("LinkedList", "insert", "start", n, llInsertStart, "Theta(1)"));
            rows.add(csvIR("DynamicArray", "insert", "middle", n, daInsertMiddle, "Theta(n)"));
            rows.add(csvIR("LinkedList", "insert", "middle", n, llInsertMiddle, "Theta(n)"));
            rows.add(csvIR("DynamicArray", "remove", "start", n, daRemoveStart, "Theta(n)"));
            rows.add(csvIR("LinkedList", "remove", "start", n, llRemoveStart, "Theta(1)"));
            rows.add(csvIR("DynamicArray", "remove", "middle", n, daRemoveMiddle, "Theta(n)"));
            rows.add(csvIR("LinkedList", "remove", "middle", n, llRemoveMiddle, "Theta(n)"));
        }

        write("workload3_insert_remove.csv", rows);
    }

    private static void runHeap() throws IOException {
        List<String> rows = new ArrayList<>();
        rows.add("operation,n,operations,avg_time_ns,comparisons,theoretical_complexity,sorted_verified");

        for (int n : SIZES) {
            int[] values = randomValues(n, SEED + 5000 + n);
            HeapResult result = averageHeap(values);
            rows.add("insert," + n + "," + n + "," + result.insertTime + "," + result.insertComparisons + ",O(log n) per insert,true");
            rows.add("extractMin," + n + "," + n + "," + result.extractTime + "," + result.extractComparisons + ",O(log n) per extract,true");
        }

        write("workload4_heap.csv", rows);
    }

    private static Result averageRandomAccessDynamic(int[] values, int[] indices) {
        long totalTime = 0;
        long totalMetric = 0;
        long sink = 0;
        for (int r = 0; r < REPETITIONS; r++) {
            DynamicArray array = buildDynamic(values);
            array.resetMetrics();
            long start = System.nanoTime();
            for (int index : indices) {
                sink += array.get(index);
            }
            totalTime += System.nanoTime() - start;
            totalMetric += array.getAccessCount();
        }
        consume(sink);
        return new Result(totalTime / REPETITIONS, totalMetric / REPETITIONS);
    }

    private static Result averageRandomAccessLinked(int[] values, int[] indices) {
        long totalTime = 0;
        long totalMetric = 0;
        long sink = 0;
        for (int r = 0; r < REPETITIONS; r++) {
            LinkedList list = buildLinked(values);
            list.resetMetrics();
            long start = System.nanoTime();
            for (int index : indices) {
                sink += list.get(index);
            }
            totalTime += System.nanoTime() - start;
            totalMetric += list.getAccessCount();
        }
        consume(sink);
        return new Result(totalTime / REPETITIONS, totalMetric / REPETITIONS);
    }

    private static Result averageSearchDynamic(int[] values, int[] searches) {
        long totalTime = 0;
        long totalMetric = 0;
        long sink = 0;
        for (int r = 0; r < REPETITIONS; r++) {
            DynamicArray array = buildDynamic(values);
            array.resetMetrics();
            long start = System.nanoTime();
            for (int value : searches) {
                if (array.contains(value)) {
                    sink++;
                }
            }
            totalTime += System.nanoTime() - start;
            totalMetric += array.getComparisonCount();
        }
        consume(sink);
        return new Result(totalTime / REPETITIONS, totalMetric / REPETITIONS);
    }

    private static Result averageSearchLinked(int[] values, int[] searches) {
        long totalTime = 0;
        long totalMetric = 0;
        long sink = 0;
        for (int r = 0; r < REPETITIONS; r++) {
            LinkedList list = buildLinked(values);
            list.resetMetrics();
            long start = System.nanoTime();
            for (int value : searches) {
                if (list.contains(value)) {
                    sink++;
                }
            }
            totalTime += System.nanoTime() - start;
            totalMetric += list.getComparisonCount();
        }
        consume(sink);
        return new Result(totalTime / REPETITIONS, totalMetric / REPETITIONS);
    }

    private static Result averageInsertDynamic(int[] values, int index) {
        long totalTime = 0;
        long totalMetric = 0;
        for (int r = 0; r < REPETITIONS; r++) {
            DynamicArray array = buildDynamic(values);
            array.resetMetrics();
            long start = System.nanoTime();
            for (int i = 0; i < 1_000; i++) {
                array.add(index, -i);
            }
            totalTime += System.nanoTime() - start;
            totalMetric += array.getMovementCount();
        }
        return new Result(totalTime / REPETITIONS, totalMetric / REPETITIONS);
    }

    private static Result averageInsertLinked(int[] values, int index) {
        long totalTime = 0;
        long totalMetric = 0;
        for (int r = 0; r < REPETITIONS; r++) {
            LinkedList list = buildLinked(values);
            list.resetMetrics();
            long start = System.nanoTime();
            for (int i = 0; i < 1_000; i++) {
                list.add(index, -i);
            }
            totalTime += System.nanoTime() - start;
            totalMetric += list.getAccessCount() + list.getMovementCount();
        }
        return new Result(totalTime / REPETITIONS, totalMetric / REPETITIONS);
    }

    private static Result averageRemoveDynamic(int[] values, int index) {
        long totalTime = 0;
        long totalMetric = 0;
        for (int r = 0; r < REPETITIONS; r++) {
            DynamicArray array = buildDynamic(values);
            array.resetMetrics();
            long elapsed = 0;
            long metric = 0;
            for (int i = 0; i < 1_000; i++) {
                long before = array.getMovementCount() + array.getAccessCount();
                long start = System.nanoTime();
                int removed = array.remove(index);
                elapsed += System.nanoTime() - start;
                long after = array.getMovementCount() + array.getAccessCount();
                metric += after - before;
                array.add(index, removed);
            }
            totalTime += elapsed;
            totalMetric += metric;
        }
        return new Result(totalTime / REPETITIONS, totalMetric / REPETITIONS);
    }

    private static Result averageRemoveLinked(int[] values, int index) {
        long totalTime = 0;
        long totalMetric = 0;
        for (int r = 0; r < REPETITIONS; r++) {
            LinkedList list = buildLinked(values);
            list.resetMetrics();
            long elapsed = 0;
            long metric = 0;
            for (int i = 0; i < 1_000; i++) {
                long before = list.getAccessCount() + list.getMovementCount();
                long start = System.nanoTime();
                int removed = list.remove(index);
                elapsed += System.nanoTime() - start;
                long after = list.getAccessCount() + list.getMovementCount();
                metric += after - before;
                list.add(index, removed);
            }
            totalTime += elapsed;
            totalMetric += metric;
        }
        return new Result(totalTime / REPETITIONS, totalMetric / REPETITIONS);
    }

    private static HeapResult averageHeap(int[] values) {
        long insertTimeTotal = 0;
        long extractTimeTotal = 0;
        long insertComparisonsTotal = 0;
        long extractComparisonsTotal = 0;

        for (int r = 0; r < REPETITIONS; r++) {
            MinHeap heap = new MinHeap();
            heap.resetMetrics();
            long startInsert = System.nanoTime();
            for (int value : values) {
                heap.insert(value);
            }
            insertTimeTotal += System.nanoTime() - startInsert;
            insertComparisonsTotal += heap.getComparisonCount();

            heap.resetMetrics();
            int previous = Integer.MIN_VALUE;
            boolean sorted = true;
            long startExtract = System.nanoTime();
            while (!heap.isEmpty()) {
                int current = heap.extractMin();
                if (current < previous) {
                    sorted = false;
                }
                previous = current;
            }
            extractTimeTotal += System.nanoTime() - startExtract;
            extractComparisonsTotal += heap.getComparisonCount();
            if (!sorted) {
                throw new IllegalStateException("Heap extraction order is invalid");
            }
        }

        return new HeapResult(
                insertTimeTotal / REPETITIONS,
                extractTimeTotal / REPETITIONS,
                insertComparisonsTotal / REPETITIONS,
                extractComparisonsTotal / REPETITIONS
        );
    }

    private static DynamicArray buildDynamic(int[] values) {
        DynamicArray array = new DynamicArray();
        for (int value : values) {
            array.add(value);
        }
        return array;
    }

    private static LinkedList buildLinked(int[] values) {
        LinkedList list = new LinkedList();
        for (int value : values) {
            list.add(value);
        }
        return list;
    }

    private static int[] randomValues(int n, long seed) {
        Random random = new Random(seed);
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = random.nextInt(2_000_001) - 1_000_000;
        }
        return values;
    }

    private static int[] randomIndices(int count, int n, long seed) {
        Random random = new Random(seed);
        int[] indices = new int[count];
        for (int i = 0; i < count; i++) {
            indices[i] = random.nextInt(n);
        }
        return indices;
    }

    private static int[] searchValues(int[] values, int count, long seed) {
        Random random = new Random(seed);
        int[] searches = new int[count];
        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                searches[i] = values[random.nextInt(values.length)];
            } else {
                searches[i] = 2_000_001 + i;
            }
        }
        return searches;
    }

    private static String csv(String structure, int n, int operations, Result result, String complexity) {
        return structure + "," + n + "," + operations + "," + result.timeNs + "," + result.metric + "," + complexity;
    }

    private static String csvIR(String structure, String operation, String position, int n, Result result, String complexity) {
        return structure + "," + operation + "," + position + "," + n + ",1000," + result.timeNs + "," + result.metric + "," + complexity;
    }

    private static void write(String file, List<String> lines) throws IOException {
        Files.write(TABLES.resolve(file), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void consume(long value) {
        if (value == Long.MIN_VALUE) {
            System.out.println(value);
        }
    }

    private record Result(long timeNs, long metric) {}

    private record HeapResult(long insertTime, long extractTime, long insertComparisons, long extractComparisons) {}
}
