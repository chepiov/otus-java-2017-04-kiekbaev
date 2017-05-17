package ru.otus.chepiov.l5;

import ru.otus.chepiov.tf.Runner;

import java.util.ArrayList;
import java.util.List;

/**
 * Direct test runner.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class AppTestRunner {

    /**
     * Direct runner.
     *
     * @param args to use
     */
    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length < 1) {
            printUsage();
            return;
        }

        switch (args[0]) {
            case "-p":
                Runner.run(args[1]);
                break;
            case "-c":
                final List<Class<?>> suits = new ArrayList<>();
                for (int i = 1; i < args.length; i++) {
                    suits.add(Class.forName(args[i]));
                }
                Runner.run(suits.toArray(new Class<?>[]{}));
                break;
            default:
                printUsage();
        }
    }

    private static void printUsage() {
        System.err.println("Usage: AppTestRunner -p <packageName> or AppTestRunner -c <className> [<className> ...]");
    }
}
