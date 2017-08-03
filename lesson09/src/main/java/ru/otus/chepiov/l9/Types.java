package ru.otus.chepiov.l9;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Type descriptor resolver.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public enum Types {

    STRING(String.class),
    LONG(long.class, Long.class),
    INT(int.class, Integer.class),
    BOOLEAN(boolean.class, Boolean.class),
    SHORT(short.class, Short.class),
    DOUBLE(double.class, Double.class),
    FLOAT(float.class, Float.class);

    private final Set<Class<?>> classes;

    Types(final Class<?>... classes) {
        this.classes = new HashSet<>(Arrays.asList(classes));
    }

    static Types fromClass(final Class<?> clazz) {
        return Arrays.stream(Types.values())
                .filter(c -> c.classes.contains(clazz))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

    }
}
