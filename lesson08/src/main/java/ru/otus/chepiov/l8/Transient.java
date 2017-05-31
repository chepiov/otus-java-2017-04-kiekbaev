package ru.otus.chepiov.l8;

import java.lang.annotation.*;

/**
 * Marks fields and types to not to be serialized to JSON string.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transient {
}
