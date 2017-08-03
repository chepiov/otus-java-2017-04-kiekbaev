package ru.otus.chepiov.l9.test;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import org.h2.tools.RunScript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
}
