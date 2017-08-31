package ru.otus.chepiov.l9.test;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import org.junit.*;
import ru.otus.chepiov.l9.DataSet;
import ru.otus.chepiov.l9.Executor;
import ru.otus.chepiov.l9.entity.User;

import java.sql.*;
import java.util.HashSet;

/**
 * Test suite.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class OrmTest {

    private static final String JDBC_H2_TEST_URL = "jdbc:h2:~/test";
    private static final String H2_DRIVER = "org.h2.Driver";

    private Liquibase liquibase;
    private Executor executor;

    @BeforeClass
    public static void beforeClass() throws ClassNotFoundException {
        Class.forName(H2_DRIVER);
    }

    @Before
    public void before() throws ClassNotFoundException, SQLException, LiquibaseException {
        Connection conn = DriverManager.getConnection(JDBC_H2_TEST_URL);
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(conn));
        liquibase = new Liquibase("src/test/resources/lb-create.changelog.yaml",
                new FileSystemResourceAccessor(), database);
        liquibase.clearCheckSums();
        liquibase.update("test");
        conn.close();
        executor = new Executor(H2_DRIVER, JDBC_H2_TEST_URL, new HashSet<Class<? extends DataSet>>() {{
            add(User.class);
        }});
    }

    @Test
    public void testSave() throws ClassNotFoundException, SQLException {
        final User ironMan = new User();
        ironMan.setName("Tony Stark");
        ironMan.setAge(45);
        executor.save(ironMan);
        Assert.assertTrue(ironMan.persisted());
        final Connection conn = DriverManager.getConnection(JDBC_H2_TEST_URL);
        final ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM user WHERE name = 'Tony Stark' AND age = 45");
        int cnt = 0;
        while (rs.next()) {
            cnt++;
        }
        Assert.assertEquals("The only one Iron man!", cnt, 1);
    }

    @Test
    public void testLoad() {
        final User starLord = executor.load(1L, User.class);
        Assert.assertNotNull("Where is the Star Lord???", starLord);
        Assert.assertTrue(starLord.persisted());
        final User batman = executor.load(2L, User.class);
        Assert.assertNotNull("Where is the Batman???", batman);
        Assert.assertTrue(batman.persisted());
    }

    @After
    public void after() throws SQLException, LiquibaseException {
        Connection conn = DriverManager.getConnection(JDBC_H2_TEST_URL);
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(conn));
        liquibase = new Liquibase("src/test/resources/lb-clear.changelog.yaml",
                new FileSystemResourceAccessor(), database);
        liquibase.update("test");
        liquibase.clearCheckSums();
        conn.close();
    }
}
