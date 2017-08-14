package ru.otus.chepiov.db.api;

import ru.otus.chepiov.db.model.User;

/**
 * Database API.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public interface DBService {
    /**
     * Saves user.
     * Sets generated id to it.
     *
     * @param user to save
     */
    void save(final User user);

    /**
     * Loads user by id.
     *
     * @param id to fetch
     * @return Loaded user or null if not found
     */
    User load(final Long id);

    /**
     * Gracefully close database service.
     */
    void close();
}
