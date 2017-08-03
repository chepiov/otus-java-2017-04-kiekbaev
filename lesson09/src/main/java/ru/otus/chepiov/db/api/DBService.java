package ru.otus.chepiov.db.api;

/**
 * Database service.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public interface DBService<T extends DataSet> {

    /**
     * Saves dataSet.
     * Sets generated id to it.
     *
     * @param dataSet to save
     */
    void save(final T dataSet);

    /**
     * Load dataSet by id.
     *
     * @param id to fetch
     * @return Loaded dataSet or null if not found
     */
    T load(final Long id);

    /**
     * Gracefully closing service.
     */
    void close();

}
