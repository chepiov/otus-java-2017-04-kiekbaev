package ru.otus.chepiov.l5.success;

import ru.otus.chepiov.tf.After;
import ru.otus.chepiov.tf.Before;
import ru.otus.chepiov.tf.Test;

import static ru.otus.chepiov.tf.Assert.*;

/**
 * Test suite.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
// Because of vanilla tests and unknown annotations for Idea - simply suppress inspections
@SuppressWarnings({"ConstantConditions", "unused"})
public class AppTestSuccess {

    private Object testObject;
    private Object nullObject;

    @Before
    public void before() {
        testObject = new Object();
    }

    @Test
    public void testPassed() {

        assertTrue(1 + 1 == 2);
        assertTrue(1 + 1 == 2, "WHAAAT?");

        assertFalse(3 + 2 == 6);
        assertFalse(3 + 2 == 6, "Again... WHAAAT?");

        assertNotNull(testObject);
        assertNotNull(testObject, "Are you kidding me?");
    }

    @After
    public void after() {
        testObject = nullObject;
    }
}
