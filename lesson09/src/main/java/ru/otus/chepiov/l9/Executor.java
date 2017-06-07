package ru.otus.chepiov.l9;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Queries executor.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public final class Executor {

    private final Map<Class<? extends DataSet>, Meta> metas;

    private final Supplier<Connection> connSup;

    public Executor(final String driverName,
                    final String jdbcUrl,
                    final Set<Class<? extends DataSet>> entities) {
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        connSup = () -> {
            try {
                return DriverManager.getConnection(jdbcUrl);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };

        metas = entities.stream()
                .map(Meta::new)
                .collect(Collectors.toMap(Meta::getEntityClass, Function.identity()));
    }

    /**
     * Saves dataSet.
     * Sets generated id to it.
     *
     * @param dataSet to save
     */
    public <T extends DataSet> void save(T dataSet) {

        try (Connection conn = connSup.get()) {
            conn.setAutoCommit(false);
            try {
                metas.get(dataSet.getClass()).save(dataSet, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                conn.setAutoCommit(true);
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load dataSet by id.
     *
     * @param id    to fetch
     * @param clazz to map
     * @return Loaded dataSet or null if not found
     */
    public <T extends DataSet> T load(final Long id, final Class<T> clazz) {
        try (Connection conn = connSup.get()) {
            conn.setAutoCommit(false);
            try {
                final T result = metas.get(clazz).load(id, conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                conn.setAutoCommit(true);
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
