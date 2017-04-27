package ru.otus.chepiov;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Simple memory leaking objects producer.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
class GarbageProducer {
    private final Random random = new Random(System.currentTimeMillis());
    private final static List<Object> GLOBAL_OBJECTS = new ArrayList<>();

    void produce() {

        IntStream.iterate(1, i -> ++i).forEach(i -> {
            localProduce();
            GLOBAL_OBJECTS.add(new Object());
            if (i % 300 == 0) {
                GLOBAL_OBJECTS.remove(random.nextInt(GLOBAL_OBJECTS.size()));
            }
        });
    }

    private void localProduce() {
        final List<Object> localObjects = new ArrayList<>();
        IntStream.iterate(1, i -> ++i).limit(10000).forEach(i -> {
            localObjects.add(new Object());
            if (i % 5 == 0) {
                localObjects.remove(random.nextInt(localObjects.size()));
            }
            if (i % 100 == 0) {
                GLOBAL_OBJECTS.add(new Object());
            }
        });
    }
}
