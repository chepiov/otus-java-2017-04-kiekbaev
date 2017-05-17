package ru.otus.chepiov.tf;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reflection utilities.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public final class ReflectionUtils {

    private static final String CLASS_SUFFIX = ".class";

    private ReflectionUtils() {
        throw new AssertionError("Non-instantiable");
    }

    /**
     * Finds all classes in defined package containing methods annotated with {@link ru.otus.chepiov.tf.Test}.
     *
     * @param packageName to search
     * @param loader      to use
     * @return classes with annotated methods
     */
    public static Class<?>[] findAllAnnotatedClasses(final String packageName, final ClassLoader loader) {

        final String packagePath = packageName.replace('.', '/');
        final Enumeration<URL> packageResources;

        try {
            packageResources = loader.getResources(packagePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final List<Path> packageDirs = new ArrayList<>();

        while (packageResources.hasMoreElements()) {
            final URL resource = packageResources.nextElement();
            try {
                final URI uri = resource.toURI();
                final Map<String, String> env = new HashMap<>();
                final String[] array = uri.toString().split("!");
                if (array.length > 1) {
                    final FileSystem fs = FileSystems.newFileSystem(URI.create(array[0]), env);
                    final Path path = fs.getPath(array[1]);
                    packageDirs.add(path);
                } else {
                    packageDirs.add(Paths.get(uri));
                }
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        }


        List<Class> classes = packageDirs.stream().map(dir -> {
            if (Files.isRegularFile(dir)) {
                return Collections.<Class<?>>emptyList();
            } else {
                return findClasses(dir, packageName, loader);
            }
        }).flatMap(Collection::stream).collect(Collectors.toList());

        return classes.toArray(new Class[classes.size()]);
    }

    private static List<Class> findClasses(final Path dir, final String packageName, final ClassLoader loader) {

        List<Class> classes = new ArrayList<>();

        if (!Files.exists(dir)) {
            return classes;
        }


        try {
            Files.walk(dir).forEach(path -> {
                if (Files.isRegularFile(path) && path.toString().endsWith(CLASS_SUFFIX)) {
                    final String fileName = path.getFileName().toString().replace(".class", "");
                    final int pathDiff = path.getNameCount() - dir.getNameCount() - 1;
                    final StringBuilder className = new StringBuilder(packageName);
                    if (pathDiff > 0) {
                        for (int pathIdx = dir.getNameCount(); pathIdx < path.getNameCount() - 1; pathIdx++) {
                            className.append(".").append(path.getName(pathIdx));
                        }
                    }
                    className.append(".").append(fileName);

                    final Class<?> clazz;
                    try {
                        clazz = loader.loadClass(className.toString());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if (Arrays.stream(clazz.getMethods()).anyMatch(m -> m.isAnnotationPresent(Test.class))) {
                        classes.add(clazz);
                    }

                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return classes;
    }
}
