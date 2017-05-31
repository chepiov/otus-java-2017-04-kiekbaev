package ru.otus.chepiov.l8;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Default type writers.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
class TypeWriters {

    private TypeWriters() {
        throw new AssertionError("Non-instantiable");
    }

    /**
     * Arrays and Collections writer.
     */
    static final class ArrayWriter implements TypeWriter {

        @Override
        public boolean accessibleTo(final Object object) {
            return object.getClass().isArray() || object instanceof Collection;
        }

        @Override
        public int priority() {
            return 1;
        }

        @Override
        public void write(final StringBuilder builder, final Object object, final Context context) {
            if (!accessibleTo(object)) {
                throw new IllegalArgumentException("not an array");
            }

            final List<Object> objects = new ArrayList<>();
            if (object.getClass().isArray()) {
                objects.addAll(Arrays.asList((Object[]) object));
            } else {
                objects.addAll((Collection<?>) object);
            }

            builder.append("[");
            objects.forEach(o -> {
                context.write(o);
                builder.append(",");
            });
            normalize(builder);
            builder.append("]");
        }
    }

    /**
     * Object writer.
     */
    static final class ObjectWriter implements TypeWriter {

        @Override
        public boolean accessibleTo(Object object) {
            return true;
        }

        @Override
        public int priority() {
            return 5;
        }

        @Override
        public void write(final StringBuilder builder, final Object object, final Context context) {

            Class<?> type = object.getClass();

            if (type.isAnnotationPresent(Transient.class)) {
                return;
            }
            builder.append("{");

            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Transient.class) || Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                builder.append("\"").append(field.getName()).append("\":");
                field.setAccessible(true);
                try {
                    // it's okay to get object value from primitive fields - it will be wrapped automatically
                    Object value = field.get(object);
                    if (Objects.isNull(value)) { // null field
                        builder.append("null");
                    } else {
                        context.write(value);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Something wrong", e);
                }
                builder.append(",");
            }
            normalize(builder);
            builder.append("}");
        }
    }

    /**
     * String writer.
     */
    static final class StringWriter implements TypeWriter {

        @Override
        public boolean accessibleTo(Object object) {
            return object instanceof String;
        }

        @Override
        public int priority() {
            return 3;
        }

        @Override
        public void write(StringBuilder builder, Object object, Context context) {
            if (!accessibleTo(object)) {
                throw new IllegalArgumentException("not a String");
            }
            builder.append("\"").append(object).append("\"");
        }
    }

    /**
     * Primitive types and it's wrappers writer.
     */
    static final class PrimitiveWriter implements TypeWriter {

        @Override
        public boolean accessibleTo(Object object) {
            return object instanceof Number
                    || object instanceof Boolean
                    || object instanceof Character;
        }

        @Override
        public int priority() {
            return 3;
        }

        @Override
        public void write(final StringBuilder builder, final Object object, final Context context) {
            if (!(accessibleTo(object))) {
                throw new IllegalArgumentException("not a primitive");
            }
            if (object instanceof Number) {
                Number number = (Number) object;
                if (object instanceof Long
                        || object instanceof Integer
                        || object instanceof Short
                        || object instanceof Byte) {
                    builder.append(number.longValue());
                } else {
                    builder.append(number.doubleValue());
                }
            } else if (object instanceof Character) {
                builder.append("\"").append(object).append("\"");
            } else {
                builder.append(object);
            }
        }
    }

    private static void normalize(final StringBuilder sb) {
        if (sb.toString().endsWith(",")) {
            sb.setLength(sb.length() - 1);
        }
    }
}
