package ru.otus.chepiov.l2;

import org.github.jamm.MemoryMeter;
import org.openjdk.jol.info.ClassLayout;

import java.util.function.Supplier;

/**
 * This utility class tries to find the size of objects.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public final class Sizer {

    public static <T> long byJAMM(final Supplier<T> objectSupplier) {
        MemoryMeter meter = new MemoryMeter();
        T object = objectSupplier.get();
        return meter.measureDeep(object);
    }

    /**
     * Finds size by <a href="http://openjdk.java.net/projects/code-tools/jol/">JOL</a>.
     *
     * @param objectSupplier creates new object
     * @param <T>            generic param
     * @return size
     */
    public static <T> long byJOL(Supplier<T> objectSupplier) {
        final ClassLayout classLayout = ClassLayout.parseInstance(objectSupplier.get());
        classLayout.fields();
        return classLayout.instanceSize();
    }

    /**
     * Tries to calculate object size by calculating the diff between memory before and after creation.
     *
     * @param objectSupplier creates new object
     * @param <T>            generic param
     * @return size
     */
    @SuppressWarnings("UnusedAssignment")
    public static <T> long byMemAssumption(Supplier<T> objectSupplier) {
        tryGC();
        long before = usedMemory();
        Object[] ELIMINATOR = new Object[LOOP_CNT];
        for (int i = 0; i < LOOP_CNT; i++) {
            T object = objectSupplier.get();
            ELIMINATOR[i] = object;
            object = null;
        }
        double after = usedMemory();
        ELIMINATOR = null;
        tryGC();
        return Math.round((after - before) / LOOP_CNT);
    }

    private Sizer() {
        throw new AssertionError("Non-instantiable utility class");
    }

    private static final int LOOP_CNT = 100;

    private static long usedMemory() {
        tryGC();
        final long total = Runtime.getRuntime().totalMemory();
        tryGC();
        return total - Runtime.getRuntime().freeMemory();
    }

    private static void tryGC() {
        System.gc();
        sleep();
        System.runFinalization();
        sleep();
        System.gc();
    }

    private static void sleep() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignore) {
            // NOP
        }
    }
}
