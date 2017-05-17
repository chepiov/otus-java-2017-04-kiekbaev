package ru.otus.chepiov.tf;

import java.util.Objects;

/**
 * Asserts for testing.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public final class Assert {

    private Assert() {
        throw new AssertionError("Non-instantiable");
    }

    /**
     * Checks that condition is false.
     *
     * @param condition to check
     * @param message   used to describe test error
     */
    public static void assertFalse(final boolean condition, final String message) {
        if (condition) {
            fail(message);
        }
    }

    /**
     * Checks that condition is false.
     *
     * @param condition to check
     */
    public static void assertFalse(final boolean condition) {
        assertFalse(condition, null);
    }

    /**
     * Checks that condition is true.
     *
     * @param condition to check
     * @param message   used to describe test error
     */
    public static void assertTrue(final boolean condition, final String message) {
        if (!condition) {
            fail(message);
        }
    }

    /**
     * Checks that condition is true.
     *
     * @param condition to check
     */
    public static void assertTrue(final boolean condition) {
        assertTrue(condition, null);
    }

    /**
     * Checks that defined object is not null .
     *
     * @param object  to check
     * @param message used to describe test error
     */
    public static void assertNotNull(final Object object, final String message) {
        if (Objects.isNull(object)) {
            fail(message);
        }
    }

    /**
     * Checks that defined object is not null .
     *
     * @param object to check
     */
    public static void assertNotNull(final Object object) {
        assertNotNull(object, null);
    }

    private static void fail(final String message) {
        if (Objects.isNull(message)) {
            throw new AssertionError();
        } else {
            throw new AssertionError(message);
        }
    }
}
