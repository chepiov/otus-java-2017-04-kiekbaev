package ru.otus.chepiov.l9;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.chepiov.db.api.DataSet;
import ru.otus.chepiov.db.api.DBService;
import ru.otus.chepiov.db.model.User;
import ru.otus.chepiov.l11.CacheEngine;
import ru.otus.chepiov.l11.SoftRefCacheEngine;

import javax.management.*;
import java.lang.management.ManagementFactory;
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
public final class Executor implements DBService {

    private static final int MAX_WAITING = 5;

    private final Map<Class<? extends DataSet>, Meta<?>> metas;

    private final Supplier<Connection> connSup;

    private final BlockingQueue<Connection> pool;

    private final AtomicBoolean on = new AtomicBoolean(true);

    private final CacheEngine<Long, User> cache = new SoftRefCacheEngine<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(Executor.class);

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
            final Connection connProxy = (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class[]{Connection.class},
                    new VoidWithoutArgsHandler<>(
                            target,
                            on,
                            (conn) -> {
                                LOGGER.debug("Returning to pool");
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
                return pool.poll(MAX_WAITING, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        metas = new HashMap<>();
        entities.forEach(e -> Meta.buildAndSaveMeta(e, metas));

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name;
        try {
            name = new ObjectName("ru.otus.chepiov:type=SoftRefCacheEngine");
            if(mbs.isRegistered(name)){
                mbs.unregisterMBean(name);
            }
            mbs.registerMBean(cache, name);
        } catch (MalformedObjectNameException
                | NotCompliantMBeanException
                | InstanceAlreadyExistsException
                | InstanceNotFoundException
                | MBeanRegistrationException e) {
            LOGGER.error("Can't expose MBean for cache", e);
        }

    }

    @Override
    public void save(final User dataSet) {

        try (final Connection conn = connSup.get()) {
            conn.setAutoCommit(false);
            try {
                @SuppressWarnings("unchecked") final Meta<User> meta = (Meta<User>) metas.get(dataSet.getClass());
                meta.save(dataSet, conn);
                conn.commit();
                cache.put(dataSet.getId(), dataSet);
            } catch (Exception e) {
                conn.rollback();
                conn.setAutoCommit(true);
                cache.dispose();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User load(final Long id) {
        final User cached = cache.get(id);
        if (Objects.nonNull(cached)) {
            return cached;
        }
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
