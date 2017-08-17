package ru.otus.chepiov.l10;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import ru.otus.chepiov.db.api.DataSet;
import ru.otus.chepiov.db.api.DBService;
import ru.otus.chepiov.db.model.User;
import ru.otus.chepiov.l10.dao.UserDAO;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Hibernate Database service implementation.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class HibernateDBService implements DBService {

    private final SessionFactory sessionFactory;

    public HibernateDBService(final String dataSource,
                              final String jdbcUrl,
                              final Set<Class<? extends DataSet>> entities,
                              final int poolSize) {

        final Configuration configuration = new Configuration();
        entities.forEach(configuration::addAnnotatedClass);
        hibernateConfiguration(configuration);
        poolConfiguration(dataSource, jdbcUrl, poolSize, configuration);
        cacheConfiguration(configuration);
        sessionFactory = createSessionFactory(configuration);

        registerCacheMBean();
    }

    @Override
    public void save(final User dataSet) {
        localTx(session -> {
            final UserDAO dao = new UserDAO(session);
            dao.save(dataSet);
        });
    }

    @Override
    public User load(final Long id) {
        return localTx(session -> {
            final UserDAO dao = new UserDAO(session);
            return dao.load(id);
        });
    }

    @Override
    public void close() {
        sessionFactory.close();
    }

    private void hibernateConfiguration(Configuration configuration) {
        configuration.setProperty(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
        configuration.setProperty(Environment.SHOW_SQL, "true");
        configuration.setProperty(Environment.FORMAT_SQL, "true");
        configuration.setProperty(Environment.HBM2DDL_AUTO, "validate");
        configuration.setProperty(Environment.ENABLE_LAZY_LOAD_NO_TRANS, "true");
        configuration.setProperty(Environment.GENERATE_STATISTICS, "true");
    }

    private void poolConfiguration(String dataSource, String jdbcUrl, int poolSize, Configuration configuration) {
        configuration.setProperty(Environment.CONNECTION_PROVIDER, "com.zaxxer.hikari.hibernate.HikariConnectionProvider");
        configuration.setProperty("hibernate.hikari.maximumPoolSize", Integer.toString(poolSize));
        configuration.setProperty("hibernate.hikari.dataSourceClassName", dataSource);
        configuration.setProperty("hibernate.hikari.dataSource.url", jdbcUrl);
    }

    private void cacheConfiguration(Configuration configuration) {
        configuration.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "true");
        configuration.setProperty(Environment.USE_QUERY_CACHE, "true");
        configuration.setProperty(Environment.CACHE_REGION_FACTORY, "org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory");
        configuration.setProperty("net.sf.ehcache.configurationResourceName", "ehcache.xml");
    }

    private void registerCacheMBean() {
        CacheManager manager = CacheManager.getCacheManager(CacheManager.DEFAULT_NAME);
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ManagementService.registerMBeans(manager, mBeanServer, false, false, true, true);
    }

    private static SessionFactory createSessionFactory(Configuration configuration) {
        final StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.applySettings(configuration.getProperties());
        final ServiceRegistry serviceRegistry = builder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }

    private <T> T localTx(final Function<Session, T> function) {
        try (final Session session
                     = sessionFactory.withOptions().interceptor(new UserSaveInterceptor()).openSession()) {

            final Transaction transaction = session.beginTransaction();
            final T result = function.apply(session);
            transaction.commit();
            return result;
        }
    }

    private void localTx(final Consumer<Session> function) {
        try (final Session session
                     = sessionFactory.withOptions().interceptor(new UserSaveInterceptor()).openSession()) {
            final Transaction transaction = session.beginTransaction();
            function.accept(session);
            transaction.commit();
        }
    }
}
