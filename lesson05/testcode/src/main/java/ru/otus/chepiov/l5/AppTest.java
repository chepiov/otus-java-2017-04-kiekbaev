package ru.otus.chepiov.l5;

import ru.otus.chepiov.tf.Test;

import static ru.otus.chepiov.tf.Assert.assertTrue;

/**
 * Test suite.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
@SuppressWarnings("unused")
public class AppTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void dumpSuccess() {
        assertTrue(1 + 1 == 2);
    }

}
