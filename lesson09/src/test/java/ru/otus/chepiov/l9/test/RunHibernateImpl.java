package ru.otus.chepiov.l9.test;

import liquibase.exception.LiquibaseException;
import ru.otus.chepiov.db.api.DataSet;
import ru.otus.chepiov.db.model.Address;
import ru.otus.chepiov.db.model.Phone;
import ru.otus.chepiov.db.model.User;
import ru.otus.chepiov.l10.HibernateDBService;
import ru.otus.chepiov.l13.Helper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Simple runner for hibernate + ehcache.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class RunHibernateImpl {

    @SuppressWarnings("UnusedAssignment")
    public static void main(String[] args) throws SQLException, LiquibaseException, ClassNotFoundException, IOException, InterruptedException {

        Helper.runAwait(() -> new HibernateDBService(Helper.H2_DATASOURCE,
                Helper.JDBC_H2_TEST_URL,
                new HashSet<Class<? extends DataSet>>() {{
                    add(User.class);
                    add(Address.class);
                    add(Phone.class);
                }},
                10));
    }
}
