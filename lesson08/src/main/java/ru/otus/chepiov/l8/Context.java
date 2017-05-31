package ru.otus.chepiov.l8;

import java.util.ArrayList;
import java.util.List;

/**
 * Serialization context.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
class Context {
    private final List<TypeWriter> typeWriters;
    private final StringBuilder sb;

    /**
     * Creates context for serialization.
     *
     * @param typeWriters writers for matching writing objects
     * @param sb          string builder for using
     */
    Context(final List<TypeWriter> typeWriters, final StringBuilder sb) {
        this.typeWriters = new ArrayList<>(typeWriters);
        this.sb = sb;
    }

    /**
     * Writes object.
     *
     * @param object to write
     */
    void write(final Object object) {
        typeWriters.stream()
                .filter(tw -> tw.accessibleTo(object))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown type"))
                .write(sb, object, this);
    }
}
