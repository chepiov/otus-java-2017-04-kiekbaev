package ru.otus.chepiov.l9;

import ru.otus.chepiov.db.api.DBService;
import ru.otus.chepiov.db.api.DataSet;
import ru.otus.chepiov.db.model.User;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Queries executor with simple connections pool.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public final class Executor implements DBService<User> {

    private final Map<Class<? extends DataSet>, Meta<?>> metas;

    private final Supplier<Connection> connSup;

    private final BlockingQueue<Connection> pool;

    private volatile AtomicBoolean on = new AtomicBoolean(true);

    public Executor(final String driverName,
                    final String jdbcUrl,
                    final Set<Class<? extends DataSet>> entities,
                    final int poolSize) {
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        pool = new ArrayBlockingQueue<>(poolSize);

        IntStream.iterate(0, i -> ++i).limit(poolSize).forEach(i -> {
            final Connection target;
            try {
                target = DriverManager.getConnection(jdbcUrl);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            Connection connProxy = (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class[]{Connection.class},
                    new VoidWithoutArgsHandler<>(
                            target,
                            on,
                            (conn) -> {
                                System.out.println("Returning to pool");
                                try {
                                    conn.rollback();
                                } catch (SQLException ignore) {
                                    // NOP
                                }
                                pool.add(conn);
                            },
                            "close"));
            pool.add(connProxy);
        });

        connSup = () -> {
            try {
                return pool.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
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

    @Override
    public void close() {
        this.on.set(false);
        this.pool.forEach(c -> {
            try {
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private static class VoidWithoutArgsHandler<T> implements InvocationHandler {

        private T target;
        private final AtomicBoolean on;
        private final Consumer<T> hook;
        private final String methodName;

        VoidWithoutArgsHandler(final T target,
                               final AtomicBoolean on,
                               final Consumer<T> hook,
                               final String methodName) {
            this.target = target;
            this.on = on;
            this.hook = hook;
            this.methodName = methodName;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (Objects.isNull(args)
                    && on.get()
                    && method.getName().equals(methodName)
                    && method.getReturnType().equals(Void.TYPE)) {
                hook.accept(target);
                return null;
            } else return method.invoke(target, args);

        }
    }
}
