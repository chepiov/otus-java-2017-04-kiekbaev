package ru.otus.chepiov.l9.test;

import liquibase.exception.LiquibaseException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.otus.chepiov.db.api.DataSet;
import ru.otus.chepiov.db.api.DBService;
import ru.otus.chepiov.db.model.Address;
import ru.otus.chepiov.db.model.Phone;
import ru.otus.chepiov.db.model.User;
import ru.otus.chepiov.l10.HibernateDBService;
import ru.otus.chepiov.l11.SoftRefCacheEngine;
import ru.otus.chepiov.l13.Helper;
import ru.otus.chepiov.l9.Executor;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

/**
 * L9 and L10 test suite.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
@RunWith(Parameterized.class)
public class OrmTest {

    private Supplier<DBService> dbServiceCreator;
    private DBService service;


    /**
     * Supplier because of firstly liquibase initialization needed.
     *
     * @param dbServiceCreator to use
     */
    public OrmTest(final Supplier<DBService> dbServiceCreator) {
        this.dbServiceCreator = dbServiceCreator;
    }

    @BeforeClass
    public static void beforeClass() throws ClassNotFoundException {
        Helper.loadDriver();
    }

    @Before
    public void before() throws ClassNotFoundException, SQLException, LiquibaseException {
        Helper.prepareDb();
        this.service = dbServiceCreator.get();
    }

    @Test
    public void testSave() throws ClassNotFoundException, SQLException {

        final User ironMan = Helper.createIronMan();

        service.save(ironMan);
        Assert.assertTrue(ironMan.persisted());
        final Connection conn = DriverManager.getConnection(Helper.JDBC_H2_TEST_URL);
        final ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM user WHERE name = 'Tony Stark' AND age = 45");
        int cnt = 0;
        while (rs.next()) {
            cnt++;
        }
        Assert.assertEquals("The only one Iron man!", cnt, 1);

        final ResultSet rs2 = conn.createStatement().executeQuery("SELECT phone.* FROM phone " +
                "INNER JOIN user ON (user.id = phone.user_id) WHERE user.name = 'Tony Stark' AND user.age = 45");
        cnt = 0;
        while (rs2.next()) {
            cnt++;
        }
        Assert.assertEquals("How can we call the Iron man?!", cnt, 1);

        final ResultSet rs3 = conn.createStatement().executeQuery("SELECT address.* FROM address " +
                "INNER JOIN user ON (user.id = address.user_id) WHERE user.name = 'Tony Stark' AND user.age = 45");
        cnt = 0;
        while (rs3.next()) {
            cnt++;
        }
        Assert.assertEquals("How can we find an Iron Man?!", cnt, 1);

        conn.close();
    }

    @Test
    public void testLoad() throws SQLException {
        final User starLord = service.load(1L);
        Assert.assertNotNull("Where is the Star Lord???", starLord);
        Assert.assertTrue(starLord.persisted());
        Assert.assertNotNull(starLord.getAddress());
        Assert.assertNotNull(starLord.getPhones());
        final User batman = service.load(2L);
        Assert.assertNotNull("Where is the Batman???", batman);
        Assert.assertTrue(batman.persisted());
        Assert.assertNotNull(batman.getAddress());
        Assert.assertNotNull(batman.getPhones());
    }

    @After
    public void after() throws SQLException, LiquibaseException {
        service.close();
        Helper.clearTables();
    }

    @AfterClass
    public static void afterClass() throws SQLException, FileNotFoundException {
        Helper.clearDb();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> serviceImplementations() {
        return Arrays.asList(new Object[][]{
                {
                        (Supplier<DBService>) () ->
                                new Executor(
                                        Helper.H2_DRIVER,
                                        Helper.JDBC_H2_TEST_URL,
                                        new HashSet<Class<? extends DataSet>>() {{
                                            add(User.class);
                                            add(Address.class);
                                            add(Phone.class);
                                        }},
                                        10,
                                        new SoftRefCacheEngine<>())

                },
                {
                        (Supplier<DBService>) () ->
                                new HibernateDBService(Helper.H2_DATASOURCE,
                                        Helper.JDBC_H2_TEST_URL,
                                        new HashSet<Class<? extends DataSet>>() {{
                                            add(User.class);
                                            add(Address.class);
                                            add(Phone.class);
                                        }},
                                        10)
                }
        });
    }
}
