package ru.otus.chepiov.l9.test;

import org.junit.Ignore;
import org.junit.Test;
import ru.otus.chepiov.l13.Helper;

import java.io.FileNotFoundException;
import java.sql.SQLException;

/**
 * Manually clearing H2 db. Just for developing purposes.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class ManualClearTest {

    @Test
    @Ignore
    public void clearManually() throws SQLException, FileNotFoundException {
        Helper.clearDb();
    }
}
