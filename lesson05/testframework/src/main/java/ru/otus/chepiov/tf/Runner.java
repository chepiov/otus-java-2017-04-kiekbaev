package ru.otus.chepiov.tf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tests runner.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public final class Runner {

    private Runner() {
        throw new AssertionError("Non-instantiable");
    }

    /**
     * Run all tests in defined classes.
     *
     * @param testClasses to use
     */
    public static void run(final Class[] testClasses) throws AssertionError {
        final Map<String, Throwable> errors = new HashMap<>();
        final Map<String, AssertionError> assertionErrors = new HashMap<>();
        for (Class<?> clazz : testClasses) {

            final Object instance;
            try {
                instance = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                handleIllegalError(errors, clazz, e);
                continue;
            }

            final Optional<Method> beforeMethod = Arrays.stream(clazz.getMethods())
                    .filter(m -> m.isAnnotationPresent(Before.class))
                    .findFirst();

            final Optional<Method> afterMethod = Arrays.stream(clazz.getMethods())
                    .filter(m -> m.isAnnotationPresent(After.class))
                    .findFirst();

            final List<Method> testMethods = Arrays.stream(clazz.getMethods())
                    .filter(m -> m.isAnnotationPresent(Test.class))
                    .collect(Collectors.toList());

            testMethods.forEach(method -> {
                try {
                    if (beforeMethod.isPresent()) {
                        beforeMethod.get().invoke(instance);
                    }

                    method.invoke(instance);

                    if (afterMethod.isPresent()) {
                        afterMethod.get().invoke(instance);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    if (Objects.nonNull(e.getCause()) && e.getCause() instanceof AssertionError) {
                        handleAssertionError(assertionErrors, clazz, method, e);
                    } else {
                        handleIllegalError(errors, clazz, e);
                    }
                }
            });
        }

        if (!errors.isEmpty()) {
            final String msg = "class: " + String.join(", class: ", errors.keySet());
            final RuntimeException runtimeException
                    = new RuntimeException("Something wrong! Problem classes is >> " + msg);
            errors.forEach((cl, thr) -> runtimeException.addSuppressed(thr));
            throw runtimeException;
        } else if (!assertionErrors.isEmpty()) {
            final StringBuilder msg = new StringBuilder("Tests failed! \n");
            assertionErrors.forEach((cl, err) -> {
                msg.append("class: ")
                        .append(cl)
                        .append("\n\t Methods: ");
                for (Throwable e : err.getSuppressed()) {
                    msg.append("\n\t\t[")
                            .append(e.getMessage())
                            .append(": ")
                            .append(e.getCause().getMessage())
                            .append("] ");
                }
            });
            throw new AssertionError(msg);
        }
    }

    private static void handleAssertionError(final Map<String, AssertionError> assertionErrors,
                                             final Class<?> clazz,
                                             final Method method,
                                             final ReflectiveOperationException e) {
        AssertionError ae = (AssertionError) e.getCause();
        if (!assertionErrors.containsKey(clazz.getName())) {
            AssertionError assertFailed = new AssertionError();
            assertionErrors.put(clazz.getName(), assertFailed);
        }
        AssertionError assertion = new AssertionError(method.getName(), ae);
        assertionErrors.get(clazz.getName()).addSuppressed(assertion);
    }

    private static void handleIllegalError(final Map<String, Throwable> errors,
                                           final Class<?> clazz,
                                           final ReflectiveOperationException e) {
        if (!errors.containsKey(clazz.getName())) {
            Throwable err = new Throwable("Illegal error in handling class: " + clazz.getName());
            errors.put(clazz.getName(), err);
        }
        Throwable err = errors.get(clazz.getName());
        err.addSuppressed(e);
    }

    /**
     * Run all tests in defined package.
     * <p>
     * Note that test classes will be searched using current thread classloader.
     *
     * @param packageName to use
     */
    public static void run(final String packageName) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        run(ReflectionUtils.findAllAnnotatedClasses(packageName, loader));
    }
}
