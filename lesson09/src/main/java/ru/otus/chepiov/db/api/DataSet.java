package ru.otus.chepiov.db.api;

import java.util.Objects;

/**
 * DataSet for all entities with long identifier.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public interface DataSet {

    /**
     * Returns dataset id.
     *
     * @return id
     */
    Long getId();

    /**
     * Sets id
     */
    void setId(final Long id);

    /**
     * Persisted or not.
     *
     * @return persistence status
     */
    default boolean persisted() {
        return Objects.nonNull(getId());
    }
}
