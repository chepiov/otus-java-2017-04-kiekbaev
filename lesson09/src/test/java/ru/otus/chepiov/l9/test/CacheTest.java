package ru.otus.chepiov.l9.test;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.chepiov.l11.CacheEngine;
import ru.otus.chepiov.l11.SoftRefCacheEngine;

import java.util.Objects;

/**
 * Cache engine test suite.
 * <p>
 * -Xmx256m -Xms256m.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class CacheTest {

    private static final int TOTAL_ELEMENTS = 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheTest.class);

    @Test
    public void test() throws InterruptedException {

        final CacheEngine<Integer, BigObject> cache = new SoftRefCacheEngine<>();

        for (int i = 0; i < TOTAL_ELEMENTS; i++) {
            cache.put(i, new BigObject(i));
        }

        for (int i = 0; i < TOTAL_ELEMENTS; i++) {
            final BigObject element = cache.get(i);
            if (Objects.nonNull(element)) {
                LOGGER.trace("Id: {}", element.id);
            }

        }

        LOGGER.debug("Cache hits: {}", cache.getHitCount());
        LOGGER.debug("Cache misses: {}", cache.getMissCount());

        System.gc();
        for (int i = 0; i < TOTAL_ELEMENTS; i++) {
            final BigObject element = cache.get(i);
            if (Objects.nonNull(element)) {
                LOGGER.trace("Id: {}", element.id);
            }
        }

        LOGGER.debug("Cache hits: {}", cache.getHitCount());
        LOGGER.debug("Cache misses: {}", cache.getMissCount());

        Assert.assertNotEquals(cache.getHitCount(), TOTAL_ELEMENTS);
        Assert.assertNotEquals(cache.getMissCount(), TOTAL_ELEMENTS);

        cache.dispose();

    }

    static class BigObject {

        final byte[] array = new byte[1024 * 1024];
        final int id;

        BigObject(final int id) {
            this.id = id;
        }

        @SuppressWarnings("unused")
        public byte[] getArray() {
            return array;
        }
    }
}
