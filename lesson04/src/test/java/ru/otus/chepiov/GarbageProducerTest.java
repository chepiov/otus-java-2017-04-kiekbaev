package ru.otus.chepiov;

import com.sun.management.GarbageCollectionNotificationInfo;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Statistic collector.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GarbageProducerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GarbageProducerTest.class);

    private static final Map<String, Statistic> STATISTICS = new HashMap<>();

    /**
     * It's okay to expecting OOM - this is what we want.
     */
    @Test(expected = java.lang.OutOfMemoryError.class)
    public void aPerform() {

        LOGGER.info("Current PID: {}", ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        final TimeSplitter splitter = new TimeSplitter(System.nanoTime());

        ManagementFactory.getGarbageCollectorMXBeans().stream()
                .filter(bean -> bean instanceof NotificationEmitter)
                .map(bean -> (NotificationEmitter) bean)
                .forEach(emitter -> {
                    final NotificationListener listener = (notification, handback) -> {
                        if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                            GarbageCollectionNotificationInfo info =
                                    GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());


                            final String name = info.getGcName();
                            final long duration = info.getGcInfo().getDuration();
                            final String type = info.getGcAction();
                            final String cause = info.getGcCause();
                            final long elapsedSeconds
                                    = TimeUnit.SECONDS.convert(System.nanoTime() - splitter.from, TimeUnit.NANOSECONDS);

                            STATISTICS.putIfAbsent(name, new Statistic());
                            Statistic stat = STATISTICS.get(name);

                            final long durationPerMinute;
                            final List<Long> durationsPerMinute = new ArrayList<>(stat.durationsPerMinute);
                            if (elapsedSeconds > 60) {
                                splitter.from = System.nanoTime();
                                durationPerMinute = 0;
                                durationsPerMinute.add(stat.currentDurationPerMinute);

                            } else {
                                durationPerMinute = stat.currentDurationPerMinute;
                            }
                            final long currentDurationPerMinute = durationPerMinute + duration;

                            STATISTICS.put(name, new Statistic(
                                    stat.count + 1,
                                    stat.totalDuration + duration,
                                    currentDurationPerMinute,
                                    durationsPerMinute));

                            LOGGER.debug("name: {}, " +
                                            "total count: {}, " +
                                            "duration: {} ms, " +
                                            "total duration per last minute: {} ms, " +
                                            "type: {}, cause: {}, " +
                                            "free mem: {} bytes",
                                    new Object[]{name,
                                            STATISTICS.get(name).count,
                                            duration,
                                            currentDurationPerMinute,
                                            type,
                                            cause,
                                            Runtime.getRuntime().freeMemory()});
                        }
                    };
                    emitter.addNotificationListener(listener, null, null);
                });
        new GarbageProducer().produce();
    }

    @Test
    public void bShowStatistic() throws IOException {
        STATISTICS.forEach((type, stat) -> {
            LOGGER.info("RESULTS:");
            LOGGER.info("   TYPE: {}, COUNT: {}, TOTAL DURATION: {} ms, AVERAGE DURATION PER MINUTE: {} ms",
                    new Object[]{
                            type,
                            stat.count,
                            stat.totalDuration,
                            stat.durationsPerMinute.stream().reduce(0L, (a, b) -> a + b) / stat.durationsPerMinute.size()
                    }
            );
        });
    }

    @Test
    public void cClear() {
        Arrays.asList("dumps", "logs").forEach(dir -> {
            try {
                //noinspection ResultOfMethodCallIgnored
                Files.walk(Paths.get(dir), 1)
                        .map(Path::toFile)
                        .filter(f -> f.isFile() && !f.isHidden())
                        .forEach(File::delete);
            } catch (IOException ignore) {
                LOGGER.error("Error during cleaning", ignore);
            }
        });
    }

    private static final class Statistic {
        final long count;
        final long totalDuration;
        final long currentDurationPerMinute;
        final List<Long> durationsPerMinute;

        Statistic() {
            this(0, 0, 0, new ArrayList<>(Collections.singletonList(0L)));
        }

        Statistic(final long count, final long totalDuration, final long currentDurationPerMinute, final List<Long> durationsPerMinute) {
            this.count = count;
            this.totalDuration = totalDuration;
            this.currentDurationPerMinute = currentDurationPerMinute;
            this.durationsPerMinute = durationsPerMinute;
        }
    }

    private static final class TimeSplitter {
        private long from;

        TimeSplitter(final long from) {
            this.from = from;
        }
    }
}
