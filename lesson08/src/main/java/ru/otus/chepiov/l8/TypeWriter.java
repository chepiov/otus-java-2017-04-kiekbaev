package ru.otus.chepiov.l8;

/**
 * Inner interface for type-specific json writing definition.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
interface TypeWriter extends Comparable<TypeWriter> {

    /**
     * Checks whether or not current writer can write specified object.
     *
     * @param object to check
     * @return true if can
     */
    boolean accessibleTo(final Object object);

    /**
     * Returns priority of writers. The lower is priority the eager it will be matched to write value.
     *
     * @return priority
     */
    int priority();

    /**
     * Writes object using context to specified builder.
     *
     * @param builder where to write
     * @param object  what to write
     * @param context what to use for continuing
     */
    void write(final StringBuilder builder, final Object object, final Context context);

    @Override
    default int compareTo(TypeWriter o) {
        return Integer.compare(priority(), o.priority());
    }
}
