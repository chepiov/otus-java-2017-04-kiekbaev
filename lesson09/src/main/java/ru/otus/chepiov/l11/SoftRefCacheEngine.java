package ru.otus.chepiov.l11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cache engine implementation backed by soft references map.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class SoftRefCacheEngine<K, V> implements CacheEngine<K, V>, SoftRefCacheEngineMBean {

    private final Map<K, SoftReference<V>> backed = new ConcurrentHashMap<>();
    private final AtomicInteger hitCount = new AtomicInteger(0);
    private final AtomicInteger missCount = new AtomicInteger(0);
    private final ReferenceQueue<V> referenceQueue = new ReferenceQueue<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(SoftRefCacheEngine.class);

    @Override
    public void put(final K key, final V value) {
        final SoftReference<V> oldValue = backed.put(key, new SoftReference<>(value, referenceQueue));
        if (Objects.nonNull(oldValue)) {
            LOGGER.trace("Releasing old reference");
            oldValue.clear();
            oldValue.enqueue();
        }
    }

    @Override
    public V get(final K key) {
        final SoftReference<V> ref = backed.get(key);
        final V value;
        if (Objects.isNull(ref)) {
            value = null;
        } else {
            value = ref.get();
        }
        if (Objects.isNull(value)) {
            missCount.incrementAndGet();
        } else {
            hitCount.incrementAndGet();
        }
        LOGGER.trace("Hit count: {}, Miss count: {}", getHitCount(), getMissCount());
        return value;
    }

    @Override
    public int getHitCount() {
        return this.hitCount.get();
    }

    @Override
    public int getMissCount() {
        return this.missCount.get();
    }

    @Override
    public void dispose() {
        this.backed.clear();
    }
}
