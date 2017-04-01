package ru.otus.chepiov.l1;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Converts comma-separated value files to tab-separated value-files.
 * <p>
 * Usage: -src sourceFiles -target targetFile
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class App {
    public static void main(String[] args) throws IOException {
        final List<String[]> srcContent =
                new CSVReader(new FileReader(args[1])).readAll();
        final CSVWriter writer = new CSVWriter(new FileWriter(args[3]), '\t');
        srcContent.forEach(writer::writeNext);
        writer.close();
    }
}
