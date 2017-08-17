package ru.otus.chepiov.l11;

/**
 * Cache engine.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public interface CacheEngine<K, V> {

    /**
     * Adds element to cache.
     * If element already existed it will be replaced.
     *
     * @param key   to add
     * @param value to add
     */
    void put(K key, V value);

    /**
     * Returns value by key
     *
     * @param key of value
     * @return value by key or null if not exist
     */
    V get(K key);

    /**
     * Returns total hit count.
     *
     * @return hit count
     */
    int getHitCount();

    /**
     * Returns total miss count.
     *
     * @return miss count
     */
    int getMissCount();

    /**
     * Releases all resources and clears cache.
     * Note that accordingly thread-safety this method will not discard statistic counters.
     */
    void dispose();

}
