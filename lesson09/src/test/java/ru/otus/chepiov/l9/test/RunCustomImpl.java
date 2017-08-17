package ru.otus.chepiov.l9.test;

import liquibase.exception.LiquibaseException;
import ru.otus.chepiov.db.api.DBService;
import ru.otus.chepiov.db.api.DataSet;
import ru.otus.chepiov.db.model.Address;
import ru.otus.chepiov.db.model.Phone;
import ru.otus.chepiov.db.model.User;
import ru.otus.chepiov.l9.Executor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Simple runner for custom orm + custom cache.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class RunCustomImpl {

    public static void main(String[] args) throws ClassNotFoundException, SQLException, LiquibaseException, IOException, InterruptedException {

        final DBService service = new Executor(
                Helper.H2_DRIVER,
                Helper.JDBC_H2_TEST_URL,
                new HashSet<Class<? extends DataSet>>() {{
                    add(User.class);
                    add(Address.class);
                    add(Phone.class);
                }},
                10);
        Helper.runAwait(() -> service);
    }
}
