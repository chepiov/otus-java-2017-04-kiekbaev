package ru.otus.chepiov.l10;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import ru.otus.chepiov.db.api.DataSet;
import ru.otus.chepiov.db.api.DBService;
import ru.otus.chepiov.db.model.User;
import ru.otus.chepiov.l10.dao.UserDAO;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Hibernate Database service implementation.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class HibernateDBService implements DBService<User> {

    private final SessionFactory sessionFactory;

    public HibernateDBService(final String driverName,
                              final String jdbcUrl,
                              final Set<Class<? extends DataSet>> entities) {

        final Configuration configuration = new Configuration();

        entities.forEach(configuration::addAnnotatedClass);

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        configuration.setProperty("hibernate.connection.driver_class", driverName);
        configuration.setProperty("hibernate.connection.url", jdbcUrl);
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.hbm2ddl.auto", "validate");
        configuration.setProperty("hibernate.connection.useSSL", "false");
        configuration.setProperty("hibernate.enable_lazy_load_no_trans", "true");

        sessionFactory = createSessionFactory(configuration);
    }

    @Override
    public void save(final User dataSet) {
        localTx(session -> {
            final UserDAO dao = new UserDAO(session);
            dao.saveUser(dataSet);
        });
    }

    @Override
    public User load(final Long id) {
        return localTx(session -> {
            final UserDAO dao = new UserDAO(session);
            return dao.getUser(id);
        });
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

            Transaction transaction = session.beginTransaction();
            T result = function.apply(session);
            transaction.commit();
            return result;
        }
    }

    private void localTx(final Consumer<Session> function) {
        try (final Session session
                     = sessionFactory.withOptions().interceptor(new UserSaveInterceptor()).openSession()) {
            Transaction transaction = session.beginTransaction();
            function.accept(session);
            transaction.commit();
        }
    }
}
