package ru.otus.chepiov.tf;

import static ru.otus.chepiov.tf.Assert.assertNotNull;
import static ru.otus.chepiov.tf.Assert.assertTrue;

import org.junit.Test;

/**
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class AssertTest {

    @Test(expected = AssertionError.class)
    public void testAssertTrue() {
        assertTrue(true, "OMG!");
        assertTrue(true);

        assertTrue(false, "Yes, false is false, not true");
    }

    @Test(expected = AssertionError.class)
    public void testAssertFalse() {
        assertTrue(false, "OMG!");
        assertTrue(false);

        assertTrue(true, "Yes, true is true, not false");
    }

    @Test(expected = AssertionError.class)
    public void testAssertNotNull() {
        assertNotNull("", "WHAAAT?");
        assertNotNull("");

        assertNotNull(null, "Hmmm...");
    }

}
