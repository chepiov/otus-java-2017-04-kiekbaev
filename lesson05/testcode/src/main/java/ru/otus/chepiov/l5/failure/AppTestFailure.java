package ru.otus.chepiov.l5.failure;

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
public class AppTestFailure {

    private Object testObject;
    private Object nullObject;
    private int correctMainAnswer;
    private int incorrectMainAnswer;

    @Before
    public void before() {
        testObject = new Object();
        correctMainAnswer = 42;
        incorrectMainAnswer = 43;
    }

    @Test
    public void testFailedFromAssertTrue() {
        assertTrue(incorrectMainAnswer == 42, "The Ultimate Question of Life, the Universe, and Everything must be 42");
    }

    @Test
    public void testFailedFromAssertFalse() {
        assertFalse(correctMainAnswer == 42, "Something wrong! What the hell???");
    }

    @Test
    public void testFailedFromAssertNotNull() {
        assertNotNull(nullObject, "AGHHHH!");
    }

    @After
    public void after() {
        testObject = nullObject;
    }
}
