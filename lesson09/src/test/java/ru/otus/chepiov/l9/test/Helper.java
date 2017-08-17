package ru.otus.chepiov.l9.test;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import org.h2.tools.RunScript;
import ru.otus.chepiov.db.api.DBService;
import ru.otus.chepiov.db.model.Address;
import ru.otus.chepiov.db.model.Phone;
import ru.otus.chepiov.db.model.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Helper utility for DB.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
class Helper {

    static final String JDBC_H2_TEST_URL = "jdbc:h2:~/test";
    static final String H2_DRIVER = "org.h2.Driver";
    static final String H2_DATASOURCE = "org.h2.jdbcx.JdbcDataSource";

    private Helper() {
        throw new AssertionError("Non-instantiable");
    }

    static void loadDriver() throws ClassNotFoundException {
        Class.forName(H2_DRIVER);
    }


    static void prepareDb() throws LiquibaseException, SQLException {
        Connection conn = DriverManager.getConnection(JDBC_H2_TEST_URL);
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(conn));
        final Liquibase liquibase = new Liquibase("src/test/resources/lb-create.changelog.yaml",
                new FileSystemResourceAccessor(), database);
        liquibase.clearCheckSums();
        liquibase.update("test");
        conn.close();
    }

    static void clearTables() throws SQLException, LiquibaseException {
        Connection conn = DriverManager.getConnection(JDBC_H2_TEST_URL);
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(conn));
        final Liquibase liquibase = new Liquibase("src/test/resources/lb-clear.changelog.yaml",
                new FileSystemResourceAccessor(), database);
        liquibase.update("test");
        liquibase.clearCheckSums();
        conn.close();
    }

    static void clearDb() throws SQLException, FileNotFoundException {
        Connection conn = DriverManager.getConnection(JDBC_H2_TEST_URL);
        @SuppressWarnings("ConstantConditions")
        File script = new File(OrmTest.class.getClassLoader().getResource("clear.h2.sql").getFile());
        RunScript.execute(conn, new FileReader(script));
    }

    static User createIronMan() {
        final User ironMan = new User();
        ironMan.setName("Tony Stark");
        ironMan.setAge(45);
        final Address ironAddress = new Address();
        ironAddress.setStreet("Iron street");
        ironAddress.setIndex(423511);
        ironMan.setAddress(ironAddress);
        final List<Phone> ironPhones = new ArrayList<Phone>() {{
            final Phone ironPhone = new Phone();
            ironPhone.setCode(7888);
            ironPhone.setNumber("8985547525");
            add(ironPhone);
        }};
        ironMan.setPhones(ironPhones);
        return ironMan;
    }

    static void runAwait(final Supplier<DBService> serviceSup) throws InterruptedException, ClassNotFoundException, SQLException, LiquibaseException, IOException {

        System.out.println(ManagementFactory.getRuntimeMXBean().getName());

        Helper.loadDriver();
        Helper.prepareDb();

        final User ironMan = createIronMan();

        final DBService service = serviceSup.get();
        service.save(ironMan);

        final ExecutorService es = Executors.newFixedThreadPool(5);
        final List<Future<User>> results = es.invokeAll(new ArrayList<Callable<User>>() {{
            add(() -> service.load(ironMan.getId()));
            add(() -> service.load(ironMan.getId()));
            add(() -> service.load(ironMan.getId()));
            add(() -> service.load(ironMan.getId()));
        }});
        es.awaitTermination(10, TimeUnit.SECONDS);


        results.forEach(u -> System.out.println(u.isDone()));

        //noinspection ResultOfMethodCallIgnored
        System.in.read();

        System.out.println("BYE");

        service.close();
        es.shutdown();
        clearTables();
        clearDb();
    }
}
