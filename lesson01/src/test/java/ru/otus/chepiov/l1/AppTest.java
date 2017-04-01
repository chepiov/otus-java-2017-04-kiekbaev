package ru.otus.chepiov.l1;

import au.com.bytecode.opencsv.CSVReader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Test case for App.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class AppTest {
    private static final Path SRC = Paths.get("src/test/resources/a.csv");
    private static final Path TARGET = Paths.get("src/test/resources/a.tsv");

    @Before
    @After
    public void before() throws IOException {
        Files.deleteIfExists(TARGET);
    }

    @Test
    public void test() throws IOException {

        final String[] args = new String[]{
                "-src",
                SRC.toString(),
                "-target",
                TARGET.toString()
        };
        App.main(args);
        Assert.assertTrue(Files.exists(TARGET));
        final List<String[]> srcContent =
                new CSVReader(new FileReader(SRC.toAbsolutePath().toString())).readAll();
        final List<String[]> targetContent =
                new CSVReader(new FileReader(TARGET.toAbsolutePath().toString()), '\t').readAll();
        IntStream.iterate(0, i -> i + 1)
                .limit(srcContent.size())
                .forEach(i -> Assert.assertArrayEquals(srcContent.get(i), targetContent.get(i)));

    }
}
