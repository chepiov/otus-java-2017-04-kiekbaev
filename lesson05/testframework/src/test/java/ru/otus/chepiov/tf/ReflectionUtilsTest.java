package ru.otus.chepiov.tf;

import ru.otus.chepiov.tf.code.RunnerTestCode;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

/**
 * Reflection utilities test suite.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class ReflectionUtilsTest {

    @Test
    public void findClassesTest() throws IOException, ClassNotFoundException {
        final Class[] expected = new Class[]{RunnerTestCode.class};

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final Class<?>[] actual = ReflectionUtils.findAllAnnotatedClasses("ru.otus.chepiov.tf", loader);

        assertThat(actual, equalTo(expected));
    }
}
