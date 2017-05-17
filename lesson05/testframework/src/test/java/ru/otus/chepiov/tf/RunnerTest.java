package ru.otus.chepiov.tf;

import ru.otus.chepiov.tf.code.RunnerTestCode;
import org.junit.Test;

/**
 * Integration runner test suite. Nothing to show, just calling methods.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class RunnerTest {

    @Test
    public void runByClasses() {
        Runner.run(new Class[]{RunnerTestCode.class});
    }

    @Test
    public void runByPackage() {
        Runner.run("ru.otus.chepiov.tf.code");
    }
}