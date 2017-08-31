package ru.otus.chepiov.l14;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@SuppressWarnings("WeakerAccess")
public class ParallelOddEven {


    private static final int N_THREADS = 4;

    /**
     * Creates copy of defined array and sorts it.
     *
     * @param inArray to sort
     * @return new sorted array with elements of defined array
     */
    public static int[] sort(final int[] inArray) {

        final int[] array = new int[inArray.length];

        // simply sort in same thread
        if (inArray.length < 4) {
            Arrays.sort(array);
            return array;
        }

        System.arraycopy(inArray, 0, array, 0, inArray.length);

        // unbounded thread pool executor
        final ExecutorService service = Executors.newFixedThreadPool(N_THREADS);

        IntStream.iterate(0, i -> ++i).limit(array.length).forEach(i -> {

            // `Compare and sort` jobs of current iteration.
            // It's OKAY to use non-threadsafe queue because all threads manipulates different elements.
            // Next iteration will see changes because of using CountDownLatch.
            final Queue<Integer> jobs = new ArrayDeque<>();
            for (int j = (i % 2 == 0) ? 1 : 0; j < array.length - 1; j += 2) {
                jobs.add(j);
            }

            // latch for current iteration.
            final CountDownLatch latch = new CountDownLatch(jobs.size());
            jobs.forEach(j -> service.execute(
                    () -> {
                        if (array[j] > array[j + 1]) {
                            int tmp = array[j];
                            array[j] = array[j + 1];
                            array[j + 1] = tmp;
                        }
                        latch.countDown();
                    }));

            // awaiting ending this iteration and creating happens-before relationship for next iteration
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        service.shutdown();
        return array;
    }
}
