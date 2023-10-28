package benchmark;

import org.openjdk.jmh.annotations.*;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;

@State(Scope.Benchmark)
public class LockingComparisonLinkedList {
    //**************************************************
    // JMH MicroBenchmark Adjustments
    public final int FORK_VALUE = 1;                        // Specify the number of JVM process forks
    public final int WARMUP_ITERATIONS = 2;                 // Specify the number of warmup iterations
    public final int MEASUREMENT_ITERATIONS = 5;            // Specify the number of measurement iterations

    //******************************************************
    // "Workload" Adjustments
    public final int THREAD_OPERATION_ITERATIONS = 5;       // Specify the number of read or write iterations done on each thread per operation
    public final int NUM_THREADS = 6;                       // Specify the number of threads performing the benchmarks

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private final LinkedList<Integer> linkedList;
        private final ReentrantLock handOverHandLock;
        private final ReentrantLock lazyLock;

        public BenchmarkState() {
            linkedList = new LinkedList<>();
            handOverHandLock = new ReentrantLock();
            lazyLock = new ReentrantLock();
        }
    }

    //****************************************************************************************************
    // handOverHandLockLinkedListBenchmark
    //****************************************************************************************************
    // In the handOverHandLockLinkedListBenchmark method, when a thread wants to read or write, it acquires a lock
    // (handOverHandLock) at the beginning and releases it at the end of its operation. This ensures
    // exclusive access to shared data during both read and write operations, so there's no concurrent
    // access by multiple threads.
    //
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = FORK_VALUE)
    @Warmup(iterations = WARMUP_ITERATIONS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS)
    public void handOverHandLockLinkedListBenchmark(BenchmarkState state) {
        for (int i = 1; i <= NUM_THREADS; i++) {
            final int clientId = i;
            new Thread(() -> {
                for (int j = 1; j <= THREAD_OPERATION_ITERATIONS; j++) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.2) {
                        addElementHandOverHand(clientId * 10 + j, state);
                    } else {
                        readElementsHandOverHand(state);
                    }
                }
            }).start();
        }
    }

    public void addElementHandOverHand(int value, BenchmarkState state) {
        state.handOverHandLock.lock();
        try {
            state.linkedList.addFirst(value);
        } finally {
            state.handOverHandLock.unlock();
        }
    }

    public void readElementsHandOverHand(BenchmarkState state) {
        state.handOverHandLock.lock();
        try {
            for (Integer value : state.linkedList) {
                // Simulate reading the value
                int val = value;
            }
        } finally {
            state.handOverHandLock.unlock();
        }
    }

    //****************************************************************************************************
    // lazyLockLinkedListBenchmark
    //****************************************************************************************************
    // In the lazyLockLinkedListBenchmark method, when a thread wants to write, it acquires a lock (lazyLock)
    // only during the actual write operation. During read operations, it doesn't lock the shared data.
    // This allows multiple threads to read data simultaneously without blocking each other.
    //
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = FORK_VALUE)
    @Warmup(iterations = WARMUP_ITERATIONS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS)
    public void lazyLockLinkedListBenchmark(BenchmarkState state) {
        for (int i = 1; i <= NUM_THREADS; i++) {
            final int clientId = i;
            new Thread(() -> {
                for (int j = 1; j <= THREAD_OPERATION_ITERATIONS; j++) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.2) {
                        addElementLazyLock(clientId * 10 + j, state);
                    } else {
                        readElementsLazyLock(state);
                    }
                }
            }).start();
        }
    }

    public void addElementLazyLock(int value, BenchmarkState state) {
        state.lazyLock.lock();
        try {
            state.linkedList.addFirst(value);
        } finally {
            state.lazyLock.unlock();
        }
    }

    public void readElementsLazyLock(BenchmarkState state) {
        for (Integer value : state.linkedList) {
            // Simulate reading the value
            int val = value;
        }
    }
}
