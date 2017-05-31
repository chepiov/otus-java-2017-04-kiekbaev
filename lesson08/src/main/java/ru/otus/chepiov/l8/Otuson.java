package ru.otus.chepiov.l8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Main API.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
@SuppressWarnings("WeakerAccess") // Public API
public final class Otuson {

    private final List<TypeWriter> writers;

    /**
     * Creates Otuson API presentation.
     */
    public Otuson() {
        writers = new ArrayList<TypeWriter>() {{
            add(new TypeWriters.PrimitiveWriter());
            add(new TypeWriters.ObjectWriter());
            add(new TypeWriters.StringWriter());
            add(new TypeWriters.ArrayWriter());
        }};

        Collections.sort(writers);
    }

    /**
     * Serializes defined object to JSON representation.
     *
     * @param object to serialize
     * @return JSON representation as String
     */
    public String toJson(final Object object) {
        if (Objects.isNull(object)) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        final Context context = new Context(this.writers, sb);
        context.write(object);
        return sb.toString();
    }
}
