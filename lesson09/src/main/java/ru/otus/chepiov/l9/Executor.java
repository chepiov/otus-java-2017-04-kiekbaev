package ru.otus.chepiov.l9;

import ru.otus.chepiov.db.api.DataSet;
import ru.otus.chepiov.db.api.DBService;
import ru.otus.chepiov.db.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Queries executor.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public final class Executor implements DBService<User> {

    private final Map<Class<? extends DataSet>, Meta<?>> metas;

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

        metas = new HashMap<>();
        entities.forEach(e -> Meta.buildAndSaveMeta(e, metas));
    }

    @Override
    public void save(final User dataSet) {

        try (final Connection conn = connSup.get()) {
            conn.setAutoCommit(false);
            try {
                @SuppressWarnings("unchecked") final Meta<User> meta = (Meta<User>) metas.get(dataSet.getClass());
                meta.save(dataSet, conn);
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

    @Override
    public User load(final Long id) {
        try (final Connection conn = connSup.get()) {
            conn.setAutoCommit(false);
            try {
                @SuppressWarnings("unchecked") final Meta<User> meta = (Meta<User>) metas.get(User.class);
                final User result = meta.load(id, conn);
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
