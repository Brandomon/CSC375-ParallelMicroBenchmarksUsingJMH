package benchmark;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
//***********************************************************************************************************************
//
//  Name: Brandon LaPointe
//  Class: CSC375
//  Professor: Doug Lea
//  Assignment #2
//
//***********************************************************************************************************************
//                                                  NOTES:
//***********************************************************************************************************************
//
// Assignment 2
//
// This is mainly an exercise in performance measurement. Each of the following steps has many possible variations;
// you are free to choose any of them.
//
// 1. Think of some kind of application in which a set of threads all rely on a shared collection of data; sometimes read-only,
//    sometimes modifying the data. For example, a game-server with game-state as the collection, or a campus course scheduling
//    system. Write a stripped-down version of this in which all the threads just emulate clients, and further strips out
//    nearly everything except the reading and writing (while still somehow using results).
//
// 2. Write one solution using a data structure and/or locking scheme of your own devising (most likely a variant of some
//    known technique). Write another to primarily use standard platform library components.
//
// 3. Compare the throughput of your program across at least two different loads on each of at least two different
//    platforms. Use JMH unless you have an approved reason not to.
//
// 4. Plot your results as a set of graphs and place on a web page.
//
//***********************************************************************************************************************
//                                                  JMH
//
// JMH (Java Microbenchmarking Harness) is a Java library and framework for conducting microbenchmarks.
//
// Microbenchmarks are a specific type of performance test designed to measure the execution time of a small piece of
// code or a specific operation in isolation.
//
// JMH is widely used for benchmarking and comparing the performance of Java code.
//
// JMH is a powerful tool for microbenchmarking because it provides a controlled environment for performance testing,
// automatically handles many of the complexities involved in benchmarking, and produces reliable and repeatable results.
//
// When conducting microbenchmarks with JMH, it's important to consider factors like JVM optimizations, warmup iterations,
// and benchmark mode to ensure accurate and meaningful results.
//
//
//***********************************************************************************************************************
//                                                  FORK
//
// The @Fork annotation in JMH (Java Microbenchmarking Harness) controls the number of JVM forks. A "fork"
// in JMH is the act of creating a new JVM process to run a benchmark. The @Fork annotation specifies how
// many JVM forks you want to use when running your benchmarks.
//
// Here's how it works:
//
// If you set @Fork(1), it means that each benchmark method will be run in a separate JVM process. This is
// useful for isolating benchmark runs from each other. It's often used when you want to ensure that the state
// of the JVM doesn't affect the benchmark results.
//
// If you set @Fork(5), it means that each benchmark method will be run in five separate JVM processes. This is
// useful for running benchmarks multiple times to get more stable and reliable results. Running benchmarks multiple
// times helps minimize the effects of warmup and other potential transient behaviors.
//
// The number you specify for @Fork controls how many times the benchmark will be executed in separate JVM processes.
// It can help in reducing the impact of factors like Just-In-Time (JIT) compilation and garbage collection on the
// benchmark results.
//
// Typically, when running JMH benchmarks, you'll want to use multiple forks to obtain more reliable results, especially
// when dealing with microbenchmarks where small variations can have a significant impact on the results. However, using
// multiple forks also increases the total runtime of your benchmarks.
//
//***********************************************************************************************************************
//                                                  WARMUP
//
// The @Warmup annotation in JMH (Java Microbenchmarking Harness) specifies the number of iterations used to warm up the
// benchmark. Warm-up iterations are essentially runs of the benchmark that are executed before the actual measurement
// starts. Their purpose is to allow the JVM to reach a stable state in terms of optimizations before recording
// benchmark measurements.
//
// Here's how it works:
//
// Warmup Iterations: The @Warmup annotation allows you to specify how many iterations should be used for warmup. For
// example, @Warmup(iterations = 2) means that the benchmark will be executed twice before the actual measurement begins.
//
// Warmup Time: Alternatively, you can specify a warmup time in milliseconds using time. For example, @Warmup(time = 2,
// timeUnit = TimeUnit.SECONDS) means that the warmup will run for 2 seconds.
//
// The primary goal of warmup iterations is to ensure that the JVM's Just-In-Time (JIT) compiler has had the opportunity
// to perform its optimizations. The JVM typically performs various optimizations (inlining, method compilation, etc.)
// as it observes the behavior of the application during execution. Warmup iterations allow these optimizations to take
// place, potentially improving the accuracy of benchmark results.
//
// After the warmup iterations are completed, the actual measurement begins. Measurement iterations (controlled by the
// @Measurement annotation) record the benchmark's performance metrics. The results collected during measurement
// iterations are the ones that you analyze to understand the performance of your code.
//
// In summary, warmup iterations are a crucial part of JMH benchmarks as they help ensure that the JVM reaches a stable
// and optimized state before measuring the code's performance. The specific number of warmup iterations can vary
// depending on the nature of the benchmark and the code being tested.
//
//***********************************************************************************************************************
//                                      HAND-OVER-HAND LOCKING VS. LAZY LOCKING
//
// The hand-over-hand locking approach uses strict locking, which means that a thread cannot read data while another
// thread is writing, and vice versa. As a result, it might lead to more contention in a scenario with high concurrent
// read and write operations.
//
// With the lazy locking approach, only when a thread wants to write, it acquires the lock, writes the data, and then
// releases the lock. This approach reduces contention during read operations and can improve throughput in scenarios
// with more read operations and fewer write operations.
//
//***********************************************************************************************************************
//          INITIAL BENCHMARK TESTS WITH NODE STRUCTURE (Simulated LinkedList) USING SIMPLE LOCKING METHODS
//                  TODO Develop locking methods to actually represent hand-over-hand and lazy locking
//
// FV = FORK_VALUE, WI = WARMUP_ITERATIONS, MI = MEASUREMENT_ITERATIONS, TOI = THREAD_OPERATION_ITERATIONS, NT = NUM_THREADS
//
// FV=1, WI=2, MI=5, TOI=50, NT=8 -> Crashed Discord and stopped processes on entire system for a minute or two.
// FV=1, WI=2, MI=5, TOI=20, NT=6 -> Crashed Discord and stopped processes on entire system for a minute or two.
// -> High number of thread operation iterations and threads = bad time
//
// FV=1, WI=2, MI=5, TOI=1, NT=6
// Benchmark                                    Mode  Cnt  Score    Error  Units
// LockingComparison.handOverHandLockBenchmark  avgt    5  0.003 ±  0.005   s/op
// LockingComparison.lazyLockBenchmark          avgt    5  0.001 ±  0.001   s/op
//
// FV=1, WI=2, MI=5, TOI=5, NT=6
// Benchmark                                    Mode  Cnt  Score   Error  Units
// LockingComparison.handOverHandLockBenchmark  avgt    5  0.008 ± 0.003   s/op
// LockingComparison.lazyLockBenchmark          avgt    5  0.002 ± 0.002   s/op
//
// FV=1, WI=2, MI=5, TOI=10, NT=6
// Benchmark                                    Mode  Cnt  Score   Error  Units
// LockingComparison.handOverHandLockBenchmark  avgt    5  0.010 ± 0.013   s/op
// LockingComparison.lazyLockBenchmark          avgt    5  0.004 ± 0.004   s/op
//
// FV=1, WI=2, MI=5, TOI=1, NT=2
// Benchmark                                    Mode  Cnt   Score    Error  Units
// LockingComparison.handOverHandLockBenchmark  avgt    5   0.001 ±  0.002   s/op
// LockingComparison.lazyLockBenchmark          avgt    5  ≈ 10⁻⁴            s/op
//
// FV=1, WI=2, MI=5, TOI=5, NT=2
// Benchmark                                    Mode  Cnt  Score    Error  Units
// LockingComparison.handOverHandLockBenchmark  avgt    5  0.002 ±  0.003   s/op
// LockingComparison.lazyLockBenchmark          avgt    5  0.001 ±  0.001   s/op
//
// FV=1, WI=2, MI=5, TOI=10, NT=2
// Benchmark                                    Mode  Cnt  Score   Error  Units
// LockingComparison.handOverHandLockBenchmark  avgt    5  0.004 ± 0.004   s/op
// LockingComparison.lazyLockBenchmark          avgt    5  0.001 ± 0.001   s/op
//
// FV=1, WI=2, MI=5, TOI=20, NT=2
// -> Failed several times after lazyLockBenchmark iteration 5
// Benchmark                                    Mode  Cnt  Score   Error  Units
// LockingComparison.handOverHandLockBenchmark  avgt    5  0.004 ± 0.005   s/op
// LockingComparison.lazyLockBenchmark          avgt    5  0.016 ± 0.114   s/op
// Benchmark                                    Mode  Cnt  Score   Error  Units
// LockingComparison.handOverHandLockBenchmark  avgt    5  0.004 ± 0.004   s/op
// LockingComparison.lazyLockBenchmark          avgt    5  0.003 ± 0.008   s/op
// -> Lazy locking benchmark seems to be having issues with larger numbers of thread operation iterations
//
// FV=1, WI=2, MI=5, TOI=5, NT=5
// Benchmark                                    Mode  Cnt  Score   Error  Units
// LockingComparison.handOverHandLockBenchmark  avgt    5  0.011 ± 0.029   s/op
// LockingComparison.lazyLockBenchmark          avgt    5  0.002 ± 0.001   s/op
//
//***********************************************************************************************************************
//                              APPLYING TO JAVA LINKED LIST USING SIMPLE LOCKING METHODS
//                  TODO Develop locking methods to actually represent hand-over-hand and lazy locking
//
// FV=1, WI=2, MI=5, TOI=5, NT=5
//
// When running lazy locking with linked list, throws ConcurrentModificationException every time a thread tries to read?
//
// Exception in thread "Thread-300810" java.util.ConcurrentModificationException
//	at java.base/java.util.LinkedList$ListItr.checkForComodification(LinkedList.java:977)
//	at java.base/java.util.LinkedList$ListItr.next(LinkedList.java:899)
//	at benchmark.LockingComparisonLinkedList.readElementsLazyLock(LockingComparisonLinkedList.java:119)
//	at benchmark.LockingComparisonLinkedList.lambda$lazyLockLinkedListBenchmark$1(LockingComparisonLinkedList.java:102)
//	at java.base/java.lang.Thread.run(Thread.java:1583)
// Exception in thread "Thread-300195" java.util.ConcurrentModificationException
//	at java.base/java.util.LinkedList$ListItr.checkForComodification(LinkedList.java:977)
//	at java.base/java.util.LinkedList$ListItr.next(LinkedList.java:899)
//	at benchmark.LockingComparisonLinkedList.readElementsLazyLock(LockingComparisonLinkedList.java:119)
//	at benchmark.LockingComparisonLinkedList.lambda$lazyLockLinkedListBenchmark$1(LockingComparisonLinkedList.java:102)
//	at java.base/java.lang.Thread.run(Thread.java:1583)
// Exception in thread "Thread-299935" java.util.ConcurrentModificationException
//	at java.base/java.util.LinkedList$ListItr.checkForComodification(LinkedList.java:977)
//	at java.base/java.util.LinkedList$ListItr.next(LinkedList.java:899)
//	at benchmark.LockingComparisonLinkedList.readElementsLazyLock(LockingComparisonLinkedList.java:119)
//	at benchmark.LockingComparisonLinkedList.lambda$lazyLockLinkedListBenchmark$1(LockingComparisonLinkedList.java:102)
//	at java.base/java.lang.Thread.run(Thread.java:1583)
// Exception in thread "Thread-299059" Exception in thread "Thread-298556" java.util.ConcurrentModificationException
//	at java.base/java.util.LinkedList$ListItr.checkForComodification(LinkedList.java:977)
//	at java.base/java.util.LinkedList$ListItr.next(LinkedList.java:899)
//	at benchmark.LockingComparisonLinkedList.readElementsLazyLock(LockingComparisonLinkedList.java:119)
//	at benchmark.LockingComparisonLinkedList.lambda$lazyLockLinkedListBenchmark$1(LockingComparisonLinkedList.java:102)
//	at java.base/java.lang.Thread.run(Thread.java:1583)
//
// Benchmark                                                        Mode  Cnt  Score   Error  Units
// LockingComparison.handOverHandLockBenchmark                      avgt    5  0.007 ± 0.009   s/op
// LockingComparison.lazyLockBenchmark                              avgt    5  0.002 ± 0.001   s/op
// LockingComparisonLinkedList.handOverHandLockLinkedListBenchmark  avgt    5  0.007 ± 0.009   s/op
// LockingComparisonLinkedList.lazyLockLinkedListBenchmark          avgt    5  0.007 ± 0.042   s/op
//
// -> Still produces output though...
//
//***********************************************************************************************************************

@State(Scope.Benchmark)
public class LockingComparison {
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
        private final Node head;
        private final ReentrantLock handOverHandLock;
        private final ReentrantLock lazyLock;

        public BenchmarkState() {
            head = new Node();
            handOverHandLock = new ReentrantLock();
            lazyLock = new ReentrantLock();
        }
    }

    //****************************************************************************************************
    // handOverHandLockBenchmark
    //****************************************************************************************************
    // In the handOverHandLockBenchmark method, when a thread wants to read or write, it acquires a lock
    // (handOverHandLock) at the beginning and releases it at the end of its operation. This ensures
    // exclusive access to shared data during both read and write operations, so there's no concurrent
    // access by multiple threads.
    //
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = FORK_VALUE)
    @Warmup(iterations = WARMUP_ITERATIONS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS)
    public void handOverHandLockBenchmark(BenchmarkState state) {
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
        Node newNode = new Node(value);
        state.handOverHandLock.lock();
        try {
            newNode.next = state.head.next;
            state.head.next = newNode;
        } finally {
            state.handOverHandLock.unlock();
        }
    }

    public void readElementsHandOverHand(BenchmarkState state) {
        Node current = state.head.next;
        state.handOverHandLock.lock();
        try {
            while (current != null) {
                int value = current.value;
                // Simulate reading the value
                current = current.next;
            }
        } finally {
            state.handOverHandLock.unlock();
        }
    }

    //****************************************************************************************************
    // lazyLockBenchmark
    //****************************************************************************************************
    // In the lazyLockBenchmark method, when a thread wants to read or write, it acquires a lock (lazyLock)
    // only during the actual write operation. During read operations, it doesn't lock the shared data.
    // This allows multiple threads to read data simultaneously without blocking each other.
    //
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = FORK_VALUE)
    @Warmup(iterations = WARMUP_ITERATIONS)
    @Measurement(iterations = MEASUREMENT_ITERATIONS)
    public void lazyLockBenchmark(BenchmarkState state) {
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
        Node newNode = new Node(value);
        state.lazyLock.lock();
        try {
            newNode.next = state.head.next;
            state.head.next = newNode;
        } finally {
            state.lazyLock.unlock();
        }
    }

    public void readElementsLazyLock(BenchmarkState state) {
        Node current = state.head.next;
        while (current != null) {
            int value = current.value;
            // Simulate reading the value
            current = current.next;
        }
    }

    private static class Node {
        int value;
        Node next;

        Node() {
            this.value = -1; // Head node
        }

        Node(int value) {
            this.value = value;
        }
    }
}
