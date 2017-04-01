package ru.otus.chepiov.l1;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * Converts comma-separated value files to tab-separated value-files.
 * <p>
 * Usage: -src sourceFile -target targetFile
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class App {
    public static void main(String[] args) {
        if (args.length != 3 || !Objects.equals(args[0], "-src") || !Objects.equals(args[2], "-target")) {
            System.out.println("Usage: -src <sourceFile> -target <targetFile>");
        }
        if (Files.isRegularFile(Paths.get(args[1]))) {
            final List<String[]> srcContent;
            final CSVWriter writer;
            try {
                srcContent = new CSVReader(new FileReader(args[1])).readAll();
                writer = new CSVWriter(new FileWriter(args[3]), '\t');
                srcContent.forEach(writer::writeNext);
                writer.close();
            } catch (IOException e) {
                System.err.println("Error during execution");
            }
        } else {
            System.err.println("Illegal arguments");
        }

    }
}
